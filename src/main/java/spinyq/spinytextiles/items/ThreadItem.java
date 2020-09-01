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
import spinyq.spinytextiles.utility.color.ColorWord;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;

public class ThreadItem extends Item implements IDyeableItem {

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

	private StorageHandler storageHandler = new StorageHandler();

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

	@Override
	public int getDyeCost() {
		return 1;
	}

	public void setColor(ItemStack stack, RYBKColor color) {
		storageHandler.withColor(stack, color);
	}
	
	@Override
	public void dye(ItemStack stack, IInventory inventory, RYBKColor color) {
		// Only dye one item at a time
		ItemStack dyedStack = stack.split(1);
		setColor(dyedStack, color);
		if (inventory instanceof PlayerInventory) {
			((PlayerInventory) inventory).addItemStackToInventory(dyedStack);
		} else if (inventory instanceof Inventory) {
			((Inventory) inventory).addItem(dyedStack);
		}
	}

	@Override
	public RYBKColor getColor(ItemStack stack) {
		return storageHandler.getColor(stack);
	}

}
