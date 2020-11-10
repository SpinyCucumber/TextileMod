package spinyq.spinytextiles.items;

import java.util.Optional;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import spinyq.spinytextiles.client.render.ItemColorHelper;
import spinyq.spinytextiles.tiles.BasinTile;
import spinyq.spinytextiles.utility.color.ColorWord;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;

public class ThreadItem extends Item implements IDyeableItem {

	/**
	 * Class that handles reading/writing color data from stacks.
	 * 
	 * @author SpinyQ
	 *
	 */
	public static class StorageHandler {
	
		private static final String KEY_COLOR = "Color";
	
		/**
		 * Writes a color to an itemstack and returns the stack for chaining
		 * 
		 * @param stack
		 * @return
		 */
		public ItemStack withColor(ItemStack stack, RYBKColor color) {
			// Create tag if it does not exist
			if (!stack.hasTag())
				stack.setTag(new CompoundNBT());
			// Set color
			stack.getTag().putInt(KEY_COLOR, color.toInt());
			return stack;
		}
	
		/**
		 * @param stack
		 * @return The color of the thread itemstack, or null if no color is attached.
		 *         (This should not happen.)
		 */
		public RYBKColor getColor(ItemStack stack) {
			if (!stack.hasTag())
				return null;
			if (!stack.getTag().contains(KEY_COLOR))
				return null;
			return new RYBKColor().fromInt(stack.getTag().getInt(KEY_COLOR));
		}
	
	}

	private StorageHandler storageHandler = new StorageHandler();
	private int dyeCost = 1, bleachCost = 1;

	public ThreadItem(Properties properties) {
		super(properties);
		// Set our color handler
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			ItemColorHelper.setItemColorHandler(this, (stack, tintIndex) -> {
				// For the overlay layer, return the color of the thread
				RGBColor rgb = getColor(stack).toRGB(new RGBColor(), Optional.empty());
				if (tintIndex == 1)
					return rgb.toInt();
				// For all other layers, return -1 (white)
				return -1;
			});
		});
	}

	/**
	 * A default, valid thread item.
	 */
	@Override
	public ItemStack getDefaultInstance() {
		ItemStack result = super.getDefaultInstance();
		setColor(result, ColorWord.WHITE.color);
		return result;
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		// If the stack has a color (it should), get the closest "word" color and use
		// that
		String colorName = "null";
		RYBKColor color = getColor(stack);
		if (color != null)
			colorName = ColorWord.getClosest(color).getName();
		return super.getTranslationKey() + '.' + colorName;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (ColorWord colorWord : ColorWord.values()) {
				// Create itemstack and add it
				ItemStack stack = new ItemStack(this);
				setColor(stack, colorWord.color);
				items.add(stack);
			}
		}
	}

	public void setColor(ItemStack stack, RYBKColor color) {
		storageHandler.withColor(stack, color);
	}
	
	public RYBKColor getColor(ItemStack stack) {
		return storageHandler.getColor(stack);
	}

	@Override
	public void dye(ItemStack stack, IInventory inventory, BasinTile basin) {
		// Only dye one item at a time
		// Add colors to get new color
		ItemStack dyedStack = stack.split(1);
		RYBKColor newColor = basin.getColor().plus(getColor(dyedStack));
		newColor.clamp();
		setColor(dyedStack, newColor);
		// Add dyed stack to inventory
		if (inventory instanceof PlayerInventory) {
			((PlayerInventory) inventory).addItemStackToInventory(dyedStack);
		} else if (inventory instanceof Inventory) {
			((Inventory) inventory).addItem(dyedStack);
		}
		// Drain some water from basin
		basin.drain(dyeCost);
	}

	@Override
	public void bleach(ItemStack stack, IInventory inventory, BasinTile basin) {
		// Only bleach one item at time
		// Subtract from current color to get new color
		ItemStack bleachedStack = stack.split(1);
		RYBKColor newColor = getColor(bleachedStack).plus(new RYBKColor(-basin.getBleachLevel()));
		newColor.clamp();
		setColor(bleachedStack, newColor);
		// Add bleached stack to inventory
		if (inventory instanceof PlayerInventory) {
			((PlayerInventory) inventory).addItemStackToInventory(bleachedStack);
		} else if (inventory instanceof Inventory) {
			((Inventory) inventory).addItem(bleachedStack);
		}
		// Drain water
		basin.drain(bleachCost);
	}

	@Override
	public boolean canDye(ItemStack object, IInventory context, BasinTile basin) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canBleach(ItemStack stack, IInventory context, BasinTile basin) {
		// Check if basin has enough water
		if (basin.getWaterLevel() < bleachCost) return false;
		// If any of the colors components is above zero, we can still apply bleach
		// Retrieve color
		RYBKColor dye = getColor(stack);
		for (RYBKColor.Axis axis : RYBKColor.Axis.values()) {
			if (dye.project(axis.direction) > 0.0f) return true;
		}
		return false;
	}

}
