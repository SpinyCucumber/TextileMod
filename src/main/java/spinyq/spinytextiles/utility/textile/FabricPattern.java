package spinyq.spinytextiles.utility.textile;

import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.Util;

/**
 * A pattern used to make fabric in the loom.
 * Consists of multiple layers, represented by a name.
 * Fabric represents a single "fabric" and additionally assigns a color to each layer.
 * @author Elijah Hilty
 *
 */
public class FabricPattern extends AbstractPattern<FabricPattern> {
	
	// An ordered list of the fabric layers
	private final ImmutableList<Supplier<FabricLayer>> layers;
	private String translationKey;

	public Stream<FabricLayer> getLayerStream() {
		return layers.stream().map(Supplier::get);
	}
	
	public IntStream getLayerIndexStream() {
		return IntStream.range(0, layers.size());
	}
	
	public FabricLayer getLayer(int index) {
		return layers.get(index).get();
	}

	private FabricPattern(ImmutableList<Supplier<FabricLayer>> layers) {
		this.layers = layers;
	}
	
	@SafeVarargs
	public FabricPattern(Supplier<FabricLayer>...layers) {
		this(ImmutableList.copyOf(layers));
	}
	
	/**
	 * Returns a translation key which can can be translated into the name of this fabric pattern.
	 * For example, this might translate as "Vertical Stripes" or "Checkerboard"
	 */
	public String getTranslationKey() {
		if (translationKey == null) translationKey = Util.makeTranslationKey("fabricpattern", getRegistryName());
		return translationKey;
	}
	
	/**
	 * Returns a translation key which can be translated to a description of this fabric pattern.
	 * For example, this might translate as "striped" in English for a striped fabric pattern.
	 */
	public String getDescriptionTranslationKey() {
		return getTranslationKey() + ".description";
	}

}
