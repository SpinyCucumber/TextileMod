package spinyq.spinytextiles.tiles;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

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
import net.minecraftforge.common.util.INBTSerializable;
import spinyq.spinytextiles.ModTags;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.items.IBleachableItem;
import spinyq.spinytextiles.items.IDyeableItem;
import spinyq.spinytextiles.utility.BlockInteraction;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.NBTHelper.ClassIdSpace;
import spinyq.spinytextiles.utility.NBTHelper.ObjectMapper;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

public class BasinTile extends TileEntity {

	public static final int MAX_WATER_LEVEL = 8;
	public static final float DYE_MULTIPLIER = 0.25f, BLEACH_MULTIPLIER = 0.25f;

	private static final String STATE_TAG = "State", WATER_LEVEL_TAG = "Level", COLOR_TAG = "Color",
			BLEACH_LEVEL_TAG = "Bleach";

	private static final ClassIdSpace CLASSES = new ClassIdSpace(EmptyState.class, FilledState.class,
			FilledState.WaterState.class, FilledState.DyeState.class, FilledState.BleachState.class);

	/**
	 * A state that a basin can occupy. Handles player interactions.
	 * 
	 * @author SpinyQ
	 *
	 */
	public interface IBasinState extends INBTSerializable<CompoundNBT> {

		ActionResultType onInteract(BlockInteraction interaction);

		<T> Optional<T> accept(BasinStateVisitor<T> visitor);

		@Override
		default CompoundNBT serializeNBT() {
			return new CompoundNBT();
		}

		@Override
		default void deserializeNBT(CompoundNBT nbt) {
		}

	}

	/**
	 * A "greedy" state that consumes an interaction.
	 * 
	 * @author SpinyQ
	 *
	 */
	public interface IConsumerState extends IBasinState {

		boolean consumeInteraction(BlockInteraction interaction);

	}

	/**
	 * Allows users to define external operations on basin states, like rendering,
	 * animating, etc.
	 */
	public static interface BasinStateVisitor<T> {

		default Optional<T> visit(EmptyState state) {
			return Optional.empty();
		}

		default Optional<T> visit(FilledState state) {
			return Optional.empty();
		}

		default Optional<T> visit(FilledState.WaterState state) {
			return Optional.empty();
		}

		default Optional<T> visit(FilledState.DyeState state) {
			return Optional.empty();
		}

		default Optional<T> visit(FilledState.BleachState state) {
			return Optional.empty();
		}

	}

	/**
	 * Used when the basin contains no water. By default, basins start in this
	 * state.
	 * 
	 * @author SpinyQ
	 *
	 */
	public class EmptyState implements IBasinState {

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			// If player is holding a water bucket, empty the bucket and fill the cauldron.
			if (interaction.item == Items.WATER_BUCKET) {

				if (!world.isRemote) {
					if (!interaction.player.abilities.isCreativeMode) {
						interaction.player.setHeldItem(interaction.hand, new ItemStack(Items.BUCKET));
					}
					state = new FilledState();
					world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F,
							1.0F);
					notifyChange();
				}

				return ActionResultType.SUCCESS;

			}
			return ActionResultType.PASS;
		}

		@Override
		public <T> Optional<T> accept(BasinStateVisitor<T> visitor) {
			return visitor.visit(this);
		}

	}

	/**
	 * Used when the basin contains water.
	 * 
	 * @author SpinyQ
	 *
	 */
	public class FilledState implements IBasinState {

		public class WaterState implements IBasinState {

			private Collection<Supplier<IConsumerState>> suppliers = ImmutableList.of(DyeState::new, BleachState::new);

			public FilledState getSuperState() {
				return FilledState.this;
			}
			
			@Override
			public ActionResultType onInteract(BlockInteraction interaction) {
				// Check to see if any other states can handle the interaction
				// If they can, switch over to them.
				for (Supplier<IConsumerState> supplier : suppliers) {
					IConsumerState consumer = supplier.get();
					if (consumer.consumeInteraction(interaction)) {
						subState = consumer;
						notifyChange();
						return ActionResultType.SUCCESS;
					}
				}
				// If no other state can handle interaction, pass
				return ActionResultType.PASS;
			}

			@Override
			public <T> Optional<T> accept(BasinStateVisitor<T> visitor) {
				// Allow substate to override
				Optional<T> override = subState.accept(visitor);
				if (override.isPresent()) return override;
				return visitor.visit(this);
			}

		}

		/**
		 * Used when the basin contains some dye. Substate of FilledState.
		 * 
		 * @author SpinyQ
		 *
		 */
		public class DyeState implements IConsumerState, IDyeProvider {

			private RYBKColor color = new RYBKColor();

			public FilledState getSuperState() {
				return FilledState.this;
			}
			
			@Override
			public RYBKColor getColor() {
				return color;
			}

			@Override
			public boolean drain(int amount) {
				return FilledState.this.drain(amount);
			}

			@Override
			public boolean consumeInteraction(BlockInteraction interaction) {
				// Basin must be heated to consume dye
				if (!isHeated())
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

				if (!world.isRemote) {
					// Consume one item if player is not in creative
					if (!interaction.player.abilities.isCreativeMode) {
						interaction.itemstack.shrink(1);
					}
					// Change the color
					color = newColor;
					world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F,
							1.0F);
					notifyChange();
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
					if (dyeable.dye(new ContainedItemStack<>(interaction.itemstack, interaction.player.inventory),
							this)) {
						world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
								1.0F, 1.0F);
						return ActionResultType.SUCCESS;
					}
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
			public <T> Optional<T> accept(BasinStateVisitor<T> visitor) {
				return visitor.visit(this);
			}

		}

		/**
		 * Used when the basin contains some bleach. Substate of FilledState.
		 * 
		 * @author SpinyQ
		 *
		 */
		public class BleachState implements IConsumerState, IBleachProvider {

			private float bleachLevel = 0.0f;

			public FilledState getSuperState() {
				return FilledState.this;
			}
			
			@Override
			public float getBleachLevel() {
				return bleachLevel;
			}

			@Override
			public boolean drain(int amount) {
				return FilledState.this.drain(amount);
			}

			@Override
			public boolean consumeInteraction(BlockInteraction interaction) {
				// Basin must be heated to add more bleach
				if (!isHeated())
					return false;
				if (!interaction.item.getTags().contains(ModTags.LYE_TAG))
					return false;
				// If bleach level didn't change at all, don't accept bleach
				float newBleachLevel = Math.min(bleachLevel + BLEACH_MULTIPLIER, 1.0f);
				if (newBleachLevel == bleachLevel)
					return false;

				if (!world.isRemote) {
					// Consume one item if player is not in creative
					if (!interaction.player.abilities.isCreativeMode) {
						interaction.itemstack.shrink(1);
					}
					// Change the bleach level
					bleachLevel = newBleachLevel;
					world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F,
							1.0F);
					notifyChange();
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
					if (bleachable.bleach(new ContainedItemStack<>(interaction.itemstack, interaction.player.inventory),
							this)) {
						world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
								1.0F, 1.0F);
						return ActionResultType.SUCCESS;
					}
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
			public <T> Optional<T> accept(BasinStateVisitor<T> visitor) {
				return visitor.visit(this);
			}

		}

		// Water level starts out at maximum
		private int waterLevel = MAX_WATER_LEVEL;
		private IBasinState subState = new WaterState();
		private ObjectMapper mapper = BasinTile.this.mapper.copy().withSupplier(WaterState.class, WaterState::new)
				.withSupplier(DyeState.class, DyeState::new).withSupplier(BleachState.class, BleachState::new);

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
			if (waterLevel == 0) {
				state = new EmptyState();
			}
			notifyChange();
			return true;
		}

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			return subState.onInteract(interaction);
		}

		@Override
		public CompoundNBT serializeNBT() {
			// Write water level
			CompoundNBT result = new CompoundNBT();
			result.putInt(WATER_LEVEL_TAG, waterLevel);
			NBTHelper.putPolymorphic(result, STATE_TAG, subState, mapper);
			return result;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			// Read water level
			waterLevel = nbt.getInt(WATER_LEVEL_TAG);
			subState = NBTHelper.getPolymorphic(nbt, STATE_TAG, mapper);
		}

		@Override
		public <T> Optional<T> accept(BasinStateVisitor<T> visitor) {
			return visitor.visit(this);
		}

	}

	private IBasinState state = new EmptyState();
	private ObjectMapper mapper = new ObjectMapper(CLASSES).withSupplier(EmptyState.class, EmptyState::new)
			.withSupplier(FilledState.class, FilledState::new);

	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
	}

	/**
	 * Whether the basin is heated. Heat is required to dye and bleach objects.
	 * 
	 * @return
	 */
	public boolean isHeated() {
		return CampfireBlock.isLitCampfireInRange(world, pos, 2);
	}

	public <T> Optional<T> accept(BasinStateVisitor<T> visitor) {
		return state.accept(visitor);
	}

	public ActionResultType onInteract(BlockInteraction interaction) {
		return state.onInteract(interaction);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		// Read state
		state = NBTHelper.getPolymorphic(compound, STATE_TAG, mapper);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT result = super.write(compound);
		// Write state
		NBTHelper.putPolymorphic(result, STATE_TAG, state, mapper);
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

	/**
	 * Syncs data with clients and marks this tile to be saved.
	 */
	private void notifyChange() {
		BlockState state = getBlockState();
		world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);
		markDirty();
	}

}
