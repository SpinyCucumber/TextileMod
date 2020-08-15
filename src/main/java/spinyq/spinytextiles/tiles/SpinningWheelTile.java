package spinyq.spinytextiles.tiles;

import java.util.Optional;
import java.util.Stack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import spinyq.spinytextiles.ModItems;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.TextileMod;
import spinyq.spinytextiles.items.IFiberItem;
import spinyq.spinytextiles.utility.EvictingStack;
import spinyq.spinytextiles.utility.FiberInfo;
import spinyq.spinytextiles.utility.NBTHelper;

public class SpinningWheelTile extends TileEntity implements ITickableTileEntity {

	private static final String TAG_THREAD_INFO = "ThreadInfo", TAG_FIBER_INFO = "FiberInfo", TAG_SPINNING = "Spinning";
	
	private static final int SPINNING_TIME = 20, REQUIRED_THREAD = 8;
	
	// A queue containing info about the thread being spun.
	// Need to keep multiple references so that we can animate.
	private Stack<FiberInfo> threadInfo = new EvictingStack<>(2);
	private Optional<FiberInfo> fiberInfo = Optional.empty();
	// Used for the spinning state
	private boolean spinning;
	private int timer;
	
	public SpinningWheelTile() {
		super(ModTiles.SPINNING_WHEEL_TILE.get());
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		threadInfo = NBTHelper.getCollection(new EvictingStack<FiberInfo>(2), FiberInfo::new, compound, TAG_THREAD_INFO);
		fiberInfo = NBTHelper.getOptional(FiberInfo::new, compound, TAG_FIBER_INFO);
		spinning = compound.getBoolean(TAG_SPINNING);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT result = super.write(compound);
		NBTHelper.putCollection(result, TAG_THREAD_INFO, threadInfo);
		NBTHelper.putOptional(result, TAG_FIBER_INFO, fiberInfo);
		result.putBoolean(TAG_SPINNING, spinning);
		return result;
	}

	private void setState(boolean spinning) {
		// Reset timer if transitioning to spinning state
		if (spinning) {
			timer = 0;
		}
		this.spinning = spinning;
	}
	
	/**
	 * Whether the thread is complete and ready to be picked up.
	 * @return
	 */
	public boolean isFinished() {
		if (threadInfo.isEmpty()) return false;
		return threadInfo.peek().amount >= REQUIRED_THREAD;
	}
	
	public Optional<ActionResultType> onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		ItemStack itemstack = player.getHeldItem(handIn);
		Item item = itemstack.getItem();
		if (!spinning) {
			if(isFinished()) {
				// If we are finished spinning thread, allow the player to put the thread on a spindle item.
				if (item == ModItems.SPINDLE_ITEM.get()) {
					if (!worldIn.isRemote) {
						// Consume one spindle
						itemstack.shrink(1);
						// Create new thread item and set color
						ItemStack threadItem = new ItemStack(ModItems.THREAD_ITEM.get());
						ModItems.THREAD_ITEM.get().setColor(threadItem, threadInfo.peek().color);
						// Add to player's inventory, drop if we can't
						if (!player.inventory.addItemStackToInventory(threadItem)) {
							player.dropItem(threadItem, false);
						}
						// Clear thread info
						threadInfo.clear();
					}
					// Play a fun pop sound
					world.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_ITEM_PICKUP,
							SoundCategory.BLOCKS, 1.0F, 1.0F);
					return Optional.of(ActionResultType.SUCCESS);
				}
			}
			else {
				// If there is some fiber present, spin it. Otherwise, try to add some more fiber.
				if (fiberInfo.isPresent()) {
					if (!worldIn.isRemote) {
						// If we already have some thread present, combine the new fiber into the thread.
						// Otherwise, simply set the thread to the new fiber
						if (threadInfo.isEmpty()) threadInfo.push(fiberInfo.get());
						else threadInfo.push(threadInfo.peek().combine(fiberInfo.get()));
						TextileMod.LOGGER.info("SpinningWheelTile onBlockActivated... threadInfo: {} fiberInfo: {}", threadInfo, fiberInfo);
						// Consume the fiber
						fiberInfo = Optional.empty();
						// Enter spinning state
						setState(true);
					}
					// Play a fun spinning sound
					// TODO Add spinning sound and change
					// TODO Also add animation
					world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO,
							SoundCategory.BLOCKS, 1.0F, 1.0F);
					return Optional.of(ActionResultType.SUCCESS);
				}
				else if (item instanceof IFiberItem) {
					if (!worldIn.isRemote) {
						// Split off one item
						ItemStack fiberItem = itemstack.split(1);
						// Get new fiber info
						fiberInfo = Optional.of(new FiberInfo(((IFiberItem) item).getInfo(fiberItem)));
					}
					// Play fun wool sound
					world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_WOOL_PLACE,
							SoundCategory.BLOCKS, 1.0F, 1.0F);
					return Optional.of(ActionResultType.SUCCESS);
				}
			}
		}
		// Default return
		return Optional.empty();
	}

	@Override
	public void tick() {
		if (spinning) {
			// If we are spinning increment timer
			timer++;
			// If we've reached the end of the timer, stop spinning.
			if (timer == SPINNING_TIME) setState(false);
		}
	}

}
