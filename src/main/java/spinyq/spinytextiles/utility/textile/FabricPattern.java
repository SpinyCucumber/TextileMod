package spinyq.spinytextiles.utility.textile;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.Util;

/**
 * A pattern used to make fabric in the loom.
 * Consists of multiple layers, represented by a name.
 * FabricTextureManager maps each layer to a texture.
 * FabricInfo represents a single "fabric" and additionally assigns a color to each layer.
 * @author Elijah Hilty
 *
 */
public class FabricPattern extends AbstractPattern<FabricPattern> {
	
	// An ordered list of the fabric layers
	private final ImmutableList<String> layers;
	private String translationKey;

	public ImmutableList<String> getLayers() {
		return layers;
	}

	private FabricPattern(ImmutableList<String> layers) {
		this.layers = layers;
	}
	
	public FabricPattern(String...layers) {
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
	
	public String getLayerTranslationKey(String layer) {
		return "fabriclayer." + layer;
	}

}
