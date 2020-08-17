package spinyq.spinytextiles.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import spinyq.spinytextiles.tiles.BasinTile;
import spinyq.spinytextiles.utility.Color3f;
import spinyq.spinytextiles.utility.Color4f;

@OnlyIn(Dist.CLIENT)
public class BasinRenderer extends TileEntityRenderer<BasinTile> {

	private static final Color3f WATER_COLOR = Color3f.fromIntString("0x3F76E4");
	private static final ResourceLocation WATER_TEXTURE = new ResourceLocation("textures/block/water_still.png");
	private static final RenderType WATER_RENDER_TYPE = RenderType.getEntityTranslucent(WATER_TEXTURE);
	@SuppressWarnings("deprecation")
	private static final Material WATER_MATERIAL = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("block/dirt"));
	private static final int STAGES = 200;

	private final ModelRenderer[] fluidModels = new ModelRenderer[STAGES];

	private ModelRenderer getFluidModel(int stage) {
		ModelRenderer model = new ModelRenderer(16, 16, 0, 0);
		model.addBox(2.0f + 0.01f, 3.0f + .01f, 2.0f + 0.01f, 12.0f - 0.01f,
				((float) stage / (float) STAGES) * 12.0f - 0.01f, 12.0f - 0.01f);
		return model;
	}

	private void generateFluidModels() {
		for (int stage = 0; stage < STAGES; stage++) {
			fluidModels[stage] = getFluidModel(stage);
		}
	}

	public BasinRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		generateFluidModels();
	}

	@Override
	public void render(BasinTile basin, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer renderer,
			int combinedLightIn, int combinedOverlayIn) {
		// Don't render anything if basin is empty
		if (!basin.isEmpty()) {
			// Calculate stage
			int stage = (int) Math
					.floor((float) STAGES * (float) basin.getWaterLevel() / (float) BasinTile.MAX_WATER_LEVEL);
			ModelRenderer model = fluidModels[stage];
			// Calculate color using dye concentration
			float conc = basin.getDyeConcentration();
			Color3f rgb = WATER_COLOR.lerp(basin.getColor(), conc);
			Color4f color = new Color4f(rgb, 1.0f);
			// Allocate buffer
			IVertexBuilder buffer = WATER_MATERIAL.getBuffer(renderer, RenderType::getEntityTranslucent);
			// Render model
			model.render(matrixStackIn, buffer, combinedLightIn, combinedOverlayIn, color.r, color.g, color.b, color.a);
		}
	}

}
