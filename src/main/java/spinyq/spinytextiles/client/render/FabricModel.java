package net.minecraftforge.client.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.DynamicBucketModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelTransformComposition;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;
import spinyq.spinytextiles.utility.color.RGBAColor;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.textile.FabricInfo;

public final class FabricModel implements IModelGeometry<FabricModel> {
	private static final Logger LOGGER = LogManager.getLogger();

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
		Material particleLocation = owner.resolveTexture("particle");
		Material maskLocation = owner.resolveTexture("mask");

		IModelTransform transformsFromModel = owner.getCombinedTransform();

		ImmutableMap<TransformType, TransformationMatrix> transformMap = transformsFromModel != null
				? PerspectiveMapWrapper
						.getTransforms(new ModelTransformComposition(transformsFromModel, modelTransform))
				: PerspectiveMapWrapper.getTransforms(modelTransform);
		TransformationMatrix transform = modelTransform.getRotation();

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

		// Create the quads here
		// Get sprites using materials
		float z = 0.0f;
		TextureAtlasSprite maskSprite = spriteGetter.apply(maskLocation);
		for (String layerId : info.getLayerIds()) {
			// Calculate the color
			TextureAtlasSprite sprite = spriteGetter.apply(info.getTexture(layerId));
			int color = new RGBAColor(info.getColor(layerId).toRGB(new RGBColor(), null), 1.0f).toIntARGB();
			// Add the quads
            builder.addAll(ItemTextureQuadConverter.convertTexture(transform, maskSprite, sprite, z, Direction.NORTH, color, 1));
            // Increase the depth for each layer
            z += Z_OFFSET;
		}

		return new BakedModel(bakery, owner, this, builder.build(), null, Maps.immutableEnumMap(transformMap),
				Maps.newHashMap(), transform.isIdentity(), modelTransform, owner.isSideLit());
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		Set<Material> texs = Sets.newHashSet();

		texs.add(owner.resolveTexture("particle"));
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

	private static final class ContainedFluidOverrideHandler extends ItemOverrideList {
		private final ModelBakery bakery;

		private ContainedFluidOverrideHandler(ModelBakery bakery) {
			this.bakery = bakery;
		}

		@Override
		public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World world,
				@Nullable LivingEntity entity) {
			return FluidUtil.getFluidContained(stack).map(fluidStack -> {
				BakedModel model = (BakedModel) originalModel;

				Fluid fluid = fluidStack.getFluid();
				String name = fluid.getRegistryName().toString();

				if (!model.cache.containsKey(name)) {
					DynamicBucketModel parent = model.parent.withFluid(fluid);
					IBakedModel bakedModel = parent.bake(model.owner, bakery, ModelLoader.defaultTextureGetter(),
							model.originalTransform, model.getOverrides(),
							new ResourceLocation("forge:bucket_override"));
					model.cache.put(name, bakedModel);
					return bakedModel;
				}

				return model.cache.get(name);
			})
					// not a fluid item apparently
					.orElse(originalModel); // empty bucket
		}
	}

	// the dynamic bucket is based on the empty bucket
	private static final class BakedModel extends BakedItemModel {
		private final IModelConfiguration owner;
		private final FabricModel parent;
		private final Map<String, IBakedModel> cache; // contains all the baked models since they'll never change
		private final IModelTransform originalTransform;
		private final boolean isSideLit;

		BakedModel(ModelBakery bakery, IModelConfiguration owner, FabricModel parent, ImmutableList<BakedQuad> quads,
				TextureAtlasSprite particle, ImmutableMap<TransformType, TransformationMatrix> transforms,
				Map<String, IBakedModel> cache, boolean untransformed, IModelTransform originalTransform,
				boolean isSideLit) {
			super(quads, particle, transforms, new ContainedFluidOverrideHandler(bakery), untransformed, isSideLit);
			this.owner = owner;
			this.parent = parent;
			this.cache = cache;
			this.originalTransform = originalTransform;
			this.isSideLit = isSideLit;
		}
	}

}