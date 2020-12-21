package spinyq.spinytextiles.client.model;

import java.util.Collection;
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
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.ModelTransformComposition;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;
import spinyq.spinytextiles.TextileMod;
import spinyq.spinytextiles.items.FabricItem;
import spinyq.spinytextiles.utility.textile.Fabric;

// ItemCameraTransform is deprecated but is still being used by BakedItemModel, so we are forced to
// use it as well.
@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public final class FabricItemModel implements IModelGeometry<FabricItemModel> {

	private static final Logger LOGGER = LogManager.getLogger();
	
	// minimal Z offset to prevent depth-fighting
	private static final float Z_OFFSET = 0.02f;
	private static final String MASK_TEXTURE = "mask";

	private final Fabric fabric;

	public FabricItemModel(Fabric fabric) {
		this.fabric = fabric;
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
		Material maskLocation = owner.resolveTexture(MASK_TEXTURE);

		IModelTransform transformsFromModel = owner.getCombinedTransform();
		ImmutableMap<TransformType, TransformationMatrix> transformMap = transformsFromModel != null
				? PerspectiveMapWrapper
						.getTransforms(new ModelTransformComposition(transformsFromModel, modelTransform))
				: PerspectiveMapWrapper.getTransforms(modelTransform);
		TransformationMatrix transform = modelTransform.getRotation();

		LOGGER.info("Baking a FabricItemModel for fabric: {} with mask texture: {}", fabric, maskLocation);
		
		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		// If our fabric info is non-null, convert it into quads
		// Get a list of layers and generate quads for each
		if (fabric != null) {
			List<Material> textures = FabricTextureManager.INSTANCE.getTextureList(fabric);
			LOGGER.info("Textures: {}", textures);
			float z = 0.0f;
			TextureAtlasSprite maskSprite = spriteGetter.apply(maskLocation);
			for (Material texture : textures) {
				TextureAtlasSprite sprite = spriteGetter.apply(texture);
				// Add the quads
				// Use white color
				builder.addAll(ItemTextureQuadConverter.convertTexture(transform, maskSprite, sprite, z, Direction.SOUTH,
						0xffffffff, 1));
				// Increase the depth for each layer
				z += Z_OFFSET;
			}
		}

		return new BakedModel(bakery, owner, builder.build(), null, Maps.immutableEnumMap(transformMap),
				Maps.newHashMap(), transform.isIdentity(), modelTransform, owner.isSideLit());
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		Set<Material> texs = new HashSet<>();

		texs.add(owner.resolveTexture(MASK_TEXTURE));
		if (fabric != null) texs.addAll(FabricTextureManager.INSTANCE.getTextures(fabric));

		return texs;
	}
	
	@EventBusSubscriber(bus = Bus.MOD)
	public static class Loader implements IModelLoader<FabricItemModel> {
		
		public static final ResourceLocation ID = new ResourceLocation(TextileMod.MODID, "fabric_item_model");
		
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
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
			// Construct a new, empty fabric item model
			return new FabricItemModel(null);
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
			// If it is, get the model corresponding to the fabric.
			// We cache models so that we don't have to bake them each time.
			if (stack.getItem() instanceof FabricItem) {
				FabricItem item = (FabricItem) stack.getItem();
				BakedModel model = (BakedModel) originalModel;
				// Get the fabric
				Fabric fabric = item.getFabric(stack);
				// Check if fabric is in cache
				// If it isn't, we have to create it
				if (model.cache.containsKey(fabric)) {
					return model.cache.get(fabric);
				} else {
					// Create a new unbaked model with the fabric then bake it
					// Override id is arbitrary
					IBakedModel bakedModel = new FabricItemModel(fabric).bake(model.owner, bakery,
							ModelLoader.defaultTextureGetter(), model.originalTransform, model.getOverrides(),
							new ResourceLocation(TextileMod.MODID, "fabric_item_override"));
					model.cache.put(fabric, bakedModel);
					return bakedModel;
				}
			}
			// If the item is not a fabric item simply return the original model
			return originalModel;
		}
	}

	private static final class BakedModel extends BakedItemModel {
		
		private final IModelConfiguration owner;
		private final Map<Fabric, IBakedModel> cache; // contains all the baked models since they'll never change
		private final IModelTransform originalTransform;

		BakedModel(ModelBakery bakery, IModelConfiguration owner, ImmutableList<BakedQuad> quads,
				TextureAtlasSprite particle, ImmutableMap<TransformType, TransformationMatrix> transforms,
				Map<Fabric, IBakedModel> cache, boolean untransformed, IModelTransform originalTransform,
				boolean isSideLit) {
			super(quads, particle, transforms, new OverrideHandler(bakery), untransformed, isSideLit);
			this.owner = owner;
			this.cache = cache;
			this.originalTransform = originalTransform;
		}
	}

}