package spinyq.spinytextiles.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
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

	public class OverrideHandler extends ItemOverrideList {

		@Override
		public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World world,
				@Nullable LivingEntity entity) {
			// Check if the item is a fabric item
			// If it is, get the model corresponding to the fabric pattern
			if (stack.getItem() instanceof FabricItem) {
				FabricItem item = (FabricItem) stack.getItem();
				// Get the fabric pattern
				FabricPattern pattern = item.getFabric(stack).getPattern();
				return bakedSubModels.get(pattern);
			}
			// If the item is not a fabric item simply return the original model
			return originalModel;
		}
	}

	public class SubModel implements IModelGeometry<SubModel> {

		private static final String TEMPLATE_TEXTURE = "template",
				DETAIL_TEXTURE = "detail";

		private FabricPattern pattern;

		public SubModel(FabricPattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
				Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
				ItemOverrideList overrides, ResourceLocation modelLocation) {
			// For each layer of the fabric pattern, create a new quad layer using the
			// texture.
			// A template is applied to each layer to create the look of a fabric item.
			// Can we use null for the particle texture?

			// Retrieve template texture and detail texture
			Material templateLocation = owner.resolveTexture(TEMPLATE_TEXTURE),
					detailLocation = owner.resolveTexture(DETAIL_TEXTURE);

			IModelTransform transformsFromModel = owner.getCombinedTransform();
			ImmutableMap<TransformType, TransformationMatrix> transformMap = transformsFromModel != null
					? PerspectiveMapWrapper
							.getTransforms(new ModelTransformComposition(transformsFromModel, modelTransform))
					: PerspectiveMapWrapper.getTransforms(modelTransform);
			TransformationMatrix transform = modelTransform.getRotation();

			LOGGER.info("Baking a model for fabric pattern: {}", pattern.getRegistryName());

			ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
			// Look up the pattern's textures using FabricTextureManager, and create a list of sprites for each layer
			TextureAtlasSprite template = spriteGetter.apply(templateLocation),
					detail = spriteGetter.apply(detailLocation);
			List<TextureAtlasSprite> sprites = FabricTextureManager.INSTANCE.getTextureStream(pattern)
					.map(spriteGetter::apply)
					.collect(Collectors.toList());
			// Build quads
			// We also increment the tint index for each layer
			int tint = 0;
			for (TextureAtlasSprite sprite : sprites) {
				TemplateItemModel.generateQuads(tint, template, sprite, transform, builder);
				TemplateItemModel.generateQuads(tint, sprite, detail, transform, builder);
				tint++;
			}
			// Construct the baked model
			// The override handler for this model is arbitrary
			return new BakedItemModel(builder.build(), null, Maps.immutableEnumMap(transformMap), overrides,
					transform.isIdentity(), owner.isSideLit());
		}

		@Override
		public Collection<Material> getTextures(IModelConfiguration owner,
				Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
			// Create a new set of textures
			Set<Material> textures = new HashSet<>();
			// Include all the textures used by the pattern
			textures.addAll(FabricTextureManager.INSTANCE.getTextures(pattern));
			// Also include the mask texture
			textures.add(owner.resolveTexture(TEMPLATE_TEXTURE));
			return textures;
		}

	}

	private static final Logger LOGGER = LogManager.getLogger();
	private static final IForgeRegistry<FabricPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(FabricPattern.class);

	// A map between fabric patterns and baked models to use when overriding items'
	// models
	private Map<FabricPattern, IBakedModel> bakedSubModels;
	private Collection<SubModel> subModels;

	public FabricItemModel() {
		// Create our submodels whenever the model is first constructed
		createSubModels();
	}

	/**
	 * Creates the fabric item model's submodel's, without baking them. This
	 * involves creating a submodel for every fabric pattern.
	 */
	private void createSubModels() {
		subModels = new LinkedList<>();
		for (FabricPattern pattern : PATTERN_REGISTRY.getValues()) {
			subModels.add(new SubModel(pattern));
		}
	}

	/**
	 * Bakes the fabric item's submodels, putting the baked models into a cache.
	 */
	private void bakeSubmodels(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
			ItemOverrideList overrides, ResourceLocation modelLocation) {
		bakedSubModels = new HashMap<>();
		for (SubModel subModel : subModels) {
			bakedSubModels.put(subModel.pattern,
					subModel.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
		}
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
			ItemOverrideList overrides, ResourceLocation modelLocation) {
		// Bake all of our submodels
		bakeSubmodels(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
		// Construct the baked model
		// Make sure to give it our custom override handler so it can switch models
		// Since this model is never really going to be rendered most of the arguments
		// here are arbitrary
		// We could make a class like "DummyBakedItemModel" if we wanted to avoid all
		// the arguments
		ItemOverrideList overrideHandler = new OverrideHandler();
		return new BakedItemModel(ImmutableList.of(), null, ImmutableMap.of(), overrideHandler, true, true);
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		// Get the textures used by each submodel and combine them into one set
		Set<Material> textures = new HashSet<>();
		for (SubModel subModel : subModels) {
			textures.addAll(subModel.getTextures(owner, modelGetter, missingTextureErrors));
		}
		return textures;
	}

}