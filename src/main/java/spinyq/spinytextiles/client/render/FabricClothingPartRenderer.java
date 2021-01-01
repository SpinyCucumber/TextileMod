package spinyq.spinytextiles.client.render;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import spinyq.spinytextiles.client.render.ClothingRenderer.IClothingPartRenderer;
import spinyq.spinytextiles.utility.textile.clothing.FabricClothingPart;
import spinyq.spinytextiles.utility.textile.clothing.IClothing;

@EventBusSubscriber(bus = Bus.MOD)
public class FabricClothingPartRenderer implements IClothingPartRenderer<FabricClothingPart> {

	private Map<FabricClothingPart, IBakedModel> bakedModels = new HashMap<>();
	private Map<FabricClothingPart, BlockModel> unbakedModels = new HashMap<>();
	
	private FabricClothingPartRenderer() {
		// Hook up event handlers
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		bakeModels(event.getModelLoader());
	}

	@Override
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn,
			FabricClothingPart part, IClothing clothing) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadResources(IResourceManager resourceManager, FabricClothingPart part) throws IOException {
		// Load the model of the fabric clothing part
		ResourceLocation modelLocation = getModelLocation(part);
		IResource resource = resourceManager.getResource(modelLocation);
		Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
		// Load model
		unbakedModels.put(part, BlockModel.deserialize(reader));
	}
	
	@SuppressWarnings("resource")
	private void bakeModels(ModelBakery bakery) {
		// For each unbaked model, bake it
		for (Entry<FabricClothingPart, BlockModel> entry : unbakedModels.entrySet()) {
			BlockModel model = entry.getValue();
			ResourceLocation modelLocation = getModelLocation(entry.getKey());
			IBakedModel bakedModel = model.bakeModel(bakery, model, bakery.getSpriteMap()::getSprite, SimpleModelTransform.IDENTITY, modelLocation, true);
			bakedModels.put(entry.getKey(), bakedModel);
		}
	}
	
	private ResourceLocation getModelLocation(FabricClothingPart part) {
		ResourceLocation id = part.getRegistryName();
		return new ResourceLocation(id.getNamespace(), "models/clothingpart/" + id.getPath() + ".json");
	}

}
