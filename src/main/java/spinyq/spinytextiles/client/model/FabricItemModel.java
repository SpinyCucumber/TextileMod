package spinyq.spinytextiles.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.ModelTransformComposition;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;
import spinyq.spinytextiles.TextileMod;
import spinyq.spinytextiles.items.FabricItem;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.FabricPattern;

// ItemCameraTransform is deprecated but is still being used by BakedItemModel, so we are forced to
// use it as well.
@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public final class FabricItemModel implements IModelGeometry<FabricItemModel> {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final IForgeRegistry<FabricPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(FabricPattern.class);

	// minimal Z offset to prevent depth-fighting
	private static final float Z_OFFSET = 0.02f;
	private static final String MASK_TEXTURE = "mask";

	// A map between fabric patterns and baked models to use when overriding items'
	// models
	private Map<FabricPattern, IBakedModel> subModels;

	/**
	 * Bakes all the submodels of the fabric item model. This involves baking a new
	 * model for every fabric pattern, and putting it in a cache.
	 */
	private void bakeSubmodels(BakingContext context) {
		subModels = new HashMap<>();
		for (FabricPattern pattern : PATTERN_REGISTRY.getValues()) {
			subModels.put(pattern, bakeSubmodel(context, pattern));
		}
	}

	private IBakedModel bakeSubmodel(BakingContext context, FabricPattern pattern) {
		// For each layer of the fabric pattern, create a new quad layer using the
		// texture.
		// A mask is applied to each layer to create the look of a fabric item.
		// Can we use null for the particle texture?

		// Retrieve the particle and mask textures
		Material maskLocation = context.owner.resolveTexture(MASK_TEXTURE);

		IModelTransform transformsFromModel = context.owner.getCombinedTransform();
		ImmutableMap<TransformType, TransformationMatrix> transformMap = transformsFromModel != null
				? PerspectiveMapWrapper
						.getTransforms(new ModelTransformComposition(transformsFromModel, context.modelTransform))
				: PerspectiveMapWrapper.getTransforms(context.modelTransform);
		TransformationMatrix transform = context.modelTransform.getRotation();

		LOGGER.info("Baking a model for fabric pattern: {} with mask texture: {}", pattern.getRegistryName(), maskLocation);

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		// If our fabric info is non-null, convert it into quads
		// Get a list of layers and generate quads for each
		List<Material> textures = FabricTextureManager.INSTANCE.getTextureList(pattern);
		float z = 0.0f;
		TextureAtlasSprite maskSprite = context.spriteGetter.apply(maskLocation);
		LOGGER.info("Textures: {}", textures);
		for (Material texture : textures) {
			TextureAtlasSprite sprite = context.spriteGetter.apply(texture);
			// Add the quads
			// Use white color
			// TODO North side, etc.
			builder.addAll(ItemTextureQuadConverter.convertTexture(transform, maskSprite, sprite, z, Direction.SOUTH,
					0xffffffff, 1));
			// Increase the depth for each layer
			z += Z_OFFSET;
		}

		// Construct the baked model
		// The override handler for this model is arbitrary
		return new BakedItemModel(builder.build(), null, Maps.immutableEnumMap(transformMap), context.overrides,
				transform.isIdentity(), context.owner.isSideLit());
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
			ItemOverrideList overrides, ResourceLocation modelLocation) {
		// Create a "baking context" to pass to other methods so we don't have to type
		// out lists of ugly arguments
		BakingContext context = new BakingContext(owner, bakery, spriteGetter, modelTransform, overrides,
				modelLocation);
		// Bake all of our submodels
		bakeSubmodels(context);
		// Construct the baked model
		// Make sure to give it our custom override handler so it can switch models
		// Since this model is never really going to be rendered most of the arguments here are arbitrary
		// We could make a class like "DummyBakedItemModel" if we wanted to avoid all the arguments
		ItemOverrideList overrideHandler = new OverrideHandler();
		return new BakedItemModel(ImmutableList.of(), null, ImmutableMap.of(), overrideHandler, true, true);
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		Set<Material> texs = new HashSet<>();

		texs.add(owner.resolveTexture(MASK_TEXTURE));
		// Add all textures used by fabric patterns
		texs.addAll(FabricTextureManager.INSTANCE.getAllTextureLocations());

		return texs;
	}

	@EventBusSubscriber(bus = Bus.MOD)
	public static class Loader implements IModelLoader<FabricItemModel> {

		public static final ResourceLocation ID = new ResourceLocation(TextileMod.MODID, "fabric_item_model");

		@SubscribeEvent
		public static void onRegisterModels(ModelRegistryEvent event) {
			// Register ourselves as a model loader
			ModelLoaderRegistry.registerLoader(ID, new Loader());
		}

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
		public FabricItemModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			// Construct a new fabric item model
			return new FabricItemModel();
		}
	}

	private class OverrideHandler extends ItemOverrideList {

		@Override
		public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World world,
				@Nullable LivingEntity entity) {
			// Check if the item is a fabric item
			// If it is, get the model corresponding to the fabric pattern
			if (stack.getItem() instanceof FabricItem) {
				FabricItem item = (FabricItem) stack.getItem();
				// Get the fabric pattern
				FabricPattern pattern = item.getFabric(stack).getPattern();
				return subModels.get(pattern);
			}
			// If the item is not a fabric item simply return the original model
			return originalModel;
		}
	}

}