package spinyq.spinytextiles.items;

import com.google.common.base.Objects;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.NBTHelper.CalculatedValue;
import spinyq.spinytextiles.utility.color.ColorWord;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

public class ThreadItem extends Item implements IDyeableItem, IBleachableItem {

	private static final String COLOR_TAG = "Color";
	private static final String TRANSLATION_KEY_TAG = "TranslationKey";
	
	private CalculatedValue<String> translationKey = NBTHelper.createCalculatedStringValue(
			TRANSLATION_KEY_TAG,
			(item) -> {
				// Retrieve the closest color word to the given color and stitch together a
				// translation key
				String colorName = ColorWord.getClosest(getColor(item)).getName();
				return ThreadItem.super.getTranslationKey() + '.' + colorName;
			});
	
	private int dyeCost = 1, bleachCost = 1;

	public ThreadItem(Properties properties) {
		super(properties);
		// Make sure we can receive events so we can register our color handler
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	
	@SubscribeEvent
	public void onItemColorHandler(ColorHandlerEvent.Item event) {
		// Register a color handler for all thread items.
		// This ensures that the thread layer of each item is rendered with the actual color
		// of the thread.
		event.getItemColors().register((stack, tintIndex) -> {
				// For the thread layer, return the color of the thread
				if (tintIndex == 1)
					return getColor(stack).toRGB(new RGBColor(), null).toInt();
				// For all other layers, return -1 (white)
				return -1;
			}, this);
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
	public String getTranslationKey(ItemStack item) {
		return translationKey.get(item);
	}

	public void setColor(ItemStack item, RYBKColor color) {
		// If new color is different from old color, mark translation key as dirty
		if (!Objects.equal(color, getColor(item))) translationKey.markDirty(item);
		NBTHelper.put(item.getOrCreateTag(), COLOR_TAG, color);
	}

	public RYBKColor getColor(ItemStack item) {
		return NBTHelper.getOrNull(RYBKColor::new, item.getOrCreateTag(), COLOR_TAG);
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
