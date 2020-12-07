package spinyq.spinytextiles.items;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import spinyq.spinytextiles.client.render.ItemColorHelper;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.NBTHelper;
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
	public class StorageHandler {

		private static final String TAG_COLOR = "Color", TRANSLATION_KEY_TAG = "TranslationKey", TAG_DIRTY = "Dirty";

		/**
		 * Writes a color to an itemstack
		 * 
		 * @param stack The itemstack
		 */
		public void setColor(ItemStack stack, RYBKColor color) {
			// If the new color is different than the itemstacks old color,
			// mark the translatio key as being dirty is it is recalculated.
			// getColor(...) may return null, so pass it as the argument
			if (!color.equals(getColor(stack)))
				markTranslationKeyDirty(stack);
			NBTHelper.put(stack.getOrCreateTag(), TAG_COLOR, color);
		}

		/**
		 * @param stack
		 * @return The color of the thread itemstack, or null if no color is attached.
		 */
		public RYBKColor getColor(ItemStack stack) {
			return NBTHelper.getOrNull(RYBKColor::new, stack.getOrCreateTag(), TAG_COLOR);
		}

		/**
		 * Retrieves the translation key of a given thread item stack. Since looking up
		 * the closest color word is a somewhat complex operation, we cache the
		 * translation key in the NBT data of the itemstack. We also store the color
		 * that was used to calculate the translation key, so that we know if the
		 * translation key is outdated.
		 * 
		 * @param stack The itemstack.
		 * @return The translation key.
		 */
		// TODO Improvement: We could abstract this using notions of a "Value,"
		// "Attribute,"
		// and a
		// "CalculatedValue." This doesn't feel necessary right now but if we have
		// items with
		// larger calculated attributes it might be worthwhile.
		// TODO Improvement: We could also cache an RGBColor for rendering.
		public String getTranslationKey(ItemStack stack) {
			// Check to see if our current translation key is out-of-date or not
			// If it is current, simply return it
			// If not, we have to calculate and store it so we can use it later
			String result;
			if (isTranslationKeyDirty(stack)) {
				RYBKColor color = getColor(stack);
				result = calculateTranslationKey(color);
				// Remove the dirty flag and cache the translation key
				stack.getOrCreateTag().putBoolean(TAG_DIRTY, false);
				stack.getOrCreateTag().putString(TRANSLATION_KEY_TAG, result);
			} else {
				result = stack.getTag().getString(TRANSLATION_KEY_TAG);
			}
			return result;
		}

		private boolean isTranslationKeyDirty(ItemStack stack) {
			// Check dirty flag
			return stack.getOrCreateTag().getBoolean(TAG_DIRTY);
		}

		private void markTranslationKeyDirty(ItemStack stack) {
			stack.getOrCreateTag().putBoolean(TAG_DIRTY, true);
		}

		private String calculateTranslationKey(RYBKColor color) {
			// Retrieve the closest color word to the given color and stitch together a
			// translation key
			String colorName = ColorWord.getClosest(color).getName();
			return ThreadItem.super.getTranslationKey() + '.' + colorName;
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

	@Override
	public String getTranslationKey(ItemStack stack) {
		return storageHandler.getTranslationKey(stack);
	}

	public void setColor(ItemStack stack, RYBKColor color) {
		storageHandler.setColor(stack, color);
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
		if (oldColor.equals(newColor))
			return false;
		// "Pay" for dye
		// If the dye provider has enough dye, proceed to dye the object
		if (provider.drain(dyeCost)) {
			// Only dye one item at a time, so split off a stack
			ItemStack dyedStack = stack.getStack().split(1);
			setColor(dyedStack, newColor);
			// Add dyed stack to inventory
			stack.getInventory().addItemStackToInventory(dyedStack);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean bleach(ContainedItemStack<PlayerInventory> stack, IBleachProvider provider) {
		// Subtract from each component of current color to get new color
		RYBKColor oldColor = getColor(stack.getStack());
		RYBKColor newColor = oldColor.minus(new RYBKColor(provider.getBleachLevel())).clamp();
		// If the new color didn't change, don't bleach the object
		if (oldColor.equals(newColor))
			return false;
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
		} else {
			return false;
		}
	}

}
