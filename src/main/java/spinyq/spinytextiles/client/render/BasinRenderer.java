package spinyq.spinytextiles.client.render;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import spinyq.spinytextiles.client.render.CuboidModelNew.BakedCuboid;
import spinyq.spinytextiles.tiles.BasinTile;
import spinyq.spinytextiles.tiles.BasinTile.BasinStateVisitor;
import spinyq.spinytextiles.tiles.BasinTile.FilledState;
import spinyq.spinytextiles.utility.FunctionHelper;
import spinyq.spinytextiles.utility.FunctionHelper.Result;
import spinyq.spinytextiles.utility.color.RGBColor;

@OnlyIn(Dist.CLIENT)
public class BasinRenderer extends TileEntityRenderer<BasinTile> {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final RGBColor WATER_COLOR = new RGBColor().fromIntString("0x3F76E4");
	private static final int STAGES = 200;

	private static final BasinStateVisitor COLOR_CALCULATOR = new BasinStateVisitor() {

		@Override
		public void visit(FilledState state) {
			throw new Result(WATER_COLOR);
		}

		@Override
		public void visit(FilledState.DyeState state) {
			throw new Result(state.getColor().toRGB(new RGBColor(), WATER_COLOR));
		}

	};
	
	private final BakedCuboid[] fluidModels = new BakedCuboid[STAGES];

	@SuppressWarnings("deprecation")
	private CuboidModelNew getFluidModel(int stage) {
		CuboidModelNew model = new CuboidModelNew();
		// Set the model's texture
		model.setTexture(new Material(
				AtlasTexture.LOCATION_BLOCKS_TEXTURE,
				Fluids.WATER.getAttributes().getStillTexture()));
		// Set the model dimensions
		model.minX = 0.125f - .01f;
		model.minY = 0.2f - .01f;
		model.minZ = 0.125f - .01f;

		model.maxX = 0.875f + .01f;
		model.maxY = 0.2f + ((float) stage / (float) STAGES) * 0.75f + .01f;
		model.maxZ = 0.875f + .01f;
		// Done
		return model;
	}

	private void bakeFluidModels() {
		for (int stage = 0; stage < STAGES; stage++) {
			CuboidModelNew model = getFluidModel(stage);
			fluidModels[stage] = model.bake(TransformationMatrix.identity());
		}
	}

	public BasinRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		// Register ourselves to receive events.
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		LOGGER.info("Generating Fluid Models...");
		bakeFluidModels();
	}

	@Override
	public void render(BasinTile basin, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer renderer,
			int combinedLightIn, int combinedOverlayIn) {
		
		BasinStateVisitor blockRenderer = new BasinStateVisitor() {

			@Override
			public void visit(FilledState state) {
				// Calculate stage and model
				int stage = (int) Math
						.floor((float) (STAGES - 1) * (float) state.getWaterLevel() / (float) BasinTile.MAX_WATER_LEVEL);
				BakedCuboid model = fluidModels[stage];
				// Calculate water color
				RGBColor color = FunctionHelper.getResult(() -> basin.accept(COLOR_CALCULATOR));
				// Allocate buffer
				IVertexBuilder buffer = renderer.getBuffer(CuboidRenderType.resizableCuboid());
				// Render model
				model.render(matrixStackIn, buffer, color, combinedLightIn, combinedOverlayIn);
			}
			
		};
		
		basin.accept(blockRenderer);
	}

}
