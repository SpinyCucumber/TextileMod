package spinyq.spinytextiles.client.render;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import spinyq.spinytextiles.client.render.ClothingRenderer.IClothingPartRenderer;
import spinyq.spinytextiles.utility.textile.clothing.FabricClothingPart;
import spinyq.spinytextiles.utility.textile.clothing.IClothing;

public class FabricClothingPartRenderer implements IClothingPartRenderer<FabricClothingPart> {

	private Map<FabricClothingPart, IBakedModel> bakedModels = new HashMap<>();
	
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
		// TODO Load model
		BlockModel model = BlockModel.deserialize(reader);
	}
	
	private ResourceLocation getModelLocation(FabricClothingPart part) {
		ResourceLocation id = part.getRegistryName();
		return new ResourceLocation(id.getNamespace(), "models/clothingpart/" + id.getPath() + ".json");
	}

}
