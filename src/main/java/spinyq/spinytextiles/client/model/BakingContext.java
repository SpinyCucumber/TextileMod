package spinyq.spinytextiles.client.model;

import java.util.function.Function;

import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

public class BakingContext {

	public final IModelConfiguration owner;
	public final ModelBakery bakery;
	public final Function<Material, TextureAtlasSprite> spriteGetter;
	public final IModelTransform modelTransform;
	public final ItemOverrideList overrides;
	public final ResourceLocation modelLocation;
	
	public BakingContext(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
			ItemOverrideList overrides, ResourceLocation modelLocation) {
		this.owner = owner;
		this.bakery = bakery;
		this.spriteGetter = spriteGetter;
		this.modelTransform = modelTransform;
		this.overrides = overrides;
		this.modelLocation = modelLocation;
	}
	
}
