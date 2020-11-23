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
import spinyq.spinytextiles.tiles.BasinTile.BasinStateVisitor;
import spinyq.spinytextiles.tiles.BasinTile.BleachState;
import spinyq.spinytextiles.tiles.BasinTile.DyeState;
import spinyq.spinytextiles.tiles.BasinTile.FilledState;
import spinyq.spinytextiles.utility.color.RGBAColor;
import spinyq.spinytextiles.utility.color.RGBColor;

@OnlyIn(Dist.CLIENT)
public class BasinRenderer extends TileEntityRenderer<BasinTile> {

	private static final RGBColor WATER_COLOR = new RGBColor().fromIntString("0x3F76E4");
	private static final int STAGES = 200;

	private static final BasinStateVisitor<RGBColor> COLOR_CALCULATOR = new BasinStateVisitor<RGBColor>() {

		@Override
		public RGBColor visit(FilledState state) {
			return WATER_COLOR;
		}

		@Override
		public RGBColor visit(DyeState state) {
			return state.getColor().toRGB(new RGBColor(), state.getSuperState().accept(this));
		}

		@Override
		public RGBColor visit(BleachState state) {
			return state.getSuperState().accept(this);
		}
		
	};
	
	private final CuboidModel[] fluidModels = new CuboidModel[STAGES];

	private CuboidModel getFluidModel(int stage, AtlasTexture texture) {
		CuboidModel model = new CuboidModel();
		// Set the model's texture
		model.setTexture(texture.getSprite(Fluids.WATER.getAttributes().getStillTexture()));
		// Set the model dimensions
		model.minX = 0.125 - .01;
		model.minY = 0.2 - .01;
		model.minZ = 0.125 - .01;

		model.maxX = 0.875 + .01;
		model.maxY = 0.2 + ((float) stage / (float) STAGES) * 0.75 + .01;
		model.maxZ = 0.875 + .01;
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
		
		BasinStateVisitor<Void> blockRenderer = new BasinStateVisitor<Void>() {

			@Override
			public Void visit(FilledState state) {
				// Calculate stage and model
				int stage = (int) Math
						.floor((float) (STAGES - 1) * (float) state.getWaterLevel() / (float) BasinTile.MAX_WATER_LEVEL);
				CuboidModel model = fluidModels[stage];
				// Calculate water color
				RGBAColor color = new RGBAColor(basin.accept(COLOR_CALCULATOR), 1.0f);
				// Allocate buffer
				IVertexBuilder buffer = renderer.getBuffer(CuboidRenderType.resizableCuboid());
				// Render model
				CuboidRenderer.INSTANCE.renderCube(model, matrixStackIn, buffer, color, combinedLightIn, combinedOverlayIn);
				return null;
			}

			@Override
			public Void visit(DyeState state) {
				return state.getSuperState().accept(this);
			}

			@Override
			public Void visit(BleachState state) {
				return state.getSuperState().accept(this);
			}
			
		};
		
		basin.accept(blockRenderer);
	}

}
