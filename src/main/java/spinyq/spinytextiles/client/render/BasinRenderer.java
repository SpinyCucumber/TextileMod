package spinyq.spinytextiles.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import spinyq.spinytextiles.TextileMod;
import spinyq.spinytextiles.client.render.CuboidRenderer.CuboidModel;
import spinyq.spinytextiles.tiles.BasinTile;
import spinyq.spinytextiles.utility.Color3f;
import spinyq.spinytextiles.utility.Color4f;

@OnlyIn(Dist.CLIENT)
public class BasinRenderer extends TileEntityRenderer<BasinTile> {

	private static final Color3f WATER_COLOR = Color3f.fromIntString("0x3F76E4");

	private static final int STAGES = 200;

	private final CuboidModel[] fluidModels = new CuboidModel[STAGES];

	private CuboidModel getFluidModel(int stage, AtlasTexture texture) {
		CuboidModel model = new CuboidModel();
		// Set the model's texture
		model.setTexture(texture.getSprite(Fluids.WATER.getAttributes().getStillTexture()));
		// Set the model dimensions
		model.minX = 0.125 + .01;
		model.minY = 0.2 + .01;
		model.minZ = 0.125 + .01;

		model.maxX = 0.875 - .01;
		model.maxY = 0.2 + ((float) stage / (float) STAGES) * 0.875 - .01;
		model.maxZ = 0.875 - .01;
		// Done
		return model;
	}

	private void generateFluidModels(AtlasTexture texture) {
		for (int stage = 0; stage < STAGES; stage++) {
			fluidModels[stage] = getFluidModel(stage, texture);
		}
	}

	public BasinRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		// Register ourselves to receive events.
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		TextileMod.LOGGER.info("Generating Fluid Models...");
		generateFluidModels(event.getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
	}

	@Override
	public void render(BasinTile basin, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer renderer,
			int combinedLightIn, int combinedOverlayIn) {
		// Don't render anything if basin is empty
		if (!basin.isEmpty()) {
			// Calculate stage
			int stage = (int) Math
					.floor((float) STAGES * (float) basin.getWaterLevel() / (float) BasinTile.MAX_WATER_LEVEL);
			CuboidModel model = fluidModels[stage];
			// Calculate color using dye concentration
			float conc = basin.getDyeConcentration();
			Color3f rgb = WATER_COLOR.lerp(basin.getColor(), conc);
			Color4f color = new Color4f(rgb, 1.0f);
			// Allocate buffer
			IVertexBuilder buffer = renderer.getBuffer(CuboidRenderType.resizableCuboid());
			// Render model
			CuboidRenderer.INSTANCE.renderCube(model, matrixStackIn, buffer, color, combinedLightIn, combinedOverlayIn);
		}
	}

}
