package spinyq.spinytextiles.tiles;

import java.util.Stack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.tiles.BasinTile.State.BaseState;
import spinyq.spinytextiles.tiles.BasinTile.State.EmptyState;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

/**
 * Uses a stack-based FSM
 * @author Elijah Hilty
 *
 */
public class BasinTile extends TileEntity {

	public static abstract class State {
		
		protected BasinTile basin;
		
		public abstract ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
				Hand handIn, BlockRayTraceResult hit);
		
		public static abstract class ChildState<T extends State> extends State {
			
			private T parent;

			public ChildState(T parent) {
				this.parent = parent;
			}

			public T getParent() {
				return parent;
			}
			
		}
		
		public static class BaseState extends State {

			@Override
			public ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
					Hand handIn, BlockRayTraceResult hit) {
				// Don't do crap
				return ActionResultType.PASS;
			}
			
		}
		
		public static class EmptyState extends ChildState<BaseState> {

			public EmptyState(BaseState parent) {
				super(parent);
			}

			@Override
			public ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
					Hand handIn, BlockRayTraceResult hit) {
				// Get itemstack and item
				ItemStack itemstack = player.getHeldItem(handIn);
				Item item = itemstack.getItem();
				// If player is holding a water bucket, empty the bucket and fill the cauldron.
				if (item == Items.WATER_BUCKET) {

					if (!world.isRemote) {
						if (!player.abilities.isCreativeMode) {
							player.setHeldItem(handIn, new ItemStack(Items.BUCKET));
						}
						basin.swapState(new FilledState(getParent()));
						world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
								1.0F, 1.0F);
					}

					return ActionResultType.SUCCESS;

				}
				// Fall back to parent state
				return getParent().onInteract(state, world, pos, player, handIn, hit);
			}
			
		}
		
		public static class FilledState extends ChildState<BaseState> {

			public FilledState(BaseState parent) {
				super(parent);
			}
			
			public boolean drain(int amount) {
				// TODO
				return false;
			}

			@Override
			public ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
					Hand handIn, BlockRayTraceResult hit) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}
		
		public static class DyeState extends ChildState<FilledState> implements IDyeProvider {

			public DyeState(FilledState parent) {
				super(parent);
			}

			@Override
			public RYBKColor getColor() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean drain(int amount) {
				return getParent().drain(amount);
			}

			@Override
			public ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
					Hand handIn, BlockRayTraceResult hit) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}
		
		public static class BleachState extends ChildState<FilledState> implements IBleachProvider {

			public BleachState(FilledState parent) {
				super(parent);
			}

			@Override
			public float getBleachLevel() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean drain(int amount) {
				return getParent().drain(amount);
			}

			@Override
			public ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
					Hand handIn, BlockRayTraceResult hit) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}
		
	}
	
	private Stack<State> stateStack = new Stack<>();
	
	public void pushState(State state) {
		// Give the state a reference to the basin so they can change state and such
		state.basin = this;
		stateStack.add(state);
	}
	
	public void popState() {
		stateStack.pop();
	}
	
	public void swapState(State newState) {
		popState();
		pushState(newState);
	}
	
	public State getState() {
		return stateStack.peek();
	}
	
	public ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		return getState().onInteract(state, world, pos, player, handIn, hit);
	}
	
	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
		// Push some initial state
		BaseState base = new BaseState();
		EmptyState empty = new EmptyState(base);
		pushState(base);
		pushState(empty);
	}

}
