package spinyq.spinytextiles.utility.textile;

import java.util.SortedMap;

import net.minecraft.util.ResourceLocation;

/**
 * A pattern used to make fabric in the loom.
 * Consists of multiple layers, each of which has a name and texture.
 * Colors are applied to layers based on the thread used to make the fabric.
 * @author Elijah Hilty
 *
 */
public class FabricPattern extends AbstractPattern<FabricPattern> {

	public static class Builder {
		
		private SortedMap<String, ResourceLocation> layers;
		
		public Builder withLayer(String layerName, ResourceLocation texture) {
			layers.put(layerName, texture);
			return this;
		}
		
		public Builder withLayer(String layerName) {
			// TODO Generate texture location using layer name and pattern registry name.
			// Check out the Item class?
			return this;
		}
		
		public FabricPattern build() {
			return new FabricPattern(layers);
		}
		
	}
	
	// A mapping between a string parameter that represents a layer with a color, and a texture.
	// Preserves order so textures are displayed correctly.
	private SortedMap<String, ResourceLocation> layers;

	public FabricPattern(SortedMap<String, ResourceLocation> layers) {
		this.layers = layers;
	}

	public SortedMap<String, ResourceLocation> getLayers() {
		return layers;
	}

}
