package spinyq.spinytextiles.client.render;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Stream;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import spinyq.spinytextiles.client.render.ClothingRenderer.BodyPart;
import spinyq.spinytextiles.client.render.ClothingRenderer.IClothingPartRenderer;
import spinyq.spinytextiles.utility.textile.clothing.FabricClothingPart;
import spinyq.spinytextiles.utility.textile.clothing.IClothing;

// TODO Rewrite without using BlockModel
@OnlyIn(Dist.CLIENT)
public class FabricClothingPartRenderer implements IClothingPartRenderer<FabricClothingPart> {

	private Map<FabricClothingPart, EnumMap<BodyPart, IBakedModel>> bakedModels = new HashMap<>();
	private Map<FabricClothingPart, EnumMap<BodyPart, BlockModel>> unbakedModels = new HashMap<>();
	private final Random random = new Random();
	
	public FabricClothingPartRenderer() {
		// Hook up event handlers
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		bakeModels(event.getModelLoader());
	}

	@Override
	public Stream<BakedQuad> getQuads(FabricClothingPart clothingPart, IClothing clothing, BodyPart bodyPart) {
		EnumMap<BodyPart, IBakedModel> bodyPartMap = bakedModels.get(clothingPart);
		if (bodyPartMap != null) {
			IBakedModel model = bodyPartMap.get(bodyPart);
			if (model != null) {
				return model.getQuads(null, null, random, EmptyModelData.INSTANCE).stream();
			}
		}
		return Stream.empty();
	}

	@Override
	public void loadResources(IResourceManager resourceManager, FabricClothingPart clothingPart) throws IOException {
		// Load the model of the fabric clothing part
		EnumMap<BodyPart, BlockModel> bodyPartMap = new EnumMap<>(BodyPart.class);
		for (BodyPart bodyPart : BodyPart.values()) {
			ResourceLocation modelLocation = getModelLocation(clothingPart, bodyPart);
			// Only load the model if it exists
			if (resourceManager.hasResource(modelLocation)) {
				IResource resource = resourceManager.getResource(modelLocation);
				Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
				// Load model
				bodyPartMap.put(bodyPart, BlockModel.deserialize(reader));
			}
		}
		// Put body part map into main map
		unbakedModels.put(clothingPart, bodyPartMap);
	}
	
	@SuppressWarnings("resource")
	private void bakeModels(ModelBakery bakery) {
		// For each unbaked model, bake it
		for (Entry<FabricClothingPart, EnumMap<BodyPart, BlockModel>> entry : unbakedModels.entrySet()) {

			EnumMap<BodyPart, IBakedModel> bodyPartMap = new EnumMap<>(BodyPart.class);
			for (Entry<BodyPart, BlockModel> subEntry : entry.getValue().entrySet()) {
				ResourceLocation modelLocation = getModelLocation(entry.getKey(), subEntry.getKey());
				BlockModel model = subEntry.getValue();
				IBakedModel bakedModel = model.bakeModel(bakery, model, bakery.getSpriteMap()::getSprite, SimpleModelTransform.IDENTITY, modelLocation, true);
				bodyPartMap.put(subEntry.getKey(), bakedModel);
			}
			
			bakedModels.put(entry.getKey(), bodyPartMap);
		}
		// Clear unbaked models to save memory?
		unbakedModels.clear();
	}
	
	private ResourceLocation getModelLocation(FabricClothingPart part, BodyPart bodyPart) {
		ResourceLocation id = part.getRegistryName();
		String bodyPartPath = bodyPart.toString().toLowerCase();
		return new ResourceLocation(id.getNamespace(), "models/clothingpart/" + id.getPath() + "_" +  bodyPartPath + ".json");
	}

}
