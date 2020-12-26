package spinyq.spinytextiles.utility.textile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;

public class Fabric implements IGarmentComponent {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String TAG_PATTERN = "Pattern", TAG_COLORS = "Colors";
	private static final IForgeRegistry<FabricPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(FabricPattern.class);
	
	// Pattern cannot be changed by users
	private FabricPattern pattern;
	private Map<FabricLayer, RYBKColor> colors = new HashMap<>();
	
	// Needed for deserialization
	public Fabric() { }
	
	public Fabric(FabricPattern pattern) {
		this.pattern = pattern;
	}
	
	public Fabric reduceColors() {
		LOGGER.info("Reducing colors...");
		// Determine if the fabric is monochrome
		Optional<RYBKColor> monochrome = getMonochrome();
		LOGGER.info("Monochrome: {}", monochrome);
		// If the fabric is monochrome, look up what pattern we should
		// switch to using the fabric pattern
		// Otherwise simply return the same fabric
		if (monochrome.isPresent()) {
			// Only construct a new fabric if the fabric pattern
			// supports it
			Optional<FabricPattern> newPattern = pattern.getMonochromePattern();
			LOGGER.info("New pattern: {}", newPattern.map(FabricPattern::getRegistryName));
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
		List<RYBKColor> colorList = pattern.getLayerStream()
				.map(colors::get)
				.collect(Collectors.toList());
		NBTHelper.putCollection(nbt, TAG_COLORS, colorList);
		// Done
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		// Retrieve the fabric's pattern by looking up registry name
		pattern = NBTHelper.getRegistryEntry(nbt, TAG_PATTERN, PATTERN_REGISTRY);
		// Retrieve the colors
		List<RYBKColor> colorList = NBTHelper.getCollection(ArrayList::new, RYBKColor::new, nbt, TAG_COLORS);
		pattern.getLayerIndexStream().forEach(
				(index) -> colors.put(pattern.getLayer(index), colorList.get(index)));
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
	 * Returns an optional color that is only present if the
	 * fabric is monochrome. That is, if all the layers are the same color,
	 * this method returns that color.
	 */
	private Optional<RYBKColor> getMonochrome() {
		Iterator<RYBKColor> colorIterator = pattern.getLayerStream()
				.map(this::getLayerColor)
				.iterator();
		// Get the first color
		RYBKColor toMatch = colorIterator.next();
		LOGGER.info("Color to match: {}", toMatch);
		// If any other color doesn't match the first color, fail
		// Otherwise return the color
		while (colorIterator.hasNext()) {
			RYBKColor next = colorIterator.next();
			boolean match = toMatch.equalsRGB(next);
			LOGGER.info("Comparing: {} Match: {}", next, match);
			if (!match) return Optional.empty();
		}
		return Optional.of(toMatch);
	}

}
