package spinyq.spinytextiles.utility.textile;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

		private List<String> layers = new LinkedList<>();
		private Map<String, Material> textures = new HashMap<>();
		
		public Builder withLayer(String layer, Material texture) {
			layers.add(layer);
			textures.put(layer, texture);
			return this;
		}
		
		public FabricPattern build() {
			return new FabricPattern(layers, textures);
		}
		
	}
	
	// An ordered list of the different layers
	final List<String> layers;
	// A mapping between a layer id and a texture.
	final Map<String, Material> textures;

	private FabricPattern(List<String> layers, Map<String, Material> textures) {
		this.layers = layers;
		this.textures = textures;
	}

}
