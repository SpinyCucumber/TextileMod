package spinyq.spinytextiles.client.render;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(bus = Bus.MOD)
public class ClothingLayer<T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {

	private static final Logger LOGGER = LogManager.getLogger();
	
	// This handles attaching the ClotherLayer to all of the player renderers.
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		// Retrieve the entity renderer manager
		EntityRendererManager renderManager = event.getMinecraftSupplier().get().getRenderManager();
		// For each player renderer, attach a new clothing layer
		for (PlayerRenderer renderer : renderManager.getSkinMap().values()) {
			renderer.addLayer(new ClothingLayer<>(renderer));
		}
	}
	
	public ClothingLayer(IEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {
		LOGGER.info("Rendering!");
	}

}
