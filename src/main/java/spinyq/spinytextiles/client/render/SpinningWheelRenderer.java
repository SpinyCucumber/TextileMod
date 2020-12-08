package spinyq.spinytextiles.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import spinyq.spinytextiles.TextileMod;
import spinyq.spinytextiles.blocks.SpinningWheelBlock;
import spinyq.spinytextiles.client.render.CuboidRenderer.CuboidModel;
import spinyq.spinytextiles.tiles.SpinningWheelTile;
import spinyq.spinytextiles.tiles.SpinningWheelTile.BaseState;
import spinyq.spinytextiles.tiles.SpinningWheelTile.SpinningWheelStateVisitor;
import spinyq.spinytextiles.utility.FunctionHelper;
import spinyq.spinytextiles.utility.FunctionHelper.Result;
import spinyq.spinytextiles.utility.color.RGBAColor;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.FiberInfo;

@OnlyIn(Dist.CLIENT)
public class SpinningWheelRenderer extends TileEntityRenderer<SpinningWheelTile> {

	private CuboidModel threadModel;

	// If the wheel is spinning, interpolate between previous and current thread
	// infos to get a smooth animation.
	// Otherwise, simply use the most current thread info.
	private static final SpinningWheelStateVisitor COLOR_CALCULATOR = new SpinningWheelStateVisitor() {

		@Override
		public void visit(BaseState state) {
			FiberInfo thread = state.getCurrThread();

			RGBColor rgb = thread.color.toRGB(new RGBColor(), null);
			float alpha = (float) thread.amount / (float) SpinningWheelTile.REQUIRED_THREAD;
			throw new Result(new RGBAColor(rgb, alpha));
		}

		@Override
		public void visit(BaseState.SpinningState state) {
			// Interpolate both color and thread amount from previous and current thread information.
			// Use the time supplied by the spinning state to interpolate
			FiberInfo curr = ((BaseState) state.getSuperState()).getCurrThread(),
					prev = ((BaseState) state.getSuperState()).getPrevThread();
			float p = Math.min(1.0f, (float) state.getTime() / (float) SpinningWheelTile.SPINNING_TIME);
			RYBKColor threadColor = prev.color.interp(curr.color, p);
			float threadAmount = MathHelper.lerp(p, (float) prev.amount, (float) curr.amount);

			RGBColor rgb = threadColor.toRGB(new RGBColor(), null);
			float alpha = threadAmount / (float) SpinningWheelTile.REQUIRED_THREAD;
			throw new Result(new RGBAColor(rgb, alpha));
		}

	};

	/**
	 * Generates a thread model to display on top of the spinning wheel.
	 */
	private void generateModel(AtlasTexture texture) {
		threadModel = new CuboidModel();
		// Set the model's texture
		threadModel.setTexture(texture.getSprite(new ResourceLocation("minecraft:block/white_wool")));
		// Set the model dimensions
		// Make sure the model is centered so it rotates correctly
		threadModel.minX = 7.5 / 16.0 - 0.5;
		threadModel.minY = 2.0 / 16.0 - 0.01 - 0.5;
		threadModel.minZ = 1.0 / 16.0 - 0.01 - 0.5;

		threadModel.maxX = 8.5 / 16.0 - 0.5;
		threadModel.maxY = 16.0 / 16.0 + 0.01 - 0.5;
		threadModel.maxZ = 15.0 / 16.0 + 0.01 - 0.5;
		// Disable side faces
		threadModel.setSideRender(Direction.EAST, false);
		threadModel.setSideRender(Direction.WEST, false);
		// Done
	}

	public SpinningWheelRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		// Register ourselves to receive events.
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		TextileMod.LOGGER.info("Generating Spinning Wheel Model...");
		generateModel(event.getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
	}

	@Override
	public void render(SpinningWheelTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer renderer, int combinedLightIn, int combinedOverlayIn) {

		SpinningWheelStateVisitor spinningWheelRenderer = new SpinningWheelStateVisitor() {

			@Override
			public void visit(BaseState state) {
				// Get thread color
				RGBAColor color = FunctionHelper.getResult(() -> tileEntityIn.accept(COLOR_CALCULATOR));
				// Rotate based on blockstate
				// Also have to center model
				matrixStackIn.push();
				matrixStackIn.translate(0.5, 0.5, 0.5);
				Direction facing = tileEntityIn.getBlockState().get(SpinningWheelBlock.FACING);
				Quaternion quat = new Quaternion(Direction.UP.toVector3f(), facing.getHorizontalAngle(), true);
				matrixStackIn.rotate(quat);
				// Allocate buffer
				IVertexBuilder buffer = renderer.getBuffer(CuboidRenderType.resizableCuboid());
				// Render model
				CuboidRenderer.INSTANCE
						.renderCube(threadModel, matrixStackIn, buffer, color, combinedLightIn, combinedOverlayIn);
				// Undo rotation
				matrixStackIn.pop();
			}

		};

		tileEntityIn.accept(spinningWheelRenderer);

	}
}
