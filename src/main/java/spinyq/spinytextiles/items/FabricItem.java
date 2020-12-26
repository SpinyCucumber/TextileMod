package spinyq.spinytextiles.items;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.Fabric;
import spinyq.spinytextiles.utility.textile.FabricPattern;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

// TODO Improvement: add method for retrieving pattern
// so we don't have to construct entire fabric info each time
public class FabricItem extends Item implements IDyeableItem, IBleachableItem {

	private static final String FABRIC_TAG = "Fabric";
	private static final IForgeRegistry<FabricPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(FabricPattern.class);
	// White and dark blue
	private static final ImmutableList<RYBKColor> DEFAULT_COLORS = ImmutableList.of(new RYBKColor(0f, 0f, 0f, 0f),
			new RYBKColor(0f, 0f, 1f, 0.5f));
	
	// TODO Add more information to tooltip
	
	public Fabric getFabric(ItemStack stack) {
		return NBTHelper.getOrNull(Fabric::new, stack.getOrCreateTag(), FABRIC_TAG);
	}
	
	public void setFabric(ItemStack stack, Fabric info) {
		NBTHelper.put(stack.getOrCreateTag(), FABRIC_TAG, info);
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		// Construct some additional arugments to pass to the text component
		// These are optionally used by the localization files to format stuff
		// We pass the fabric pattern description but only if we have fabric info
		Fabric fabric = getFabric(stack);
		if (fabric != null) {
			FabricPattern pattern = fabric.getPattern();
			return new TranslationTextComponent(getTranslationKey(stack),
					new TranslationTextComponent(pattern.getDescriptionTranslationKey()));
		}
		return super.getDisplayName(stack);
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
				Fabric fabric = getFabric(stack);
				// Only return a color if the fabric info is non-null
				// If it is null, return -1 (white)
				if (fabric != null) {
					// Look up the layer name using the tint index
					String layer = fabric.getPattern().getLayers().get(tintIndex);
					// Use the layer name to get the color
					return fabric.getColor(layer).toRGB(new RGBColor(), null).toInt();
				}
				return -1;
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
