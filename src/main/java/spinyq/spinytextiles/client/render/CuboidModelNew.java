package spinyq.spinytextiles.client.render;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.FaceBakery;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public class CuboidModelNew implements IModelGeometry<CuboidModelNew> {

	private static final FaceBakery FACE_BAKERY = new FaceBakery();

	private BlockPart blockPart;

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
			ItemOverrideList overrides, ResourceLocation modelLocation) {
		// This code is adapted from BlockModel
		// Start constructing a new simple baked model
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(owner, overrides);
		// Add quads for each face
		for (Direction direction : blockPart.mapFaces.keySet()) {
			BlockPartFace blockpartface = blockPart.mapFaces.get(direction);
			TextureAtlasSprite textureatlassprite1 = spriteGetter.apply(this.resolveTextureName(blockpartface.texture));
			if (blockpartface.cullFace == null) {
				builder.addGeneralQuad(bakeFace(blockPart, blockpartface, textureatlassprite1, direction,
						modelTransform, modelLocation));
			} else {
				builder.addFaceQuad(
						Direction.rotateFace(modelTransform.getRotation().getMatrix(), blockpartface.cullFace),
						bakeFace(blockPart, blockpartface, textureatlassprite1, direction, modelTransform,
								modelLocation));
			}
		}
		// Finished
		return builder.build();
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		// TODO
		return null;
	}

	@SuppressWarnings("deprecation")
	public Material resolveTextureName(String nameIn) {
		// Simply construct a new material using the block atlas
		return new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(nameIn));
	}

	private static BakedQuad bakeFace(BlockPart partIn, BlockPartFace partFaceIn, TextureAtlasSprite spriteIn,
			Direction directionIn, IModelTransform transformIn, ResourceLocation locationIn) {
		return FACE_BAKERY.bakeQuad(partIn.positionFrom, partIn.positionTo, partFaceIn, spriteIn, directionIn,
				transformIn, partIn.partRotation, partIn.shade, locationIn);
	}

}
