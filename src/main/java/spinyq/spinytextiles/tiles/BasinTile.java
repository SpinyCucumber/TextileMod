package spinyq.spinytextiles.tiles;

import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import spinyq.spinytextiles.ModTags;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.items.IBleachableItem;
import spinyq.spinytextiles.items.IDyeableItem;
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
// TODO Notify updates so that we can sync client, also mark dirty
public class BasinTile extends TileEntity {

	public static final int MAX_WATER_LEVEL = 8;
	public static final float LYE_VALUE = 0.25f;

	private static final String STATE_TAG = "State",
			WATER_LEVEL_TAG = "Level",
			COLOR_TAG = "Color",
			BLEACH_LEVEL_TAG = "Bleach";

	private static final ClassMapper CLASS_MAPPER = new ClassMapper(EmptyState.class, FilledState.class, DyeState.class, BleachState.class);

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

	public static abstract class BasinState extends State<BasinState> {

		public abstract ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit);
		public abstract <T> T accept(BasinStateVisitor<T> visitor);
		
	}

	public class FilledState extends BasinState {
	
		private Set<Supplier<SaturatedState>> substateSuppliers = ImmutableSet.of(DyeState::new, BleachState::new);
	
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
			return true;
		}
	
		// TODO Simplify this
		@Override
		public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// Check if a substate can handle the action.
			// If they can, push the new state
			for (Supplier<SaturatedState> supplier : substateSuppliers) {
				SaturatedState subState = supplier.get();
				if (subState.consumeInteraction(player, handIn, hit)) {
					fsm.pushState(subState);
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
		public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// Get itemstack and item
			ItemStack itemstack = player.getHeldItem(handIn);
			Item item = itemstack.getItem();
			// If player is holding a water bucket, empty the bucket and fill the cauldron.
			if (item == Items.WATER_BUCKET) {

				if (!BasinTile.this.world.isRemote) {
					if (!player.abilities.isCreativeMode) {
						player.setHeldItem(handIn, new ItemStack(Items.BUCKET));
					}
					fsm.swapState(this, new FilledState());
					BasinTile.this.world.playSound((PlayerEntity) null, BasinTile.this.pos, SoundEvents.ITEM_BUCKET_EMPTY,
							SoundCategory.BLOCKS, 1.0F, 1.0F);
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

	public static abstract class SaturatedState extends BasinState {

		public abstract boolean consumeInteraction(PlayerEntity player, Hand handIn, BlockRayTraceResult hit);

		public abstract ActionResultType finishInteraction(PlayerEntity player, Hand handIn, BlockRayTraceResult hit);

		@Override
		public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// If we can consume, return success
			if (consumeInteraction(player, handIn, hit))
				return ActionResultType.SUCCESS;
			else
				return finishInteraction(player, handIn, hit);
		}

	}

	public class DyeState extends SaturatedState implements IDyeProvider {

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
		public boolean consumeInteraction(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// Get itemstack and item
			ItemStack itemStack = player.getHeldItem(handIn);
			Item item = itemStack.getItem();
			if (!(item instanceof DyeItem))
				return false;
			// Retrieve the color of the dye
			DyeItem dye = (DyeItem) item;
			RYBKColor dyeColor = new RYBKColor().fromDye(dye.getDyeColor());
			// If new color didn't change at all, don't accept dye
			RYBKColor newColor = color.plus(dyeColor).clamp();
			if (newColor.equals(color))
				return false;

			if (!BasinTile.this.world.isRemote) {
				// Consume one item if player is not in creative
				if (!player.abilities.isCreativeMode) {
					itemStack.shrink(1);
				}
				// Change the color
				color = newColor;
				BasinTile.this.world.playSound((PlayerEntity) null, BasinTile.this.pos, SoundEvents.ITEM_BUCKET_EMPTY,
						SoundCategory.BLOCKS, 1.0F, 1.0F);
			}

			return true;
		}

		@Override
		public ActionResultType finishInteraction(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// Get itemstack and item
			ItemStack itemStack = player.getHeldItem(handIn);
			Item item = itemStack.getItem();
			// If item is dyeable, dye it
			if (item instanceof IDyeableItem) {
				IDyeableItem dyeable = (IDyeableItem) item;
				ContainedItemStack<PlayerInventory> containedStack = new ContainedItemStack<>(itemStack,
						player.inventory);
				if (dyeable.dye(containedStack, this))
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

	public class BleachState extends SaturatedState implements IBleachProvider {

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
		public boolean consumeInteraction(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// Get itemstack and item
			ItemStack itemStack = player.getHeldItem(handIn);
			Item item = itemStack.getItem();
			if (!item.getTags().contains(ModTags.LYE_TAG))
				return false;
			// If bleach level didn't change at all, don't accept bleach
			float newBleachLevel = Math.min(bleachLevel + LYE_VALUE, 1.0f);
			if (newBleachLevel == bleachLevel)
				return false;

			if (!BasinTile.this.world.isRemote) {
				// Consume one item if player is not in creative
				if (!player.abilities.isCreativeMode) {
					itemStack.shrink(1);
				}
				// Change the bleach level
				bleachLevel = newBleachLevel;
				BasinTile.this.world.playSound((PlayerEntity) null, BasinTile.this.pos, SoundEvents.ITEM_BUCKET_EMPTY,
						SoundCategory.BLOCKS, 1.0F, 1.0F);
			}

			return true;
		}

		@Override
		public ActionResultType finishInteraction(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// Get itemstack and item
			ItemStack itemStack = player.getHeldItem(handIn);
			Item item = itemStack.getItem();
			// If item is bleachable, bleach it
			if (item instanceof IDyeableItem) {
				IBleachableItem bleachable = (IBleachableItem) item;
				ContainedItemStack<PlayerInventory> containedStack = new ContainedItemStack<>(itemStack,
						player.inventory);
				if (bleachable.bleach(containedStack, this))
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

	private StackFSM<BasinState> fsm = new StackFSM<>(CLASS_MAPPER);

	public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		return fsm.getState().onInteract(player, handIn, hit);
	}
	
	public <T> T accept(BasinStateVisitor<T> visitor) {
		return fsm.getState().accept(visitor);
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
		// TODO Auto-generated method stub
		return super.getUpdatePacket();
	}

	@Override
	public CompoundNBT getUpdateTag() {
		// TODO Auto-generated method stub
		return super.getUpdateTag();
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		// TODO Auto-generated method stub
		super.onDataPacket(net, pkt);
	}

	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		// TODO Auto-generated method stub
		super.handleUpdateTag(tag);
	}

	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
		// Push some initial state
		fsm.pushState(new EmptyState());
	}

}
