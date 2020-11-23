package spinyq.spinytextiles.tiles;

import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.util.Constants;
import spinyq.spinytextiles.ModTags;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.items.IBleachableItem;
import spinyq.spinytextiles.items.IDyeableItem;
import spinyq.spinytextiles.utility.BlockInteraction;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.NBTHelper.ClassMapper;
import spinyq.spinytextiles.utility.StackFSM;
import spinyq.spinytextiles.utility.StackFSM.State;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

/**
 * Uses a stack-based FSM
 * 
 * @author Elijah Hilty
 *
 */
public class BasinTile extends TileEntity {

	public static final int MAX_WATER_LEVEL = 8;
	public static final float DYE_MULTIPLIER = 0.25f, BLEACH_MULTIPLIER = 0.25f;

	private static final String STATE_TAG = "State", WATER_LEVEL_TAG = "Level", COLOR_TAG = "Color",
			BLEACH_LEVEL_TAG = "Bleach";

	public static interface BasinStateVisitor<T> {

		default T visit(EmptyState state) {
			return null;
		}

		default T visit(FilledState state) {
			return null;
		}

		default T visit(DyeState state) {
			return null;
		}

		default T visit(BleachState state) {
			return null;
		}

	}

	public abstract class BasinState extends State<BasinState> {

		public abstract ActionResultType onInteract(BlockInteraction interaction);

		public abstract <T> T accept(BasinStateVisitor<T> visitor);

	}

	public abstract class ConsumerState extends BasinState {

		public abstract boolean consumeInteraction(BlockInteraction interaction);

	}

	public class FilledState extends BasinState {

		private Set<Supplier<ConsumerState>> substateSuppliers = ImmutableSet.of(DyeState::new, BleachState::new);

		// Water level starts out at maximum
		private int waterLevel = MAX_WATER_LEVEL;

		public double getWaterHeight() {
			return 0.2 + ((double) waterLevel / (double) MAX_WATER_LEVEL) * 0.875;
		}

		public int getWaterLevel() {
			return waterLevel;
		}

		public boolean drain(int amount) {
			// Fail if amount is greater than water level
			if (amount > waterLevel)
				return false;
			// Subtract amount from water level
			// If water level reaches zero, transition to empty state
			waterLevel -= amount;
			if (waterLevel == 0)
				fsm.swapState(this, new EmptyState());
			BasinTile.this.notifyChange();
			return true;
		}

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			// Check if a substate can handle the action.
			// If they can, push the new state
			for (Supplier<ConsumerState> supplier : substateSuppliers) {
				ConsumerState subState = supplier.get();
				if (subState.consumeInteraction(interaction)) {
					fsm.pushState(subState);
					BasinTile.this.notifyChange();
					return ActionResultType.SUCCESS;
				}
			}
			return ActionResultType.PASS;
		}

		@Override
		public CompoundNBT serializeNBT() {
			// Write water level
			CompoundNBT result = new CompoundNBT();
			result.putInt(WATER_LEVEL_TAG, waterLevel);
			return result;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			// Read water level
			waterLevel = nbt.getInt(WATER_LEVEL_TAG);
		}

		@Override
		public <T> T accept(BasinStateVisitor<T> visitor) {
			return visitor.visit(this);
		}

	}

	public class EmptyState extends BasinState {

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			// If player is holding a water bucket, empty the bucket and fill the cauldron.
			if (interaction.item == Items.WATER_BUCKET) {

				if (!BasinTile.this.world.isRemote) {
					if (!interaction.player.abilities.isCreativeMode) {
						interaction.player.setHeldItem(interaction.hand, new ItemStack(Items.BUCKET));
					}
					fsm.swapState(this, new FilledState());
					BasinTile.this.world.playSound((PlayerEntity) null, BasinTile.this.pos,
							SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
					BasinTile.this.notifyChange();
				}

				return ActionResultType.SUCCESS;

			}
			return ActionResultType.PASS;
		}

		@Override
		public <T> T accept(BasinStateVisitor<T> visitor) {
			return visitor.visit(this);
		}

	}

	public class DyeState extends ConsumerState implements IDyeProvider {

		private RYBKColor color = new RYBKColor();

		@Override
		public RYBKColor getColor() {
			return color;
		}

		@Override
		public boolean drain(int amount) {
			return ((FilledState) superState).drain(amount);
		}

		@Override
		public boolean consumeInteraction(BlockInteraction interaction) {
			// Basin must be heated to consume dye
			if (!BasinTile.this.isHeated())
				return false;
			if (!(interaction.item instanceof DyeItem))
				return false;
			// Retrieve the color of the dye
			DyeItem dye = (DyeItem) interaction.item;
			RYBKColor dyeColor = new RYBKColor().fromDye(dye.getDyeColor());
			// If new color didn't change at all, don't accept dye
			RYBKColor newColor = color.plus(dyeColor.scaledBy(DYE_MULTIPLIER)).clamp();
			if (newColor.equals(color))
				return false;

			if (!BasinTile.this.world.isRemote) {
				// Consume one item if player is not in creative
				if (!interaction.player.abilities.isCreativeMode) {
					interaction.itemstack.shrink(1);
				}
				// Change the color
				color = newColor;
				BasinTile.this.world.playSound((PlayerEntity) null, BasinTile.this.pos, SoundEvents.ITEM_BUCKET_EMPTY,
						SoundCategory.BLOCKS, 1.0F, 1.0F);
				BasinTile.this.notifyChange();
			}

			return true;
		}

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			// If we can consume, return success
			if (consumeInteraction(interaction))
				return ActionResultType.SUCCESS;
			// If item is dyeable, dye it
			if (interaction.item instanceof IDyeableItem) {
				IDyeableItem dyeable = (IDyeableItem) interaction.item;
				if (dyeable.dye(new ContainedItemStack<>(interaction.itemstack, interaction.player.inventory), this))
					BasinTile.this.world.playSound((PlayerEntity) null, BasinTile.this.pos,
							SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return ActionResultType.SUCCESS;
			}
			// TODO Glowstone saturation modifier
			return ActionResultType.PASS;
		}

		@Override
		public CompoundNBT serializeNBT() {
			// Write color
			CompoundNBT result = new CompoundNBT();
			result.putInt(COLOR_TAG, color.toInt());
			return result;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			// Read color
			color = new RYBKColor().fromInt(nbt.getInt(COLOR_TAG));
		}

		@Override
		public <T> T accept(BasinStateVisitor<T> visitor) {
			return visitor.visit(this);
		}

	}

	public class BleachState extends ConsumerState implements IBleachProvider {

		private float bleachLevel = 0.0f;

		@Override
		public float getBleachLevel() {
			return bleachLevel;
		}

		@Override
		public boolean drain(int amount) {
			return ((FilledState) superState).drain(amount);
		}

		@Override
		public boolean consumeInteraction(BlockInteraction interaction) {
			// Basin must be heated to add more bleach
			if (!BasinTile.this.isHeated())
				return false;
			if (!interaction.item.getTags().contains(ModTags.LYE_TAG))
				return false;
			// If bleach level didn't change at all, don't accept bleach
			float newBleachLevel = Math.min(bleachLevel + BLEACH_MULTIPLIER, 1.0f);
			if (newBleachLevel == bleachLevel)
				return false;

			if (!BasinTile.this.world.isRemote) {
				// Consume one item if player is not in creative
				if (!interaction.player.abilities.isCreativeMode) {
					interaction.itemstack.shrink(1);
				}
				// Change the bleach level
				bleachLevel = newBleachLevel;
				BasinTile.this.world.playSound((PlayerEntity) null, BasinTile.this.pos, SoundEvents.ITEM_BUCKET_EMPTY,
						SoundCategory.BLOCKS, 1.0F, 1.0F);
				BasinTile.this.notifyChange();
			}

			return true;
		}

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			// If we can consume, return success
			if (consumeInteraction(interaction))
				return ActionResultType.SUCCESS;
			// If item is bleachable, bleach it
			if (interaction.item instanceof IDyeableItem) {
				IBleachableItem bleachable = (IBleachableItem) interaction.item;
				if (bleachable.bleach(new ContainedItemStack<>(interaction.itemstack, interaction.player.inventory), this))
					BasinTile.this.world.playSound((PlayerEntity) null, BasinTile.this.pos,
							SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.PASS;
		}

		@Override
		public CompoundNBT serializeNBT() {
			// Write bleach level
			CompoundNBT result = new CompoundNBT();
			result.putFloat(BLEACH_LEVEL_TAG, bleachLevel);
			return result;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			// Read bleach level
			bleachLevel = nbt.getFloat(BLEACH_LEVEL_TAG);
		}

		@Override
		public <T> T accept(BasinStateVisitor<T> visitor) {
			return visitor.visit(this);
		}

	}

	private StackFSM<BasinState> fsm;

	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
		// Construct fsm and push some initial state
		ClassMapper mapper = new ClassMapper().withClass(EmptyState.class, EmptyState::new)
				.withClass(FilledState.class, FilledState::new).withClass(DyeState.class, DyeState::new)
				.withClass(BleachState.class, BleachState::new);
		fsm = new StackFSM<>(mapper);
		fsm.pushState(new EmptyState());
	}

	/**
	 * Syncs data with clients and marks this tile to be saved.
	 */
	private void notifyChange() {
		BlockState state = getBlockState();
		world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);
		markDirty();
	}

	public ActionResultType onInteract(BlockInteraction interaction) {
		return fsm.getState().onInteract(interaction);
	}

	public <T> T accept(BasinStateVisitor<T> visitor) {
		return fsm.getState().accept(visitor);
	}

	/**
	 * Whether the basin is heated. Heat is required to dye and bleach objects.
	 * 
	 * @return
	 */
	public boolean isHeated() {
		return CampfireBlock.isLitCampfireInRange(world, pos, 2);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		// Retrieve a list NBT
		fsm.deserializeNBT(compound.getList(STATE_TAG, 10));
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT result = super.write(compound);
		// Create a new ListNBT
		result.put(STATE_TAG, fsm.serializeNBT());
		return result;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbtTag = new CompoundNBT();
		// Write data into the nbtTag
		write(nbtTag);
		return new SUpdateTileEntityPacket(getPos(), -1, nbtTag);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbtTagCompound = new CompoundNBT();
		write(nbtTagCompound);
		return nbtTagCompound;
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		CompoundNBT tag = pkt.getNbtCompound();
		// Handle packet
		read(tag);
	}

	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		read(tag);
	}

}
