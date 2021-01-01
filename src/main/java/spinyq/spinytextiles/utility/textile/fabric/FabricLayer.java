package spinyq.spinytextiles.utility.textile.fabric;

import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FabricLayer extends ForgeRegistryEntry<FabricLayer> {

	// Items are forced to use the block atlas, since they use the TRANSLUCENT_BLOCK_TYPE render type.
	@SuppressWarnings("deprecation")
	private static final ResourceLocation ATLAS_LOCATION = AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	
	private String translationKey;
	
	/**
	 * Returns the texture used by this fabric layer.
	 */
	@OnlyIn(Dist.CLIENT)
	public Material getTexture() {
		// Construct new resource location then create material
		ResourceLocation textureLocation = new ResourceLocation(
				getRegistryName().getNamespace(), "fabriclayer/" + getRegistryName().getPath());
		return new Material(ATLAS_LOCATION, textureLocation);
	}
	
	public String getTranslationKey() {
		if (translationKey == null) translationKey = Util.makeTranslationKey("fabriclayer", getRegistryName());
		return translationKey;
	}
	
}
