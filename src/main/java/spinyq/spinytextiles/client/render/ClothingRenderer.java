package spinyq.spinytextiles.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.clothing.ClothingPart;
import spinyq.spinytextiles.utility.textile.clothing.IClothing;

public class ClothingRenderer implements IFutureReloadListener {

	public interface IClothingPartRenderer<T extends ClothingPart> {
		
		void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T part, IClothing clothing);
		void loadResources(T part);
		
	}
	
	private static final IForgeRegistry<ClothingPart> PART_REGISTRY = LazyForgeRegistry.of(ClothingPart.class);
	public static final ClothingRenderer INSTANCE = new ClothingRenderer();
	
	private final Map<Class<?>, IClothingPartRenderer<?>> PART_RENDERERS = new HashMap<>();
	
	public <T extends ClothingPart> void registerPartRenderer(Class<T> clazz, IClothingPartRenderer<T> renderer) {
		PART_RENDERERS.put(clazz, renderer);
	}
	
	public void renderClothing(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, IClothing clothing) {
		// For each part in the clothing pattern, look up the part renderer
		// and let the part renderer handle rendering the clothing part
		clothing.getPattern().getPartStream().forEach((part) -> {
			renderClothingPart(matrixStackIn, bufferIn, packedLightIn, part, clothing);
		});
	}
	
	@SuppressWarnings("unchecked")
	private <T extends ClothingPart> void renderClothingPart(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T part, IClothing clothing) {
		IClothingPartRenderer<T> renderer = (IClothingPartRenderer<T>) PART_RENDERERS.get(part.getClass());
		if (renderer != null) renderer.render(matrixStackIn, bufferIn, packedLightIn, part, clothing);
	}

	@SuppressWarnings("unchecked")
	private <T extends ClothingPart> void loadClothingPart(T part) {
		IClothingPartRenderer<T> renderer = (IClothingPartRenderer<T>) PART_RENDERERS.get(part.getClass());
		if (renderer != null) renderer.loadResources(part);
	}
	
	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager,
			IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor,
			Executor gameExecutor) {
		// File IO can run off-thread, so this can be ran asynchronously.
		return CompletableFuture.runAsync(() -> {
			// For each clothing part, let its renderer handle loading resources.
			for (ClothingPart part : PART_REGISTRY.getValues()) {
				loadClothingPart(part);
			}
		}, backgroundExecutor)
				// Mark that we are finished with our background tasks
				.thenCompose(stage::markCompleteAwaitingOthers);
	}

	// Called when the mod is first constructed
	public void onModConstructed() {
		// Let Minecraft know we manage resources
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this);
	}

}
