package spinyq.spinytextiles.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
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
	
	private Map<Integer, CalculatedValue<ColorWord>> closestColorWordMap = new HashMap<>();
	// The costs to dye/bleach a fabric item, applied for each layer
	private int layerDyeCost = 1, layerBleachCost = 1;
	
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
					// Look up the color using the tint index
					return fabric.getColor(tintIndex).toRGB(new RGBColor(), null).toInt();
				}
				return -1;
			}, this);
	}
	
	// TODO Possible improvement: For both dyeing and bleaching, combine consecutive layers
	// into a single layer if they have the same color. There would be a list of rules about
	// what kind of layers can do this, but it would make sense from a user perspective.
	// When layers are combined, we would need to find a pattern that matches the new list of layers.
	@Override
	public boolean dye(ContainedItemStack<PlayerInventory> stack, IDyeProvider provider) {
		// Only attempt to dye if we have fabric info attached
		Fabric fabric = getFabric(stack.getStack());
		if (fabric == null) return false;
		// Iterate over each layer in the pattern, keeping
		// track of whether we successfully dyed the layer
		FabricPattern pattern = fabric.getPattern();
		boolean success = pattern.getLayerIndexStream().anyMatch((layerIndex) -> {
			// Get current color of layer
			// Add dye color to existing color to get new color of layer
			RYBKColor oldColor = fabric.getColor(layerIndex);
			RYBKColor newColor = provider.getColor().plus(oldColor).clamp();
			// Fail if new color didn't change
			if (Objects.equal(oldColor, newColor)) return false;
			// Attempt to pay for dye
			// If the provider has enough dye, proceed to dye the layer
			if (provider.drain(layerDyeCost)) {
				fabric.setColor(layerIndex, newColor);
				// Also mark the layer's closest color word as dirty,
				// so we know to recalculate it
				getClosestColorWord(layerIndex).markDirty(stack.getStack());
				return true;
			}
			return false;
		});
		// If we successfully dyed a layer, create a new itemstack
		// with the updated fabric info and give it to the player
		if (success) {
			// Only dye one item at a time
			ItemStack dyedFabricItem = stack.getStack().split(1);
			setFabric(dyedFabricItem, fabric);
			stack.getInventory().addItemStackToInventory(dyedFabricItem);
		}
		// Return whether we were successful
		return success;
	}

	@Override
	public boolean bleach(ContainedItemStack<PlayerInventory> stack, IBleachProvider provider) {
		// Only attempt to bleach if we have fabric info attached
		Fabric fabric = getFabric(stack.getStack());
		if (fabric == null) return false;
		// Iterate over each layer in the pattern, keeping
		// track of whether we successfully bleached the layer
		FabricPattern pattern = fabric.getPattern();
		boolean success = pattern.getLayerIndexStream().anyMatch((layerIndex) -> {
			// Get current color of layer
			// Subtract from each component of current color to get new color
			RYBKColor oldColor = fabric.getColor(layerIndex);
			RYBKColor newColor = oldColor.minus(new RYBKColor(provider.getBleachLevel())).clamp();
			// Skip layer if new color didn't change
			if (Objects.equal(oldColor, newColor)) return false;
			// Attempt to pay for bleach
			// If the provider has enough bleach, proceed to bleach the layer
			if (provider.drain(layerBleachCost)) {
				fabric.setColor(layerIndex, newColor);
				// Also mark the layer's closest color word as dirty,
				// so we know to recalculate it
				getClosestColorWord(layerIndex).markDirty(stack.getStack());
				return true;
			}
			return false;
		});
		// If we successfully bleached a layer, create a new itemstack
		// with the updated fabric info and give it to the player
		if (success) {
			// Only bleach one item at a time
			ItemStack bleachedFabricItem = stack.getStack().split(1);
			setFabric(bleachedFabricItem, fabric);
			stack.getInventory().addItemStackToInventory(bleachedFabricItem);
		}
		// Return whether we were successful
		return success;
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (FabricPattern pattern : PATTERN_REGISTRY.getValues()) {
				items.add(createDefaultFabricItem(pattern));
			}
		}
	}
	
	// Adds additional information to the fabric item's tooltip
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
			pattern.getLayerIndexStream().forEach((layerIndex) -> {
				ColorWord closestColorWord = getClosestColorWord(layerIndex).get(stack);
				// Construct tooltip line
				// We pass the layer's name and the color's name as parameters
				tooltip.add(new TranslationTextComponent(colorInfoTranslationKey,
						new TranslationTextComponent(pattern.getLayer(layerIndex).getTranslationKey()),
						new TranslationTextComponent(closestColorWord.getTranslationKey()))
						.applyTextStyles(TextFormatting.GRAY));
			});
		}
	}

	// Retrieves the color word closest to the color of a given layer.
	// We use CalculatedValue because it automatically caches the value for us,
	// so we don't have to recompute it each time, as determining the
	// closest color is a slightly expensive operation.
	private CalculatedValue<ColorWord> getClosestColorWord(int layerIndex) {
		// Try to look up our calculated value.
		// If it doesn't exist, we have to create it.
		// We also store it in the cache after creating it.
		CalculatedValue<ColorWord> value = closestColorWordMap.get(layerIndex);
		if (value == null) {
			String tag = "ColorWord" + String.valueOf(layerIndex);
			value = NBTHelper.createCalculatedEnumValue(
					tag, ColorWord.class,
					(item) -> {
						// Get color of layer, then get closest color word to color
						Fabric fabric = getFabric(item);
						return ColorWord.getClosest(fabric.getColor(layerIndex));
					});
			// Store the calculated value in our map
			closestColorWordMap.put(layerIndex, value);
		}
		// Finally, use the calculated value to retrieve the closest color word
		return value;
	}

	private ItemStack createDefaultFabricItem(FabricPattern pattern) {
		// Create new fabric that uses the pattern
		Fabric fabric = new Fabric(pattern);
		// For every layer in the pattern, make the fabric use a default color
		// If there are more layers than colors cycle through the colors
		pattern.getLayerIndexStream().forEach((layerIndex) -> {
			RYBKColor color = DEFAULT_COLORS.get(layerIndex % DEFAULT_COLORS.size()).copy();
			fabric.setColor(layerIndex, color);
		});
		// Construct a new itemstack and set the fabric
		ItemStack item = new ItemStack(this);
		setFabric(item, fabric);
		return item;
	}
	
}
