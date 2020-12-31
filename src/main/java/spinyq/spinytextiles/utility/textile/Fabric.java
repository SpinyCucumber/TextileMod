package spinyq.spinytextiles.utility.textile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;

public class Fabric implements INBTSerializable<CompoundNBT> {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final String TAG_PATTERN = "Pattern", TAG_COLORS = "Colors";
	private static final IForgeRegistry<FabricPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(FabricPattern.class);
	private static final IForgeRegistry<FabricLayer> LAYER_REGISTRY = LazyForgeRegistry.of(FabricLayer.class);

	// Pattern cannot be changed by users
	private FabricPattern pattern;
	private Map<FabricLayer, RYBKColor> colors = new HashMap<>();

	// Needed for deserialization
	public Fabric() {
	}

	public Fabric(FabricPattern pattern) {
		this.pattern = pattern;
	}

	public Fabric reduceColors() {
		LOGGER.trace("Reducing colors...");
		// Determine if the fabric is monochrome
		Optional<RYBKColor> monochrome = getMonochrome();
		LOGGER.trace("Monochrome: {}", monochrome);
		// If the fabric is monochrome, look up what pattern we should
		// switch to using the fabric pattern
		// Otherwise simply return the same fabric
		if (monochrome.isPresent()) {
			// Only construct a new fabric if the fabric pattern
			// supports it
			Optional<FabricPattern> newPattern = pattern.getMonochromePattern();
			LOGGER.trace("New pattern: {}", newPattern.map(FabricPattern::getRegistryName));
			if (newPattern.isPresent()) {
				// Create a new fabric with the new pattern, with all colors
				// set to the monochrome color
				Fabric fabric = new Fabric(newPattern.get());
				fabric.setColor(monochrome.get());
				return fabric;
			}
		}
		return this;
	}

	public void setLayerColor(FabricLayer layer, RYBKColor color) {
		colors.put(layer, color);
	}

	public void setLayerColor(int index, RYBKColor color) {
		setLayerColor(pattern.getLayer(index), color);
	}

	public RYBKColor getLayerColor(FabricLayer layer) {
		return colors.get(layer);
	}

	public RYBKColor getLayerColor(int index) {
		return getLayerColor(pattern.getLayer(index));
	}

	/**
	 * Sets all layers of this fabric to the given color.
	 * 
	 * @param color The new color
	 */
	public void setColor(RYBKColor color) {
		pattern.getLayerStream().forEach((layer) -> setLayerColor(layer, color));
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		// Write registry name of pattern to nbt
		NBTHelper.putRegistryEntry(nbt, TAG_PATTERN, pattern);
		// Write colors
		// Start constructing new compound NBT
		CompoundNBT mapNBT = new CompoundNBT();
		// For each entry in the map, write to the map nbt
		for (Entry<FabricLayer, RYBKColor> entry : colors.entrySet()) {
			// Get the key from the registry entry
			String key = entry.getKey().getRegistryName().toString();
			mapNBT.put(key, entry.getValue().serializeNBT());
		}
		// Put map nbt into the compound
		nbt.put(TAG_COLORS, mapNBT);
		// Done
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		// Retrieve the fabric's pattern by looking up registry name
		pattern = NBTHelper.getRegistryEntry(nbt, TAG_PATTERN, PATTERN_REGISTRY);
		// Retrieve the colors
		colors.clear();
		// Get the map NBT
		CompoundNBT mapNBT = nbt.getCompound(TAG_COLORS);
		// For each key in the map NBT, put the entry into the map
		for (String key : mapNBT.keySet()) {
			// Get the registry entry using the registry
			FabricLayer fabricLayer = LAYER_REGISTRY.getValue(new ResourceLocation(key));
			// Construct a new value and deserialize it
			RYBKColor color = new RYBKColor();
			color.deserializeNBT((IntNBT) mapNBT.get(key));
			// Put into map
			colors.put(fabricLayer, color);
		}
		// Finished
	}

	public FabricPattern getPattern() {
		return pattern;
	}

	@Override
	public int hashCode() {
		return Objects.hash(colors, pattern);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fabric other = (Fabric) obj;
		return Objects.equals(colors, other.colors) && Objects.equals(pattern, other.pattern);
	}

	@Override
	public String toString() {
		return "Fabric [pattern=" + pattern.getRegistryName() + ", colors=" + colors + "]";
	}

	/**
	 * Returns an optional color that is only present if the fabric is monochrome.
	 * That is, if all the layers are the same color, this method returns that
	 * color.
	 */
	private Optional<RYBKColor> getMonochrome() {
		Iterator<RYBKColor> colorIterator = pattern.getLayerStream().map(this::getLayerColor).iterator();
		// Get the first color
		RYBKColor toMatch = colorIterator.next();
		LOGGER.trace("Color to match: {}", toMatch);
		// If any other color doesn't match the first color, fail
		// Otherwise return the color
		while (colorIterator.hasNext()) {
			RYBKColor next = colorIterator.next();
			boolean match = toMatch.equalsRGB(next);
			LOGGER.trace("Comparing: {} Match: {}", next, match);
			if (!match)
				return Optional.empty();
		}
		return Optional.of(toMatch);
	}

}
