package spinyq.spinytextiles.client.model;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.model.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FabricTextureManager {

	/**
	 * Provides textures for each layer of a fabric pattern.
	 * @author SpinyQ
	 *
	 */
	public static class FabricTextures {
		
		private final ImmutableMap<String, Material> textures;

		private FabricTextures(ImmutableMap<String, Material> textures) {
			this.textures = textures;
		}
		
		public Material getTexture(String layer) {
			return textures.get(layer);
		}
		
	}
	
}
