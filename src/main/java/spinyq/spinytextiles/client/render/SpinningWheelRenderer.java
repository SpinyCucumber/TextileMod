package spinyq.spinytextiles.client.render;

import java.util.Optional;
import java.util.Stack;

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
import spinyq.spinytextiles.utility.FiberInfo;
import spinyq.spinytextiles.utility.color.RGBAColor;
import spinyq.spinytextiles.utility.color.RGBColor;
import spinyq.spinytextiles.utility.color.RYBKColor;

@OnlyIn(Dist.CLIENT)
public class SpinningWheelRenderer extends TileEntityRenderer<SpinningWheelTile> {

	private CuboidModel threadModel;

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
		TextileMod.LOGGER.info("Generating Fluid Models...");
		generateModel(event.getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
	}

	@Override
	public void render(SpinningWheelTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer renderer, int combinedLightIn, int combinedOverlayIn) {
		// TODO Add animation
		// Render thread if we have some
		if (tileEntityIn.hasThread()) {
			Stack<FiberInfo> threadInfo = tileEntityIn.getThreadInfo();
			FiberInfo prev = threadInfo.elementAt(threadInfo.size() - 2), curr = threadInfo.peek();
			// Construct the info used to render the thread
			RYBKColor threadColor;
			float threadAmount;
			// If the wheel is spinning, interpolate between previous and current thread
			// infos to get a smooth animation.
			// Otherwise, simply use the most current thread info.
			if (tileEntityIn.isSpinning()) {
				float s = Math.min((float) tileEntityIn.getSpinningTimer() / (float) SpinningWheelTile.SPINNING_TIME,
						1.0f);
				threadColor = prev.color.interp(curr.color, s);
				threadAmount = MathHelper.lerp(s, (float) prev.amount, (float) curr.amount);
			} else {
				threadColor = curr.color;
				threadAmount = curr.amount;
			}
			// Calculate the final color
			// TODO Cache this
			RGBAColor color = new RGBAColor(threadColor.toRGB(new RGBColor(), Optional.empty()), threadAmount / (float) SpinningWheelTile.REQUIRED_THREAD);
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
			CuboidRenderer.INSTANCE.renderCube(threadModel, matrixStackIn, buffer, color, combinedLightIn, combinedOverlayIn);
			// Undo rotation
			matrixStackIn.pop();
		}
	}
}
