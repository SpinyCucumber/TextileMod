package spinyq.spinytextiles.tiles;

import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import spinyq.spinytextiles.tiles.BasinTile.State.EmptyState;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

/**
 * Uses a stack-based FSM
 * 
 * @author Elijah Hilty
 *
 */
public class BasinTile extends TileEntity {

	private static final int MAX_WATER_LEVEL = 8;
	private static final float LYE_VALUE = 0.25f;

	public static abstract class State {

		protected BasinTile basin;
		protected State superState, subState;

		public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			// By default do nothing
			return ActionResultType.PASS;
		}

		public State getSuperState() {
			return superState;
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
						basin.swapState(this, new FilledState());
						basin.world.playSound((PlayerEntity) null, basin.pos, SoundEvents.ITEM_BUCKET_EMPTY,
								SoundCategory.BLOCKS, 1.0F, 1.0F);
					}

					return ActionResultType.SUCCESS;

				}
				return ActionResultType.PASS;
			}

		}

		public static class FilledState extends State {

			private Set<Supplier<SaturatedState>> substateSuppliers = ImmutableSet.of(DyeState::new, BleachState::new);

			// Water level starts out at maximum
			private int waterLevel = MAX_WATER_LEVEL;

			public double getWaterHeight() {
				return 0.2 + ((double) waterLevel / (double) MAX_WATER_LEVEL) * 0.875;
			}
			
			public boolean drain(int amount) {
				// Fail if amount is greater than water level
				if (amount > waterLevel)
					return false;
				// Subtract amount from water level
				// If water level reaches zero, transition to empty state
				waterLevel -= amount;
				if (waterLevel == 0)
					basin.swapState(this, new EmptyState());
				return true;
			}

			@Override
			public ActionResultType onInteract(PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
				// Check if a substate can handle the action.
				// If they can, push the new state
				for (Supplier<SaturatedState> supplier : substateSuppliers) {
					SaturatedState subState = supplier.get();
					if (subState.consumeInteraction(player, handIn, hit)) {
						basin.pushState(subState);
						return ActionResultType.SUCCESS;
					}
				}
				return ActionResultType.PASS;
			}

		}

		public static abstract class SaturatedState extends State {

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

		public static class DyeState extends SaturatedState implements IDyeProvider {

			private RYBKColor color = new RYBKColor();

			@Override
			public RYBKColor getColor() {
				return color;
			}

			@Override
			public boolean drain(int amount) {
				return ((FilledState) getSuperState()).drain(amount);
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

				if (!basin.world.isRemote) {
					// Consume one item if player is not in creative
					if (!player.abilities.isCreativeMode) {
						itemStack.shrink(1);
					}
					// Change the color
					color = newColor;
					basin.world.playSound((PlayerEntity) null, basin.pos, SoundEvents.ITEM_BUCKET_EMPTY,
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
					ContainedItemStack<PlayerInventory> containedStack = new ContainedItemStack<>(itemStack, player.inventory);
					if (dyeable.dye(containedStack, this)) return ActionResultType.SUCCESS;
				}
				// TODO Glowstone saturation modifier
				return ActionResultType.PASS;
			}

		}

		public static class BleachState extends SaturatedState implements IBleachProvider {

			private float bleachLevel = 0.0f;
			
			@Override
			public float getBleachLevel() {
				return bleachLevel;
			}

			@Override
			public boolean drain(int amount) {
				return ((FilledState) getSuperState()).drain(amount);
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
				if (newBleachLevel== bleachLevel)
					return false;

				if (!basin.world.isRemote) {
					// Consume one item if player is not in creative
					if (!player.abilities.isCreativeMode) {
						itemStack.shrink(1);
					}
					// Change the bleach level
					bleachLevel = newBleachLevel;
					basin.world.playSound((PlayerEntity) null, basin.pos, SoundEvents.ITEM_BUCKET_EMPTY,
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
					ContainedItemStack<PlayerInventory> containedStack = new ContainedItemStack<>(itemStack, player.inventory);
					if (bleachable.bleach(containedStack, this)) return ActionResultType.SUCCESS;
				}
				return ActionResultType.PASS;
			}

		}

	}

	private Stack<State> stack = new Stack<>();

	public void pushState(State state) {
		// Give the state a reference to the basin so they can change state and such
		state.basin = this;
		// If there is already a state in the stack, hook up substate/superstate
		// references
		if (!stack.empty()) {
			State superState = stack.peek();
			superState.subState = state;
			state.superState = superState;
		}
		stack.add(state);
	}

	public void popState(State state) {
		State popped = null;
		while (!popped.equals(state)) popped = stack.pop();
	}

	public void swapState(State oldState, State newState) {
		popState(oldState);
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
