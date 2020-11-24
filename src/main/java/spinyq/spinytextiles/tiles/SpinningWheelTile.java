package spinyq.spinytextiles.tiles;

import java.util.Stack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import spinyq.spinytextiles.ModItems;
import spinyq.spinytextiles.ModSounds;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.items.IFiberItem;
import spinyq.spinytextiles.utility.BlockInteraction;
import spinyq.spinytextiles.utility.EvictingStack;
import spinyq.spinytextiles.utility.NBTHelper.ClassMapper;
import spinyq.spinytextiles.utility.StackFSM;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.FiberInfo;

public class SpinningWheelTile extends TileEntity implements ITickableTileEntity {

	private static final int SPINNING_TIME = 60,
			REQUIRED_THREAD = 4;
	
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

		private Stack<FiberInfo> threadInfo;

		public ThreadState() {
			threadInfo = new EvictingStack<>(2);
			// Add a "dummy" fiberInfo so we can interpolate.
			threadInfo.add(new FiberInfo(new RYBKColor(), 0));
		}
		
		@Override
		public void acceptThread(FiberInfo fiber) {
			// Combine previous info with new, and push to our "memory"
			threadInfo.push(threadInfo.peek().combine(fiber));
			// Push new spinning state
			fsm.pushState(new SpinningState());
		}
		
		public FiberInfo getThread() {
			return threadInfo.peek();
		}

		@Override
		public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
			return visitor.visit(this);
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
				boolean finished = ((ThreadState) superState).getThread().amount >= REQUIRED_THREAD;
				fsm.popState(this);
				fsm.pushState(finished ? new FinishedState() : new IdleState());
			}
		}

		@Override
		public <T> T accept(SpinningWheelStateVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
		// TODO Set blockstate
		
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
					ModItems.THREAD_ITEM.get().setColor(threadItem, ((ThreadState) superState).getThread().color);
					// Add to player's inventory, drop if we can't
					if (!interaction.player.inventory.addItemStackToInventory(threadItem)) {
						interaction.player.dropItem(threadItem, false);
					}
					// Transition to an empty, idle state
					fsm.popState(superState);
					fsm.pushState(new EmptyState());
					fsm.pushState(new IdleState());
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
	
	public ActionResultType onInteract(BlockInteraction interaction) {
		return fsm.getState().onInteract(interaction);
	}

	@Override
	public void tick() {
		fsm.getState().tick();
	}

}
