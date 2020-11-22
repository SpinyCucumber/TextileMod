package spinyq.spinytextiles.items;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import spinyq.spinytextiles.client.render.ItemColorHelper;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.color.ColorWord;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

public class ThreadItem extends Item implements IDyeableItem, IBleachableItem {

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
				RGBColor rgb = getColor(stack).toRGB(new RGBColor(), null);
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
	public boolean dye(ContainedItemStack<PlayerInventory> stack, IDyeProvider provider) {
		// Add dye color to existing color to get new color
		RYBKColor oldColor = getColor(stack.getStack());
		RYBKColor newColor = provider.getColor().plus(oldColor).clamp();
		// If the new color didn't change, don't dye the object
		if (oldColor.equals(newColor)) return false;
		// "Pay" for dye
		// If the dye provider has enough dye, proceed to dye the object
		if (provider.drain(dyeCost)) {
			// Only dye one item at a time, so split off a stack
			ItemStack dyedStack = stack.getStack().split(1);
			setColor(dyedStack, newColor);
			// Add dyed stack to inventory
			stack.getInventory().addItemStackToInventory(dyedStack);
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean bleach(ContainedItemStack<PlayerInventory> stack, IBleachProvider provider) {
		// Subtract from each component of current color to get new color
		RYBKColor oldColor = getColor(stack.getStack());
		RYBKColor newColor = oldColor.minus(new RYBKColor(provider.getBleachLevel())).clamp();
		// If the new color didn't change, don't bleach the object
		if (oldColor.equals(newColor)) return false;
		// "Pay" for bleach
		// If the bleach provider has enough bleach, proceed to bleach the object
		if (provider.drain(bleachCost)) {
			// Only bleach one item at a time, so split off a stack
			// Subtract from each component of current color to get new color
			ItemStack bleachedStack = stack.getStack().split(1);
			setColor(bleachedStack, newColor);
			// Add bleached stack to inventory
			stack.getInventory().addItemStackToInventory(bleachedStack);
			return true;
		}
		else {
			return false;
		}
	}

}
