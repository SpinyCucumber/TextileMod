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
import spinyq.spinytextiles.client.model.CuboidModel;
import spinyq.spinytextiles.client.model.CuboidModel.BakedCuboid;
import spinyq.spinytextiles.client.model.CuboidModel.UVList;
import spinyq.spinytextiles.tiles.SpinningWheelTile;
import spinyq.spinytextiles.tiles.SpinningWheelTile.BaseState;
import spinyq.spinytextiles.tiles.SpinningWheelTile.BaseState.SpinningState;
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
	private static final Material TEXTURE = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
			new ResourceLocation("spinytextiles:block/spinning_wheel")),
			TEXTURE_SPINNING = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
					new ResourceLocation("spinytextiles:block/spinning_wheel_spinning"));
	
	private static final Direction[] THREAD_MODEL_SIDES = new Direction[] { Direction.DOWN, Direction.UP,
			Direction.SOUTH, Direction.NORTH };

	private BakedCuboid threadModel, threadModelSpinning;

	// If the wheel is spinning, interpolate between previous and current threads to
	// get a smooth animation.
	// Otherwise, simply use the most current thread info.
	private final SpinningWheelStateVisitor colorCalculator = new SpinningWheelStateVisitor() {

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
	
	private final SpinningWheelStateVisitor threadModelGetter = new SpinningWheelStateVisitor() {

		@Override
		public void visit(BaseState state) {
			throw new Result(threadModel);
		}

		@Override
		public void visit(SpinningState state) {
			throw new Result(threadModelSpinning);
		}
		
	};

	/**
	 * Creates a thread model to display on top of the spinning wheel.
	 */
	private BakedCuboid bakeThreadModel(Material texture) {
		// Create a cuboid model
		CuboidModel cuboid = new CuboidModel();
		// Set the texture of the sides
		// Also set the UV of each side
		UVList uv = new UVList(45f, 0f, 46f, 14f);
		for (Direction side : THREAD_MODEL_SIDES) {
			cuboid.getFace(side).setTexture(texture);
			cuboid.getFace(side).setUV(uv);
		}
		// Set the model dimensions
		// Minimal offset to prevent depth-fighting with spinning wheel model
		cuboid.setFromPosition(new Vector3f(7.5f / 16f, 2f / 16f - 0.0002f, 1f / 16f - 0.0002f));
		cuboid.setToPosition(new Vector3f(8.5f / 16f, 16f / 16f + 0.0002f, 15f / 16f + 0.0002f));
		// Finally, bake the model
		// Make sure the model is centered so it rotates correctly
		return cuboid.bake(new TransformationMatrix(new Vector3f(-0.5f, -0.5f, -0.5f), null, null, null));
	}

	public SpinningWheelRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		// Register ourselves to receive events.
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		LOGGER.info("Baking Spinning Wheel Thread Models...");
		threadModel = bakeThreadModel(TEXTURE);
		threadModelSpinning = bakeThreadModel(TEXTURE_SPINNING);
	}

	@Override
	public void render(SpinningWheelTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer renderer, int combinedLightIn, int combinedOverlayIn) {

		SpinningWheelStateVisitor spinningWheelRenderer = new SpinningWheelStateVisitor() {

			@Override
			public void visit(BaseState state) {
				// Get thread color
				RGBAColor color = FunctionHelper.getResult(() -> tileEntityIn.accept(colorCalculator));
				// Rotate based on blockstate
				// Also have to center model
				matrixStackIn.push();
				matrixStackIn.translate(0.5, 0.5, 0.5);
				Direction facing = tileEntityIn.getBlockState().get(SpinningWheelBlock.FACING);
				Quaternion quat = new Quaternion(Direction.UP.toVector3f(), facing.getHorizontalAngle(), true);
				matrixStackIn.rotate(quat);
				// Get the thread model based on current state
				BakedCuboid model = FunctionHelper.getResult(() -> tileEntityIn.accept(threadModelGetter));
				// Allocate buffer and render model
				IVertexBuilder buffer = renderer.getBuffer(CuboidRenderType.resizableCuboid());
				model.render(buffer, matrixStackIn, color, combinedLightIn, combinedOverlayIn);
				// Undo rotation
				matrixStackIn.pop();
			}

		};

		tileEntityIn.accept(spinningWheelRenderer);

	}
}
