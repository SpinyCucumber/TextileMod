package spinyq.spinytextiles.utility.textile;

import com.google.common.collect.ImmutableList;

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

	public ImmutableList<String> getLayers() {
		return layers;
	}

	private FabricPattern(ImmutableList<String> layers) {
		this.layers = layers;
	}
	
	public FabricPattern(String...layers) {
		this(ImmutableList.copyOf(layers));
	}

}
