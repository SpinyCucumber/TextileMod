package spinyq.spinytextiles.items;

import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import spinyq.spinytextiles.utility.Color3f;

public class FiberItem extends Item implements IFiberItem {

	private int fiberValue;
	private StorageHandler storageHandler = new StorageHandler();
	
	@SubscribeEvent
	public void onItemColorHandler(ColorHandlerEvent.Item event) {
		event.getItemColors().register((stack, tintIndex) -> storageHandler.getColor(stack).getColorValue(), this);
	}
	
	public FiberItem(Properties properties, int fiberValue) {
		super(properties);
		this.fiberValue = fiberValue;
		// Register ourselves to receive events so we can register the color handler.
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public int getFiberValue() {
		return fiberValue;
	}

	/**
	 * Class that handles reading/writing color data from stacks.
	 * @author SpinyQ
	 *
	 */
	public static class StorageHandler {
		
		private static final String KEY_COLOR = "Color";
		
		/**
		 * Writes a color to an itemstack and returns the stack for chaining
		 * @param stack
		 * @return
		 */
		public ItemStack withColor(ItemStack stack, DyeColor color) {
			// Create tag if it does not exist
			if (!stack.hasTag()) stack.setTag(new CompoundNBT());
			// Set color
			stack.getTag().putInt(KEY_COLOR, color.getId());
			return stack;
		}
		
		/**
		 * @param stack
		 * @return The color of the fiber itemstack, or null if no color is attached. (This should not happen.)
		 */
		public DyeColor getColor(ItemStack stack) {
			if (!stack.hasTag()) return null;
			if (!stack.getTag().contains(KEY_COLOR)) return null;
			return DyeColor.byId(stack.getTag().getInt(KEY_COLOR));
		}
		
	}

	@Override
	public Color3f getColor(ItemStack stack) {
		return Color3f.fromDye(storageHandler.getColor(stack));
	}
	
	@Override
	public ItemStack getDefaultInstance() {
		return storageHandler.withColor(super.getDefaultInstance(), DyeColor.WHITE);
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (DyeColor color : DyeColor.values()) {
				// Create itemstack and add it
				ItemStack stack = storageHandler.withColor(new ItemStack(this), color);
				items.add(stack);
			}
		}
	}
	
	@Override
	public String getTranslationKey(ItemStack stack) {
		String colorName = "null";
		DyeColor color = storageHandler.getColor(stack);
		if (color != null)
			colorName = color.getTranslationKey();
		return super.getTranslationKey() + '.' + colorName;
	}
	
}
