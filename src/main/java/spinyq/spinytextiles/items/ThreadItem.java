package spinyq.spinytextiles.items;

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
import spinyq.spinytextiles.utility.Color3f;
import spinyq.spinytextiles.utility.ColorWord;

public class ThreadItem extends Item implements IDyeableItem {

	public ThreadItem(Properties properties) {
		super(properties);
		// Set our color handler
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			ItemColorHelper.setItemColorHandler(this, (stack, tintIndex) -> {
				// For the overlay layer, return the color of the thread
				Color3f color = storageHandler.getColor(stack);
				if (tintIndex == 1 && color != null)
					return color.toInt();
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
		return storageHandler.withColor(super.getDefaultInstance(), ColorWord.WHITE.getColor());
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		// If the stack has a color (it should), get the closest "word" color and use
		// that
		String colorName = "null";
		Color3f color = storageHandler.getColor(stack);
		if (color != null)
			colorName = ColorWord.getClosest(color).getName();
		return super.getTranslationKey() + '.' + colorName;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (ColorWord colorWord : ColorWord.values()) {
				// Create itemstack and add it
				ItemStack stack = storageHandler.withColor(new ItemStack(this), colorWord.getColor());
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
		public ItemStack withColor(ItemStack stack, Color3f color) {
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
		public Color3f getColor(ItemStack stack) {
			if (!stack.hasTag())
				return null;
			if (!stack.getTag().contains(KEY_COLOR))
				return null;
			return Color3f.fromInt(stack.getTag().getInt(KEY_COLOR));
		}

	}

	@Override
	public int getDyeCost() {
		return 1;
	}

	@Override
	public void setColor(ItemStack stack, IInventory inventory, Color3f color) {
		// Only dye one item at a time
		ItemStack dyedStack = stack.split(1);
		storageHandler.withColor(dyedStack, color);
		if (inventory instanceof PlayerInventory) {
			((PlayerInventory) inventory).addItemStackToInventory(dyedStack);
		} else if (inventory instanceof Inventory) {
			((Inventory) inventory).addItem(dyedStack);
		}
	}

	@Override
	public Color3f getColor(ItemStack stack) {
		return storageHandler.getColor(stack);
	}

}
