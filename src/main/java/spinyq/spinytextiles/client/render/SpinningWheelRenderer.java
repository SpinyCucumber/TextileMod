package spinyq.spinytextiles.client.render;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Material;
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
import spinyq.spinytextiles.blocks.SpinningWheelBlock;
import spinyq.spinytextiles.client.render.CuboidModelNew.BakedCuboid;
import spinyq.spinytextiles.tiles.SpinningWheelTile;
import spinyq.spinytextiles.tiles.SpinningWheelTile.BaseState;
import spinyq.spinytextiles.tiles.SpinningWheelTile.SpinningWheelStateVisitor;
import spinyq.spinytextiles.utility.FunctionHelper;
import spinyq.spinytextiles.utility.FunctionHelper.Result;
import spinyq.spinytextiles.utility.color.RGBAColor;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.Fiber;

@OnlyIn(Dist.CLIENT)
public class SpinningWheelRenderer extends TileEntityRenderer<SpinningWheelTile> {

	private static final Logger LOGGER = LogManager.getLogger();
	@SuppressWarnings("deprecation")
	private static final Material THREAD_TEXTURE = new Material(
			AtlasTexture.LOCATION_BLOCKS_TEXTURE,
			new ResourceLocation("minecraft:block/white_wool"));
	
	private BakedCuboid threadModel;

	// If the wheel is spinning, interpolate between previous and current threads to get a smooth animation.
	// Otherwise, simply use the most current thread info.
	private static final SpinningWheelStateVisitor COLOR_CALCULATOR = new SpinningWheelStateVisitor() {

		@Override
		public void visit(BaseState state) {
			Fiber thread = state.getCurrThread();

			RGBColor rgb = thread.color.toRGB(new RGBColor(), null);
			float alpha = (float) thread.amount / (float) SpinningWheelTile.REQUIRED_THREAD;
			throw new Result(new RGBAColor(rgb, alpha));
		}

		@Override
		public void visit(BaseState.SpinningState state) {
			// Interpolate both color and thread amount from previous and current thread.
			// Use the time supplied by the spinning state to interpolate
			Fiber curr = ((BaseState) state.getSuperState()).getCurrThread(),
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
	private void generateModel() {
		// Create a cuboid model
		CuboidModelNew cuboid = new CuboidModelNew();
		// Set the texture of certain sides
		cuboid.setSideTexture(Direction.DOWN, THREAD_TEXTURE);
		cuboid.setSideTexture(Direction.UP, THREAD_TEXTURE);
		cuboid.setSideTexture(Direction.SOUTH, THREAD_TEXTURE);
		cuboid.setSideTexture(Direction.NORTH, THREAD_TEXTURE);
		// Set the model dimensions
		cuboid.positionFrom.setX(7.5f / 16f);
		cuboid.positionFrom.setY(2f / 16f - 0.01f);
		cuboid.positionFrom.setZ(1f / 16f - 0.01f);

		cuboid.positionTo.setX(8.5f / 16f);
		cuboid.positionTo.setY(16f / 16f + 0.01f);
		cuboid.positionTo.setZ(15f / 16f + 0.01f);
		// Finally, bake the model
		// Make sure the model is centered so it rotates correctly
		threadModel = cuboid.bake(new TransformationMatrix(new Vector3f(-0.5f,-0.5f,-0.5f), null, null, null));
		// Done
	}

	public SpinningWheelRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		// Register ourselves to receive events.
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		LOGGER.info("Baking Spinning Wheel Model...");
		generateModel();
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
				threadModel.render(buffer, matrixStackIn, color, combinedLightIn, combinedOverlayIn);
				// Undo rotation
				matrixStackIn.pop();
			}

		};

		tileEntityIn.accept(spinningWheelRenderer);

	}
}
