package spinyq.spiny_textiles.items;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import spinyq.spiny_textiles.util.Color;
import spinyq.spiny_textiles.util.ColorWord;

public class ThreadItem extends Item {
	
	public ThreadItem(Properties properties) {
		super(properties);
	}

	/**
	 * A default, valid thread item.
	 */
	@Override
	public ItemStack getDefaultInstance() {
		return getStorageHandler().withColor(super.getDefaultInstance(), ColorWord.WHITE.getColor());
	}
	
	@Override
	public String getTranslationKey(ItemStack stack) {
		// If the stack has a color (it should), get the closest "word" color and use that
		String colorWord = "null";
		Color color = storageHandler.getColor(stack);
		if (color != null)
			colorWord = ColorWord.getClosest(color).getName();
		return "item.thread." + colorWord;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (ColorWord colorWord : ColorWord.values()) {
				// Create itemstack and add it
				ItemStack stack = getStorageHandler().withColor(new ItemStack(this), colorWord.getColor());
				items.add(stack);
			}
		}
	}

	@Override
	public Item getItem() {
		return this;
	}

	// TODO Register Model, Color Handler
	
	public StorageHandler getStorageHandler() {
		return storageHandler;
	}
	
	private StorageHandler storageHandler = new StorageHandler();
	
	private IItemColor colorHandler = new IItemColor() {

		@Override
		public int getColor(ItemStack stack, int tintIndex) {
			// For the overlay layer, return the color of the thread
			Color color = getStorageHandler().getColor(stack);
			if (tintIndex == 1 && color != null) return color.toInt();
			// For all other layers, return -1 (white)
			return -1;
		}
		
	};
	
	/**
	 * Class that handles reading/writing color data from stacks.
	 * @author SpinyQ
	 *
	 */
	public static class StorageHandler {
		
		private static final String KEY_COLOR = "color";
		
		/**
		 * Writes a color to an itemstack and returns the stack for chaining
		 * @param stack
		 * @return
		 */
		public ItemStack withColor(ItemStack stack, Color color) {
			// Create tag if it does not exist
			if (!stack.hasTag()) stack.setTag(new CompoundNBT());
			// Set color
			stack.getTag().putInt(KEY_COLOR, color.toInt());
			return stack;
		}
		
		/**
		 * @param stack
		 * @return The color of the thread itemstack, or null if no color is attached. (This should not happen.)
		 */
		public Color getColor(ItemStack stack) {
			if (!stack.hasTag()) return null;
			if (!stack.getTag().contains(KEY_COLOR)) return null;
			return Color.fromInt(stack.getTag().getInt(KEY_COLOR));
		}
		
	}

}
