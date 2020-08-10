package spinyq.immersivetextiles.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import spinyq.immersivetextiles.TextileMod;
import spinyq.immersivetextiles.util.Color;
import spinyq.immersivetextiles.util.ColorWord;

public class ItemThread extends Item {
	
	public ItemThread() {
		this.setRegistryName(new ResourceLocation(TextileMod.MODID, "thread"));
	}
	
	/**
	 * A default, valid thread item.
	 */
	@Override
	public ItemStack getDefaultInstance() {
		return getStorageHandler().withColor(super.getDefaultInstance(), ColorWord.WHITE.getColor());
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		// If the stack has a color (it should), get the closest "word" color and use that
		String colorWord = "null";
		Color color = storageHandler.getColor(stack);
		if (color != null)
			colorWord = ColorWord.getClosest(color).getName();
		return "item.thread." + colorWord;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		// Create a subitem for every "word" color and add it.
		for (ColorWord colorWord : ColorWord.values()) {
			// Create itemstack and add it
			ItemStack stack = getStorageHandler().withColor(new ItemStack(this), colorWord.getColor());
			items.add(stack);
		}
	}

	@Override
	public Item getItem() {
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		// Register our custom mesh definition.
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(getItem(), new ItemMeshDefinition() {

			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return new ModelResourceLocation(new ResourceLocation(ImmersiveTextiles.TextileMod.MODID, "thread"), "inventory");
			}
			
		});
		// Put this here for now. Might have to move it.
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(colorHandler, getItem());
	}
	
	public StorageHandler getStorageHandler() {
		return storageHandler;
	}
	
	private StorageHandler storageHandler = new StorageHandler();
	
	private IItemColor colorHandler = new IItemColor() {

		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
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
		
		private String keyColor = "color";
		
		/**
		 * Writes a color to an itemstack and returns the stack for chaining
		 * @param stack
		 * @return
		 */
		public ItemStack withColor(ItemStack stack, Color color) {
			// Create tag if it does not exist
			if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
			// Set color
			stack.getTagCompound().setInteger(keyColor, color.toInt());
			return stack;
		}
		
		/**
		 * @param stack
		 * @return The color of the thread itemstack, or null if no color is attached. (This should not happen.)
		 */
		public Color getColor(ItemStack stack) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey(keyColor))
				return Color.fromInt(stack.getTagCompound().getInteger(keyColor));
			return null;
		}
		
	}

}
