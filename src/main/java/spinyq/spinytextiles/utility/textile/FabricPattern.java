package spinyq.spinytextiles.utility.textile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.model.Material;

/**
 * A pattern used to make fabric in the loom.
 * Consists of multiple layers, each of which has a name and texture.
 * Colors are applied to layers based on the thread used to make the fabric.
 * @author Elijah Hilty
 *
 */
public class FabricPattern extends AbstractPattern<FabricPattern> {

	/**
	 * Utility class which can be used to create a FabricPattern.
	 * @author Elijah Hilty
	 *
	 */
	public static class Builder {

		private ImmutableList.Builder<String> layerBuilder;
		private ImmutableMap.Builder<String, Material> textureBuilder;
		
		public Builder withLayer(String layer, Material texture) {
			layerBuilder.add(layer);
			textureBuilder.put(layer, texture);
			return this;
		}
		
		public FabricPattern build() {
			return new FabricPattern(layerBuilder.build(), textureBuilder.build());
		}
		
	}
	
	// An ordered list of the different layers
	final ImmutableList<String> layers;
	// A mapping between a layer id and a texture.
	final ImmutableMap<String, Material> textures;

	private FabricPattern(ImmutableList<String> layers, ImmutableMap<String, Material> textures) {
		this.layers = layers;
		this.textures = textures;
	}

}
