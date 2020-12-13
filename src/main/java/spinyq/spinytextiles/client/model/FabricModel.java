package spinyq.spinytextiles.client.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelTransformComposition;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;
import spinyq.spinytextiles.TextileMod;
import spinyq.spinytextiles.client.model.ModelHelper.Layer;
import spinyq.spinytextiles.items.FabricItem;
import spinyq.spinytextiles.utility.textile.FabricInfo;

public final class FabricModel implements IModelGeometry<FabricModel> {

	// minimal Z offset to prevent depth-fighting
	private static final float Z_OFFSET = 0.02f;

	@Nonnull
	private final FabricInfo info;

	public FabricModel(FabricInfo info) {
		this.info = info;
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
			ItemOverrideList overrides, ResourceLocation modelLocation) {
		// For each layer of the fabric pattern, create a new quad layer using the
		// texture.
		// A mask is applied to each layer to create the look of a fabric item.
		// Can we use null for the particle texture?

		// Retrieve the particle and mask textures
		Material maskLocation = owner.resolveTexture("mask");

		IModelTransform transformsFromModel = owner.getCombinedTransform();
		ImmutableMap<TransformType, TransformationMatrix> transformMap = transformsFromModel != null
				? PerspectiveMapWrapper
						.getTransforms(new ModelTransformComposition(transformsFromModel, modelTransform))
				: PerspectiveMapWrapper.getTransforms(modelTransform);
		TransformationMatrix transform = modelTransform.getRotation();

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		// Create the quads
		// Get a list of layers and generate quads for each
		float z = 0.0f;
		TextureAtlasSprite maskSprite = spriteGetter.apply(maskLocation);
		for (Layer layer : info.getLayers()) {
			TextureAtlasSprite sprite = spriteGetter.apply(layer.texture);
			// Add the quads
			builder.addAll(ItemTextureQuadConverter.convertTexture(transform, maskSprite, sprite, z, Direction.NORTH,
					layer.color.toIntARGB(), 1));
			// Increase the depth for each layer
			z += Z_OFFSET;
		}

		return new BakedModel(bakery, owner, builder.build(), null, Maps.immutableEnumMap(transformMap),
				Maps.newHashMap(), transform.isIdentity(), modelTransform, owner.isSideLit());
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		Set<Material> texs = new HashSet<>();

		texs.add(owner.resolveTexture("mask"));
		texs.addAll(info.getTextures());

		return texs;
	}

	public enum Loader implements IModelLoader<FabricModel> {
		INSTANCE;

		@Override
		public IResourceType getResourceType() {
			return VanillaResourceType.MODELS;
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
			// no need to clear cache since we create a new model instance
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager,
				Predicate<IResourceType> resourcePredicate) {
			// no need to clear cache since we create a new model instance
		}

		@Override
		public FabricModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			// Construct a new model with a default fabric info

		}
	}

	private static final class OverrideHandler extends ItemOverrideList {
		
		private final ModelBakery bakery;

		private OverrideHandler(ModelBakery bakery) {
			this.bakery = bakery;
		}

		@Override
		public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World world,
				@Nullable LivingEntity entity) {
			// Check if the item is a fabric item
			// If it is, get the model corresponding to the fabric info.
			// We cache models so that we don't have to bake them each time.
			if (stack.getItem() instanceof FabricItem) {
				FabricItem item = (FabricItem) stack.getItem();
				BakedModel model = (BakedModel) originalModel;
				// Get the fabric info
				FabricInfo info = item.getInfo(stack);
				// Check if info is in cache
				// If it isn't, we have to create it
				if (model.cache.containsKey(info)) {
					return model.cache.get(info);
				} else {
					// Create a new unbaked model with the fabric info then bake it
					IBakedModel bakedModel = new FabricModel(info).bake(model.owner, bakery,
							ModelLoader.defaultTextureGetter(), model.originalTransform, model.getOverrides(),
							new ResourceLocation(TextileMod.MODID, "fabric_item_override"));
					model.cache.put(info, bakedModel);
					return bakedModel;
				}
			}
			// If the item is not a fabric item simply return the original model
			return originalModel;
		}
	}

	// the dynamic bucket is based on the empty bucket
	private static final class BakedModel extends BakedItemModel {
		
		private final IModelConfiguration owner;
		private final Map<FabricInfo, IBakedModel> cache; // contains all the baked models since they'll never change
		private final IModelTransform originalTransform;

		BakedModel(ModelBakery bakery, IModelConfiguration owner, ImmutableList<BakedQuad> quads,
				TextureAtlasSprite particle, ImmutableMap<TransformType, TransformationMatrix> transforms,
				Map<FabricInfo, IBakedModel> cache, boolean untransformed, IModelTransform originalTransform,
				boolean isSideLit) {
			super(quads, particle, transforms, new OverrideHandler(bakery), untransformed, isSideLit);
			this.owner = owner;
			this.cache = cache;
			this.originalTransform = originalTransform;
		}
	}

}