package spinyq.spinytextiles.client.render;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import spinyq.spinytextiles.utility.textile.clothing.ClothingPart;
import spinyq.spinytextiles.utility.textile.clothing.IClothing;

public class ClothingRenderer {

	public interface IClothingPartRenderer<T extends ClothingPart> {
		
		void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T part, IClothing clothing);
		
	}
	
	private static final Map<Class<?>, IClothingPartRenderer<?>> PART_RENDERERS = new HashMap<>();
	
	public static <T extends ClothingPart> void registerPartRenderer(Class<T> clazz, IClothingPartRenderer<T> renderer) {
		PART_RENDERERS.put(clazz, renderer);
	}
	
	public static void renderClothing(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, IClothing clothing) {
		// For each part in the clothing pattern, look up the part renderer
		// and let the part renderer handle rendering the clothing part
		clothing.getPattern().getPartStream().forEach((part) -> {
			renderClothingPart(matrixStackIn, bufferIn, packedLightIn, part, clothing);
		});
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends ClothingPart> void renderClothingPart(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T part, IClothing clothing) {
		IClothingPartRenderer<T> renderer = (IClothingPartRenderer<T>) PART_RENDERERS.get(part.getClass());
		if (renderer != null) renderer.render(matrixStackIn, bufferIn, packedLightIn, part, clothing);
	}
	
//	@Override
//	public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager,
//			IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor,
//			Executor gameExecutor) {
//		// File IO can run off-thread, so this can be ran asynchronously.
//		return CompletableFuture.runAsync(() -> {
//			// TODO Load clothing models
//		}, backgroundExecutor)
//				// Mark that we are finished with our background tasks
//				.thenCompose(stage::markCompleteAwaitingOthers);
//	}
//
//	// Called when the mod is first constructed
//	public void onModConstructed() {
//		// Let Minecraft know we manage resources
//		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this);
//	}

}
