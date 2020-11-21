package spinyq.spinytextiles.tiles;

import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

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

	private static final int MAX_WATER_LEVEL = 8;
	
	public static abstract class State {
		
		protected BasinTile stack;
		
		public abstract ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
				Hand handIn, BlockRayTraceResult hit);
		
		public static abstract class SubState<T extends State> extends State {
			
			private T superState;

			public SubState(T superState) {
				this.superState = superState;
			}

			public T getSuperState() {
				return superState;
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
		
		public static class EmptyState extends SubState<BaseState> {

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
						stack.swapState(new FilledState(getSuperState()));
						world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
								1.0F, 1.0F);
					}

					return ActionResultType.SUCCESS;

				}
				// Fall back to superstate
				return getSuperState().onInteract(state, world, pos, player, handIn, hit);
			}
			
		}
		
		public static class FilledState extends SubState<BaseState> {

			// Water level starts out at maximum
			private int waterLevel = MAX_WATER_LEVEL;
			private Set<Function<FilledState, State>> subStateFactories = ImmutableSet.of(DyeState::new, BleachState::new);
			
			public FilledState(BaseState parent) {
				super(parent);
			}
			
			public boolean drain(int amount) {
				// Fail if amount is greater than water level
				if (amount > waterLevel) return false;
				// Subtract amount from water level
				// If water level reaches zero, transition to empty state
				waterLevel -= amount;
				if (waterLevel == 0) stack.swapState(new EmptyState(getSuperState()));
				return true;
			}

			@Override
			public ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
					Hand handIn, BlockRayTraceResult hit) {
				// TODO Will have to change this, doesn't make sense in all situations
				// Iterate through possible substates
				// If substate can handle the interaction, switch to it
				for (Function<FilledState, State> factory : subStateFactories) {
					// Create new substate
					State subState = factory.apply(this);
					// Let new substate handle action
					ActionResultType result = subState.onInteract(state, world, pos, player, handIn, hit);
					if (!result.equals(ActionResultType.PASS)) stack.pushState(subState);
				}
				// Fall back to superstate
				return getSuperState().onInteract(state, world, pos, player, handIn, hit);
			}
			
		}
		
		public static class DyeState extends SubState<FilledState> implements IDyeProvider {

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
				return getSuperState().drain(amount);
			}

			@Override
			public ActionResultType onInteract(BlockState state, World world, BlockPos pos, PlayerEntity player,
					Hand handIn, BlockRayTraceResult hit) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}
		
		public static class BleachState extends SubState<FilledState> implements IBleachProvider {

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
				return getSuperState().drain(amount);
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
		state.stack = this;
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
