package spinyq.spinytextiles.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.NBTHelper.CalculatedValue;
import spinyq.spinytextiles.utility.color.ColorWord;
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
	
	private Map<String, CalculatedValue<ColorWord>> closestColorWordMap = new HashMap<>();
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
		// Make sure to mark color words as dirty if applicable
		return false;
	}

	@Override
	public boolean bleach(ContainedItemStack<PlayerInventory> object, IBleachProvider provider) {
		// TODO Auto-generated method stub
		// Make sure to mark color words as dirty if applicable
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
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		// Only add more info if we have fabric data attached
		Fabric fabric = getFabric(stack);
		if (fabric != null) {
			// Iterate over each layer of the fabric's pattern
			// For each layer, get the closest color word
			// Use the closest color word to create a translation text component
			FabricPattern pattern = fabric.getPattern();
			String colorInfoTranslationKey = getTranslationKey() + ".color_info";
			for (String layer : pattern.getLayers()) {
				ColorWord closestColorWord = getClosestColorWord(stack, layer);
				// Construct tooltip line
				// We pass the layer's name and the color's name as parameters
				tooltip.add(new TranslationTextComponent(colorInfoTranslationKey,
						new TranslationTextComponent(pattern.getLayerTranslationKey(layer)),
						new TranslationTextComponent(closestColorWord.getTranslationKey()))
						.applyTextStyles(TextFormatting.GRAY));
			}
		}
	}

	// Retrieves the color word closest to the color of a given layer.
	// We use CalculatedValue because it automatically caches the value for us,
	// so we don't have to recompute it each time, as determining the
	// closest color is a slightly expensive operation.
	private ColorWord getClosestColorWord(ItemStack stack, String layer) {
		// Try to look up our calculated value.
		// If it doesn't exist, we have to create it.
		// We also store it in the cache after creating it.
		CalculatedValue<ColorWord> value = closestColorWordMap.get(layer);
		if (value == null) {
			String tag = StringUtils.capitalize(layer) + "ColorWord";
			value = NBTHelper.createCalculatedEnumValue(
					tag, ColorWord.class,
					(item) -> {
						// Get color of layer, then get closest color word to color
						Fabric fabric = getFabric(item);
						return ColorWord.getClosest(fabric.getColor(layer));
					});
			// Store the calculated value in our map
			closestColorWordMap.put(layer, value);
		}
		// Finally, use the calculated value to retrieve the closest color word
		return value.get(stack);
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
