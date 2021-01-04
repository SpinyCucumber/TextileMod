package spinyq.spinytextiles.client.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.clothing.ClothingPart;
import spinyq.spinytextiles.utility.textile.clothing.FabricClothingPart;
import spinyq.spinytextiles.utility.textile.clothing.IClothing;

@OnlyIn(Dist.CLIENT)
public class ClothingRenderer implements IFutureReloadListener {

	@OnlyIn(Dist.CLIENT)
	public interface IClothingPartRenderer<T extends ClothingPart> {
		
		void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn,
				T clothingPart, IClothing clothing, BipedModel<?> skeleton);
		void loadResources(IResourceManager resourceManager, T part) throws IOException;
		
	}
	
	private static final IForgeRegistry<ClothingPart> PART_REGISTRY = LazyForgeRegistry.of(ClothingPart.class);
	public static final ClothingRenderer INSTANCE = new ClothingRenderer();
	
	private final Map<Class<?>, IClothingPartRenderer<?>> partRenderers = new HashMap<>();
	
	public ClothingRenderer() {
		// Register built-in part renderers when we are first constructed
		registerBuiltinRenderers();
		// Hook up event handlers
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	
	public <T extends ClothingPart> void registerPartRenderer(Class<T> clazz, IClothingPartRenderer<T> renderer) {
		partRenderers.put(clazz, renderer);
	}
	
	/**
	 * Renders a piece of clothing.
	 * Clothing is rendered using a BipedModel, to allow the piece of clothing
	 * to take different poses.
	 * @param matrixStackIn
	 * @param bufferIn
	 * @param packedLightIn
	 * @param clothing
	 * @param skeleton
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void renderClothing(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, IClothing clothing, BipedModel<?> skeleton) {
		// Render each clothing part that makes up the clothing piece
		clothing.getPattern().getPartStream().forEach((clothingPart) -> {
			// Try to look up a clothing part renderer
			IClothingPartRenderer renderer = partRenderers.get(clothingPart.getClass());
			// If it exists, pass the control over to the renderer
			if (renderer != null) renderer.render(matrixStackIn, bufferIn, packedLightIn, clothingPart, clothing, skeleton); 
		});
	}

	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager,
			IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor,
			Executor gameExecutor) {
		// File IO can run off-thread, so this can be ran asynchronously.
		return CompletableFuture.runAsync(() -> {
			// For each clothing part, let its renderer handle loading resources.
			for (ClothingPart part : PART_REGISTRY.getValues()) {
				loadClothingPart(resourceManager, part);
			}
		}, backgroundExecutor)
				// Mark that we are finished with our background tasks
				.thenCompose(stage::markCompleteAwaitingOthers);
	}

	@SubscribeEvent
	public void onCommonSetup(FMLCommonSetupEvent event) {
		// Let Minecraft know we manage resources
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this);
	}

	@SuppressWarnings("unchecked")
	private <T extends ClothingPart> void loadClothingPart(IResourceManager resourceManager, T part) {
		IClothingPartRenderer<T> renderer = (IClothingPartRenderer<T>) partRenderers.get(part.getClass());
		if (renderer != null) {
			try {
				renderer.loadResources(resourceManager, part);
			} catch (IOException e) {
				throw new RuntimeException("Error while loading clothing part: " + part.getRegistryName(), e);
			}
		}
	}

	private void registerBuiltinRenderers() {
		registerPartRenderer(FabricClothingPart.class, new FabricClothingPartRenderer());
	}

}
