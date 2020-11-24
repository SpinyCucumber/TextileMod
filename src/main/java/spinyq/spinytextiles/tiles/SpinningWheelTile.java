package spinyq.spinytextiles.tiles;

import java.util.Stack;

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
import spinyq.spinytextiles.ModItems;
import spinyq.spinytextiles.ModSounds;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.blocks.SpinningWheelBlock;
import spinyq.spinytextiles.items.IFiberItem;
import spinyq.spinytextiles.utility.BlockInteraction;
import spinyq.spinytextiles.utility.EvictingStack;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.NBTHelper.ClassMapper;
import spinyq.spinytextiles.utility.StackFSM;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.FiberInfo;

public class SpinningWheelTile extends TileEntity implements ITickableTileEntity {

	public static final int SPINNING_TIME = 60,
			REQUIRED_THREAD = 4;
	
	private static final String STATE_TAG = "State",
			FIBER_TAG = "Fiber",
			THREAD_TAG = "Thread";
	
	public static interface SpinningWheelStateVisitor<T> {
		
		default T visit(EmptyState state) { return null; }
		default T visit(ThreadState state) { return null; }
		default T visit(IdleState state) { return null; }
		default T visit(FiberState state) { return null; }
		default T visit(SpinningState state) { return null; }
		default T visit(FinishedState state) { return null; }
		
	}
	
	/**
	 * A state that a spinning wheel may occupy.
	 * Handles player interactions.
	 */
	public abstract class SpinningWheelState extends StackFSM.State<SpinningWheelState> {
		
		public ActionResultType onInteract(BlockInteraction interaction) { return ActionResultType.PASS; }
		public void tick() { }
		public abstract <T> T accept(SpinningWheelStateVisitor<T> visitor);
		
	}
	
	public abstract class ThreadAcceptorState extends SpinningWheelState {
		
		public abstract void acceptThread(FiberInfo fiber);
		
	}
	
	/**
	 * Used when the spinning wheel has no thread being spun.
	 *
	 */
	public class EmptyState extends ThreadAcceptorState {

		@Override
		public void acceptThread(FiberInfo fiber) {
			ThreadState threadState = new ThreadState();
			fsm.popState(this);
			fsm.pushState(threadState);
			threadState.acceptThread(fiber);
		}

		@Override
		public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	/**
	 * Used when the spinning wheel has thread.
	 *
	 */
	public class ThreadState extends ThreadAcceptorState {

		// TODO Do something simpler
		private Stack<FiberInfo> thread;

		public ThreadState() {
			thread = new EvictingStack<>(2);
			// Add a "dummy" fiberInfo so we can interpolate.
			thread.add(new FiberInfo(new RYBKColor(), 0));
		}
		
		@Override
		public void acceptThread(FiberInfo fiber) {
			// Combine previous info with new, and push to our "memory"
			thread.push(thread.peek().combine(fiber));
			// Push new spinning state
			fsm.pushState(new SpinningState());
			notifyChange();
		}
		
		public FiberInfo getThread(int i) {
			return thread.get(thread.size() - i - 1);
		}

		@Override
		public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
			return visitor.visit(this);
		}

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT nbt = new CompoundNBT();
			NBTHelper.putCollection(nbt, THREAD_TAG, thread);
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			thread = NBTHelper.getCollection(() -> new EvictingStack<>(2), FiberInfo::new, nbt, THREAD_TAG);
		}
		
	}
	
	/**
	 * Used when the spinning wheel is waiting for more fiber to be added.
	 *
	 */
	public class IdleState extends SpinningWheelState {

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			if (interaction.item instanceof IFiberItem) {
				if (!world.isRemote) {
					IFiberItem fiberItem = (IFiberItem) interaction.item;
					// Split off one item and retrieve fiber info
					FiberInfo info = fiberItem.getInfo(interaction.itemstack.split(1));
					// Transition to FiberState
					fsm.popState(this);
					fsm.pushState(new FiberState(info));
					notifyChange();
				}
				// Play fun wool sound
				world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_WOOL_PLACE,
						SoundCategory.BLOCKS, 1.0F, 1.0F);
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.PASS;
		}

		@Override
		public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	/**
	 * Used when the spinning wheel has fiber that is waiting to be spun.
	 *
	 */
	public class FiberState extends SpinningWheelState {
		
		private FiberInfo fiber;

		public FiberState(FiberInfo fiber) {
			this.fiber = fiber;
		}
		
		public FiberState() { }

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			if (!world.isRemote) {
				fsm.popState(this);
				// Let superstate accept thread
				((ThreadAcceptorState) superState).acceptThread(fiber);
			}
			// Play a fun spinning sound
			world.playSound((PlayerEntity) null, pos, ModSounds.BLOCK_SPINNING_WHEEL_SPIN.get(),
					SoundCategory.BLOCKS, 1.0F, 1.0F);
			return ActionResultType.SUCCESS;
		}

		@Override
		public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
			return visitor.visit(this);
		}

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put(FIBER_TAG, fiber.serializeNBT());
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {
			fiber = new FiberInfo();
			fiber.deserializeNBT(nbt.getCompound(FIBER_TAG));
		}
		
	}
	
	/**
	 * Used when the spinning wheel is actually spinning.
	 *
	 */
	public class SpinningState extends SpinningWheelState {
		
		private int timer = 0;

		@Override
		public void tick() {
			// Increment our timer
			timer++;
			if (timer == SPINNING_TIME) {
				boolean finished = ((ThreadState) superState).getThread(0).amount >= REQUIRED_THREAD;
				fsm.popState(this);
				fsm.pushState(finished ? new FinishedState() : new IdleState());
				notifyChange();
			}
		}

		@Override
		public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
		public int getTime() {
			return timer;
		}

		@Override
		public void onPush() {
			world.setBlockState(getPos(), getBlockState().with(SpinningWheelBlock.SPINNING, Boolean.TRUE));
		}

		@Override
		public void onPop() {
			world.setBlockState(getPos(), getBlockState().with(SpinningWheelBlock.SPINNING, Boolean.FALSE));
		}
		
	}
	
	/**
	 * Used when the spinning wheel has thread that is ready to be collected.
	 *
	 */
	public class FinishedState extends SpinningWheelState {

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			// If we are finished spinning thread, allow the player to put the thread on a spindle item.
			if (interaction.item == ModItems.SPINDLE_ITEM.get()) {
				if (!world.isRemote) {
					// Consume one spindle
					interaction.itemstack.shrink(1);
					// Create new thread item and set color
					ItemStack threadItem = new ItemStack(ModItems.THREAD_ITEM.get());
					ModItems.THREAD_ITEM.get().setColor(threadItem, ((ThreadState) superState).getThread(0).color);
					// Add to player's inventory, drop if we can't
					if (!interaction.player.inventory.addItemStackToInventory(threadItem)) {
						interaction.player.dropItem(threadItem, false);
					}
					// Transition to an empty, idle state
					fsm.popState(superState);
					fsm.pushState(new EmptyState());
					fsm.pushState(new IdleState());
					notifyChange();
				}
				// Play a fun pop sound
				world.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_ITEM_PICKUP,
						SoundCategory.BLOCKS, 1.0F, 1.0F);
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.PASS;
		}

		@Override
		public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	private StackFSM<SpinningWheelState> fsm;
	
	public SpinningWheelTile() {
		super(ModTiles.SPINNING_WHEEL_TILE.get());
		ClassMapper mapper = new ClassMapper()
				.withClass(EmptyState.class, EmptyState::new)
				.withClass(ThreadState.class, ThreadState::new)
				.withClass(IdleState.class, IdleState::new)
				.withClass(FiberState.class, FiberState::new)
				.withClass(SpinningState.class, SpinningState::new)
				.withClass(FinishedState.class, FinishedState::new);
		fsm = new StackFSM<>(mapper);
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

	@Override
	public void tick() {
		fsm.getState().tick();
	}
	
	public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
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
