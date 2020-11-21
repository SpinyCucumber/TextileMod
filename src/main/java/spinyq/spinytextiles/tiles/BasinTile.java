package spinyq.spinytextiles.tiles;

import java.util.Stack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import spinyq.spinytextiles.ModTiles;
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
		
		protected BasinTile basin;
		protected State superState, subState;
		
		public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// By default do nothing
			return ActionResultType.PASS;
		}
		
		public static class EmptyState extends State {

			@Override
			public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
				// Get itemstack and item
				ItemStack itemstack = player.getHeldItem(handIn);
				Item item = itemstack.getItem();
				// If player is holding a water bucket, empty the bucket and fill the cauldron.
				if (item == Items.WATER_BUCKET) {

					if (!basin.world.isRemote) {
						if (!player.abilities.isCreativeMode) {
							player.setHeldItem(handIn, new ItemStack(Items.BUCKET));
						}
						basin.swapState(new FilledState());
						basin.world.playSound((PlayerEntity) null, basin.pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
								1.0F, 1.0F);
					}

					return ActionResultType.SUCCESS;

				}
				return ActionResultType.PASS;
			}
			
		}
		
		public static class FilledState extends State {

			// Water level starts out at maximum
			private int waterLevel = MAX_WATER_LEVEL;
			
			public boolean drain(int amount) {
				// Fail if amount is greater than water level
				if (amount > waterLevel) return false;
				// Subtract amount from water level
				// If water level reaches zero, transition to empty state
				waterLevel -= amount;
				if (waterLevel == 0) basin.swapState(new EmptyState());
				return true;
			}

			@Override
			public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
				// TODO
				return null;
			}
			
		}
		
		public static abstract class SaturatedState extends State {

			public abstract boolean consumeItem(ItemStack itemStack);
			
		}
		
		public static class DyeState extends SaturatedState implements IDyeProvider {

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
			public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean consumeItem(ItemStack itemStack) {
				// TODO Auto-generated method stub
				return false;
			}
			
		}
		
		public static class BleachState extends SaturatedState implements IBleachProvider {

			@Override
			public float getBleachLevel() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean drain(int amount) {
				return ((FilledState) superState).drain(amount);
			}

			@Override
			public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean consumeItem(ItemStack itemStack) {
				// TODO Auto-generated method stub
				return false;
			}
			
		}
		
	}
	
	private Stack<State> stack = new Stack<>();
	
	public void pushState(State state) {
		// Give the state a reference to the basin so they can change state and such
		state.basin = this;
		// If there is already a state in the stack, hook up substate/superstate references
		if (!stack.empty()) {
			State superState = stack.peek();
			superState.subState = state;
			state.superState = superState;
		}
		stack.add(state);
	}
	
	public void popState() {
		stack.pop();
	}
	
	public void swapState(State newState) {
		popState();
		pushState(newState);
	}
	
	public State getState() {
		return stack.peek();
	}
	
	public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		return getState().onInteract(player, handIn, hit);
	}
	
	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
		// Push some initial state
		pushState(new EmptyState());
	}

}
