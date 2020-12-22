package spinyq.spinytextiles.items;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.ColorWord;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.Fabric;
import spinyq.spinytextiles.utility.textile.FabricPattern;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

public class FabricItem extends Item implements IDyeableItem, IBleachableItem {

	private static final IForgeRegistry<FabricPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(FabricPattern.class);
	private static final String FABRIC_TAG = "Fabric";
	private static final ImmutableList<RYBKColor> DEFAULT_COLORS = ImmutableList.of(ColorWord.WHITE.color, ColorWord.BLACK.color);
	
	public Fabric getFabric(ItemStack stack) {
		return NBTHelper.getOrNull(Fabric::new, stack.getOrCreateTag(), FABRIC_TAG);
	}
	
	public void setFabric(ItemStack stack, Fabric info) {
		NBTHelper.put(stack.getOrCreateTag(), FABRIC_TAG, info);
	}
	
	public FabricItem(Properties properties) {
		super(properties);
		// Make sure we can receive events so we can register our color handler
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SubscribeEvent
	public void onItemColorHandler(ColorHandlerEvent.Item event) {
		// Register a color handler for all fabric items.
		// The color handler makes it so each layer of the item is rendered with the right color.
		event.getItemColors().register((stack, tintIndex) -> {
				// Look up the layer name using the tint index
				Fabric fabric = getFabric(stack);
				String layer = fabric.getPattern().getLayers().get(tintIndex);
				// Use the layer name to get the color
				return fabric.getColor(layer).toRGB(new RGBColor(), null).toInt();
			}, this);
	}
	
	@Override
	public boolean dye(ContainedItemStack<PlayerInventory> object, IDyeProvider provider) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean bleach(ContainedItemStack<PlayerInventory> object, IBleachProvider provider) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (FabricPattern pattern : PATTERN_REGISTRY.getValues()) {
				items.add(createDefaultFabricItem(pattern));
			}
		}
	}
	
	private ItemStack createDefaultFabricItem(FabricPattern pattern) {
		// Create new fabric that uses the pattern
		Fabric fabric = new Fabric(pattern);
		// For every layer in the pattern, make the fabric use a default color
		// If there are more layers than colors cycle through the colors
		int i = 0;
		for (String layer : pattern.getLayers()) {
			RYBKColor color = DEFAULT_COLORS.get(i % DEFAULT_COLORS.size()).copy();
			fabric.setColor(layer, color);
			i++;
		}
		// Construct a new itemstack and set the fabric
		ItemStack item = new ItemStack(this);
		setFabric(item, fabric);
		return item;
	}
	
}
