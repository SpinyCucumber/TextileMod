package spinyq.spinytextiles.utility.textile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;

public class Fabric implements IGarmentComponent {

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
		// Determine if the fabric is monochrome
		Optional<RYBKColor> monochrome = getMonochrome();
		// If the fabric is monochrome, look up what pattern we should
		// switch to using the fabric pattern
		// Otherwise simply return the same fabric
		if (monochrome.isPresent()) {
			// TODO
		}
		return this;
	}
	
	public void setColor(FabricLayer layer, RYBKColor color) {
		colors.put(layer, color);
	}
	
	public void setColor(int index, RYBKColor color) {
		setColor(pattern.getLayer(index), color);
	}
	
	public RYBKColor getColor(FabricLayer layer) {
		return colors.get(layer);
	}
	
	public RYBKColor getColor(int index) {
		return getColor(pattern.getLayer(index));
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
		Stream<RYBKColor> colorStream = pattern.getLayerStream().map(this::getColor);
		// Get the first color
		RYBKColor toMatch = colorStream.findFirst().get();
		// If any other color doesn't match the first color, fail
		// Otherwise return the color
		if (!colorStream.allMatch(Predicate.isEqual(toMatch))) return Optional.empty();
		return Optional.of(toMatch);
	}

}
