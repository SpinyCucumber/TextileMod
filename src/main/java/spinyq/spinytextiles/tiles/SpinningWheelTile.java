package spinyq.spinytextiles.tiles;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import spinyq.spinytextiles.ModItems;
import spinyq.spinytextiles.ModSounds;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.blocks.SpinningWheelBlock;
import spinyq.spinytextiles.items.FiberItem;
import spinyq.spinytextiles.utility.BlockInteraction;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.NBTHelper.ClassIdSpace;
import spinyq.spinytextiles.utility.NBTHelper.ObjectMapper;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.Fiber;

public class SpinningWheelTile extends TileEntity implements ITickableTileEntity {

	public static final int SPINNING_TIME = 60, REQUIRED_THREAD = 4;

	private static final String STATE_TAG = "State", FIBER_TAG = "Fiber", PREV_THREAD_TAG = "Prev",
			CURR_THREAD_TAG = "Curr";

	private static final ClassIdSpace CLASSES = new ClassIdSpace(BaseState.IdleState.class,
			BaseState.FiberState.class, BaseState.SpinningState.class, BaseState.FinishedState.class);

	public static interface SpinningWheelStateVisitor {

		default void visit(BaseState state) {
		}

		default void visit(BaseState.IdleState state) {
		}

		default void visit(BaseState.FiberState state) {
		}

		default void visit(BaseState.SpinningState state) {
		}

		default void visit(BaseState.FinishedState state) {
		}

	}

	/**
	 * A state that a spinning wheel may occupy.
	 * Handles player interactions, ticks, and saving/loading data.
	 */
	public interface ISpinningWheelState extends INBTSerializable<CompoundNBT> {

		ActionResultType onInteract(BlockInteraction interaction);

		void accept(SpinningWheelStateVisitor visitor);

		default void onTransitionTo() {
		}

		default void onTransitionFrom() {
		}

		default void tick() {
		}

		@Override
		default CompoundNBT serializeNBT() {
			return new CompoundNBT();
		}

		@Override
		default void deserializeNBT(CompoundNBT nbt) {
		}

	}

	/**
	 * The base state of the spinning wheel.
	 * Keeps track of the thread being spun, and has a substate.
	 */
	public class BaseState implements ISpinningWheelState {

		/**
		 * Used when the spinning wheel is waiting for more fiber to be added.
		 *
		 */
		public class IdleState implements ISpinningWheelState {

			public BaseState getSuperState() {
				return BaseState.this;
			}

			@Override
			public ActionResultType onInteract(BlockInteraction interaction) {
				if (interaction.item instanceof FiberItem) {
					if (!world.isRemote) {
						FiberItem fiberItem = (FiberItem) interaction.item;
						// Split off one item and retrieve fiber
						Fiber fiber = fiberItem.getFiber(interaction.itemstack.split(1));
						// Transition to FiberState
						transition(new FiberState(fiber));
						notifyChange();
					}
					// Play fun wool sound
					world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1.0F,
							1.0F);
					return ActionResultType.SUCCESS;
				}
				return ActionResultType.PASS;
			}

			@Override
			public void accept(SpinningWheelStateVisitor visitor) {
				visitor.visit(this);
			}

		}

		/**
		 * Used when the spinning wheel has fiber that is waiting to be spun.
		 *
		 */
		public class FiberState implements ISpinningWheelState {

			private Fiber fiber;

			public FiberState(Fiber fiber) {
				this.fiber = fiber;
			}

			public FiberState() {
			}

			public BaseState getSuperState() {
				return BaseState.this;
			}

			@Override
			public ActionResultType onInteract(BlockInteraction interaction) {
				if (!world.isRemote) {
					// Add fiber to thread and transition to spinning state
					prevThread = currThread;
					currThread = currThread.combine(fiber);
					transition(new SpinningState());
					notifyChange();
				}
				// Play a fun spinning sound
				world.playSound((PlayerEntity) null, pos, ModSounds.BLOCK_SPINNING_WHEEL_SPIN.get(),
						SoundCategory.BLOCKS, 1.0F, 1.0F);
				return ActionResultType.SUCCESS;
			}

			@Override
			public void accept(SpinningWheelStateVisitor visitor) {
				visitor.visit(this);
			}

			@Override
			public CompoundNBT serializeNBT() {
				CompoundNBT nbt = new CompoundNBT();
				nbt.put(FIBER_TAG, fiber.serializeNBT());
				return nbt;
			}

			@Override
			public void deserializeNBT(CompoundNBT nbt) {
				fiber = new Fiber();
				fiber.deserializeNBT(nbt.getCompound(FIBER_TAG));
			}

		}

		/**
		 * Used when the spinning wheel is actually spinning.
		 */
		public class SpinningState implements ISpinningWheelState {

			private int timer = 0;

			public BaseState getSuperState() {
				return BaseState.this;
			}

			public int getTime() {
				return timer;
			}

			@Override
			public void tick() {
				// Increment our timer
				timer++;
				// If the timer has finished, determine the result.
				// If we have enough fiber, transition to the finished state.
				// If not, go back to an idle state.
				if (timer == SPINNING_TIME) {
					transition((getCurrThread().amount >= REQUIRED_THREAD) ? new FinishedState() : new IdleState());
					notifyChange();
				}
			}

			@Override
			public void accept(SpinningWheelStateVisitor visitor) {
				visitor.visit(this);
			}

			@Override
			public ActionResultType onInteract(BlockInteraction interaction) {
				return ActionResultType.PASS;
			}

			// When the spinning wheel transitions to this state, set the spinning property to true so that
			// the block is animated.
			@Override
			public void onTransitionTo() {
				world.setBlockState(getPos(), getBlockState().with(SpinningWheelBlock.SPINNING, Boolean.TRUE));
			}

			// Make sure to set the spinning property to false when the spinning wheel transitions to another state.
			@Override
			public void onTransitionFrom() {
				world.setBlockState(getPos(), getBlockState().with(SpinningWheelBlock.SPINNING, Boolean.FALSE));
			}

		}

		/**
		 * Used when the spinning wheel has thread that is ready to be collected.
		 *
		 */
		public class FinishedState implements ISpinningWheelState {

			public BaseState getSuperState() {
				return BaseState.this;
			}

			@Override
			public ActionResultType onInteract(BlockInteraction interaction) {
				// If we are finished spinning thread, allow the player to put the thread on a
				// spindle item.
				if (interaction.item == ModItems.SPINDLE_ITEM.get()) {
					if (!world.isRemote) {
						// Consume one spindle
						interaction.itemstack.shrink(1);
						// Create new thread item and set color
						ItemStack threadItem = new ItemStack(ModItems.THREAD_ITEM.get());
						ModItems.THREAD_ITEM.get().setColor(threadItem, getCurrThread().color);
						// Add to player's inventory, drop if we can't
						if (!interaction.player.inventory.addItemStackToInventory(threadItem)) {
							interaction.player.dropItem(threadItem, false);
						}
						// Reset the spinning wheel's state
						SpinningWheelTile.this.state = new BaseState();
						notifyChange();
					}
					// Play a fun pop sound
					world.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS,
							1.0F, 1.0F);
					return ActionResultType.SUCCESS;
				}
				return ActionResultType.PASS;
			}

			@Override
			public void accept(SpinningWheelStateVisitor visitor) {
				visitor.visit(this);
			}

		}

		// Create an object mapper to serialize/deserialize the substate
		ObjectMapper mapper = new ObjectMapper(CLASSES).withSupplier(IdleState.class, IdleState::new)
				.withSupplier(FiberState.class, FiberState::new)
				.withSupplier(SpinningState.class, SpinningState::new)
				.withSupplier(FinishedState.class, FinishedState::new);

		private Fiber prevThread, currThread;
		private ISpinningWheelState state;

		public BaseState() {
			// Current thread is initiliaze to a "dummy" thread, so we can interpolate
			// colors and such.
			currThread = new Fiber(new RYBKColor(), 0);
			// Start in an idle state
			transition(new IdleState());
		}

		public Fiber getPrevThread() {
			return prevThread;
		}

		public Fiber getCurrThread() {
			return currThread;
		}

		public void transition(ISpinningWheelState state) {
			// Trigger callbacks
			if (this.state != null)
				this.state.onTransitionFrom();
			this.state = state;
			this.state.onTransitionTo();
		}

		@Override
		public void onTransitionTo() {
			state.onTransitionTo();
		}

		@Override
		public void onTransitionFrom() {
			state.onTransitionFrom();
		}

		@Override
		public void tick() {
			state.tick();
		}

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			return state.onInteract(interaction);
		}

		@Override
		public void accept(SpinningWheelStateVisitor visitor) {
			// Visit substate first
			state.accept(visitor);
			visitor.visit(this);
		}

		@Override
		public CompoundNBT serializeNBT() {
			// Write the previous thread, which may be null
			CompoundNBT nbt = new CompoundNBT();
			NBTHelper.putNullable(nbt, PREV_THREAD_TAG, prevThread);
			nbt.put(CURR_THREAD_TAG, currThread.serializeNBT());
			NBTHelper.putPolymorphic(nbt, STATE_TAG, state, mapper);
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			// First, read previous thread, which may be null
			prevThread = NBTHelper.getNullable(Fiber::new, nbt, PREV_THREAD_TAG);
			// Then read the current thread and the state
			// Transition to the new state
			currThread.deserializeNBT(nbt.getCompound(CURR_THREAD_TAG));
			transition(NBTHelper.getPolymorphic(nbt, STATE_TAG, mapper));
		}

	}

	private BaseState state;

	public SpinningWheelTile() {
		super(ModTiles.SPINNING_WHEEL_TILE.get());
		transition(new BaseState());
	}

	/**
	 * Syncs data with clients and marks this tile to be saved.
	 */
	private void notifyChange() {
		BlockState state = getBlockState();
		world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);
		markDirty();
	}
	
	public void transition(BaseState state) {
		// Trigger callbacks
		if (this.state != null)
			this.state.onTransitionFrom();
		this.state = state;
		this.state.onTransitionTo();
	}

	public ActionResultType onInteract(BlockInteraction interaction) {
		return state.onInteract(interaction);
	}

	public void accept(SpinningWheelStateVisitor visitor) {
		state.accept(visitor);
	}

	@Override
	public void tick() {
		state.tick();
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		// Read the new state and transition to it
		BaseState state = new BaseState();
		state.deserializeNBT(compound.getCompound(STATE_TAG));
		transition(state);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT result = super.write(compound);
		// Write the state
		result.put(STATE_TAG, state.serializeNBT());
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
