package spinyq.spinytextiles.client.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ModelRenderer;
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
	public enum BodyPart {

		HEAD {
			@Override
			public ModelRenderer getBone(BipedModel<?> model) {
				return model.bipedHead;
			}
		},
		TORSO {
			@Override
			public ModelRenderer getBone(BipedModel<?> model) {
				return model.bipedBody;
			}
		},
		LEFT_ARM {
			@Override
			public ModelRenderer getBone(BipedModel<?> model) {
				return model.bipedLeftArm;
			}
		},
		RIGHT_ARM {
			@Override
			public ModelRenderer getBone(BipedModel<?> model) {
				return model.bipedRightArm;
			}
		},
		LEFT_LEG {
			@Override
			public ModelRenderer getBone(BipedModel<?> model) {
				return model.bipedLeftLeg;
			}
		},
		RIGHT_LEG {
			@Override
			public ModelRenderer getBone(BipedModel<?> model) {
				return model.bipedRightLeg;
			}
		};

		public abstract ModelRenderer getBone(BipedModel<?> model);

	}

	@OnlyIn(Dist.CLIENT)
	public interface IClothingPartRenderer<T extends ClothingPart> {
		
		Stream<BakedQuad> getQuads(T clothingPart, IClothing clothing, BodyPart bodyPart);
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
	public void renderClothing(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, IClothing clothing, BipedModel skeleton) {
		// For each body part that clothing can be rendered on,
		// let the clothing part renderers do their thing
		for (BodyPart bodyPart : BodyPart.values()) {
			// Get the skeleton bone that the bodyPart corresponds to
			// and apply transforms
			ModelRenderer bone = bodyPart.getBone(skeleton);
			matrixStackIn.push();
			bone.translateRotate(matrixStackIn);
			// Render each part of the clothing piece
			
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends ClothingPart> void renderClothingPart(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T part, IClothing clothing) {
		IClothingPartRenderer<T> renderer = (IClothingPartRenderer<T>) partRenderers.get(part.getClass());
		if (renderer != null) {
			// TODO
		}
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

}
