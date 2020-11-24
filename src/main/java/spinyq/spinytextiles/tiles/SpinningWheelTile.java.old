package spinyq.spinytextiles.tiles;

import java.util.Optional;
import java.util.Stack;
import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.common.util.Constants;
import spinyq.spinytextiles.ModItems;
import spinyq.spinytextiles.ModSounds;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.TextileMod;
import spinyq.spinytextiles.blocks.SpinningWheelBlock;
import spinyq.spinytextiles.items.IFiberItem;
import spinyq.spinytextiles.utility.EvictingStack;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.FiberInfo;

public class SpinningWheelTile extends TileEntity implements ITickableTileEntity {

	private static final String TAG_THREAD_INFO = "ThreadInfo", TAG_FIBER_INFO = "FiberInfo", TAG_SPINNING = "Spinning";
	
	public static final int SPINNING_TIME = 60, REQUIRED_THREAD = 4;
	
	// A queue containing info about the thread being spun.
	// Need to keep multiple references so that we can animate.
	private Stack<FiberInfo> threadInfo;
	private Supplier<Stack<FiberInfo>> threadInfoFactory = () -> new EvictingStack<>(2);
	private Optional<FiberInfo> fiberInfo = Optional.empty();
	// Used for the spinning state
	private boolean spinning;
	private int spinningTimer;
	
	public SpinningWheelTile() {
		super(ModTiles.SPINNING_WHEEL_TILE.get());
		resetThread();
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		threadInfo = NBTHelper.getCollection(threadInfoFactory, FiberInfo::new, compound, TAG_THREAD_INFO);
		fiberInfo = NBTHelper.getOptional(FiberInfo::new, compound, TAG_FIBER_INFO);
		setState(compound.getBoolean(TAG_SPINNING));
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT result = super.write(compound);
		NBTHelper.putCollection(result, TAG_THREAD_INFO, threadInfo);
		NBTHelper.putOptional(result, TAG_FIBER_INFO, fiberInfo);
		result.putBoolean(TAG_SPINNING, spinning);
		return result;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbtTag = new CompoundNBT();
		// Write your data into the nbtTag
		write(nbtTag);
		return new SUpdateTileEntityPacket(getPos(), -1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		CompoundNBT tag = pkt.getNbtCompound();
		// Handle your Data
		read(tag);
		TextileMod.LOGGER.trace("SpinningWheelTile onDataPacket... tag: {}", tag);
	}

	/*
	 * Creates a tag containing all of the TileEntity information, used by vanilla
	 * to transmit from server to client
	 */
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbtTagCompound = new CompoundNBT();
		write(nbtTagCompound);
		return nbtTagCompound;
	}

	/*
	 * Populates this TileEntity with information from the tag, used by vanilla to
	 * transmit from server to client
	 */
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		read(tag);
	}

	/**
	 * Syncs data with clients and marks this tile to be saved.
	 */
	private void update() {
		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);
		markDirty();
	}

	private void setState(boolean spinning) {
		// Reset timer if transitioning to spinning state
		if (spinning) {
			spinningTimer = 0;
		}
		// Also set blockstate so we can select models and such
		TextileMod.LOGGER.trace("SpinningWheelTile setState... spinning: {}", spinning);
		this.world.setBlockState(getPos(), this.getBlockState().with(SpinningWheelBlock.SPINNING, Boolean.valueOf(spinning)));
		this.spinning = spinning;
	}
	
	private void resetThread() {
		// Create new stack
		threadInfo = threadInfoFactory.get();
		// Add a dummy initial thread info
		threadInfo.add(new FiberInfo(new RYBKColor(), 0));
	}
	
	public Stack<FiberInfo> getThreadInfo() {
		return threadInfo;
	}

	public boolean hasThread() {
		return threadInfo.peek().amount > 0;
	}
	
	/**
	 * Whether the thread is complete and ready to be picked up.
	 * @return
	 */
	public boolean isFinished() {
		return threadInfo.peek().amount >= REQUIRED_THREAD;
	}
	
	public boolean isSpinning() {
		return spinning;
	}

	public int getSpinningTimer() {
		return spinningTimer;
	}

	public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		ItemStack itemstack = player.getHeldItem(handIn);
		Item item = itemstack.getItem();
		if (!spinning) {
			if(isFinished()) {
				// If we are finished spinning thread, allow the player to put the thread on a spindle item.
				if (item == ModItems.SPINDLE_ITEM.get()) {
					if (!world.isRemote) {
						// Consume one spindle
						itemstack.shrink(1);
						// Create new thread item and set color
						ItemStack threadItem = new ItemStack(ModItems.THREAD_ITEM.get());
						ModItems.THREAD_ITEM.get().setColor(threadItem, threadInfo.peek().color);
						// Add to player's inventory, drop if we can't
						if (!player.inventory.addItemStackToInventory(threadItem)) {
							player.dropItem(threadItem, false);
						}
						resetThread();
						update();
					}
					// Play a fun pop sound
					world.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_ITEM_PICKUP,
							SoundCategory.BLOCKS, 1.0F, 1.0F);
					return ActionResultType.SUCCESS;
				}
			}
			else {
				// If there is some fiber present, spin it. Otherwise, try to add some more fiber.
				if (fiberInfo.isPresent()) {
					if (!world.isRemote) {
						// If we already have some thread present, combine the new fiber into the thread.
						// Otherwise, simply set the thread to the new fiber
						threadInfo.push(threadInfo.peek().combine(fiberInfo.get()));
						TextileMod.LOGGER.info("SpinningWheelTile onBlockActivated... threadInfo: {} fiberInfo: {}", threadInfo, fiberInfo);
						// Consume the fiber
						fiberInfo = Optional.empty();
						// Enter spinning state
						setState(true);
						update();
					}
					// Play a fun spinning sound
					world.playSound((PlayerEntity) null, pos, ModSounds.BLOCK_SPINNING_WHEEL_SPIN.get(),
							SoundCategory.BLOCKS, 1.0F, 1.0F);
					return ActionResultType.SUCCESS;
				}
				else if (item instanceof IFiberItem) {
					if (!world.isRemote) {
						// Split off one item
						ItemStack fiberItem = itemstack.split(1);
						// Get new fiber info
						fiberInfo = Optional.of(((IFiberItem) item).getInfo(fiberItem).copy());
						update();
					}
					// Play fun wool sound
					world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_WOOL_PLACE,
							SoundCategory.BLOCKS, 1.0F, 1.0F);
					return ActionResultType.SUCCESS;
				}
			}
		}
		// Default return
		return ActionResultType.PASS;
	}

	@Override
	public void tick() {
		if (spinning) {
			// If we are spinning increment timer
			spinningTimer++;
			// If we've reached the end of the timer, stop spinning.
			if (spinningTimer == SPINNING_TIME) {
				setState(false);
				update();
			}
		}
	}

}
