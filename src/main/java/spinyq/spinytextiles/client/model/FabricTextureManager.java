package spinyq.spinytextiles.client.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.model.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import spinyq.spinytextiles.utility.textile.Fabric;
import spinyq.spinytextiles.utility.textile.FabricPattern;

/**
 * Assigns fabric patterns textures for each of their layers.
 * @author SpinyQ
 *
 */
@OnlyIn(Dist.CLIENT)
public class FabricTextureManager {

	/**
	 * Provides textures for each layer of a fabric pattern.
	 * @author SpinyQ
	 *
	 */
	private static class FabricTextures {
		
		private final ImmutableMap<String, Material> map;

		private FabricTextures(ImmutableMap<String, Material> textures) {
			this.map = textures;
		}
		
		private Material get(String layer) {
			return map.get(layer);
		}
		
		private Collection<Material> values() {
			return map.values();
		}
		
	}
	
	// The internal map between fabric patterns and textures
	private static Map<FabricPattern, FabricTextures> map = new HashMap<>();
	
	/**
	 * Returns a list of the corresponding texture for each layer in the fabric pattern.
	 * The list is in order of layers and is unmodifiable.
	 * @param pattern The fabric pattern
	 * @return The list
	 */
	public static List<Material> getTextureList(FabricPattern pattern) {
		// Use streams to create a new list
		// We use the pattern's list to ensure that the textures are in the right order
		// First, look up the fabric textures
		FabricTextures textures = map.get(pattern);
		return pattern.getLayers().stream().map(textures::get).collect(Collectors.toUnmodifiableList());
	}
	
	/**
	 * Returns of collection of textures that the fabric pattern uses.
	 * The collection has no order is unmodifiable. If the user wants a list of textures
	 * ordered by layer, they should use getTextureList.
	 * @param pattern The fabric pattern
	 * @return The collection
	 */
	public static Collection<Material> getTextures(FabricPattern pattern) {
		// Lookup textures and return values
		FabricTextures textures = map.get(pattern);
		return Collections.unmodifiableCollection(textures.values());
	}
	
	// The following are convenience methods that retrieve the textures for a fabric.
	
	public static List<Material> getTextureList(Fabric fabric) {
		return getTextureList(fabric.getPattern());
	}
	
	public static Collection<Material> getTextures(Fabric fabric) {
		return getTextures(fabric.getPattern());
	}
	
	// TODO Loading
	
}
