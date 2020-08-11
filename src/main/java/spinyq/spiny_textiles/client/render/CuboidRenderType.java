package spinyq.spiny_textiles.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

public class CuboidRenderType extends RenderType {

	private static final AlphaState CUBOID_ALPHA = new RenderState.AlphaState(0.1F);
	
	public CuboidRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	private static CuboidRenderType.State.Builder preset(ResourceLocation resourceLocation) {
        return CuboidRenderType.State.getBuilder()
              .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
              .cull(CULL_ENABLED)//enableCull
              .transparency(TRANSLUCENT_TRANSPARENCY)//enableBlend/blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
              .shadeModel(SHADE_ENABLED);//shadeModel(GL11.GL_SMOOTH)
    }

    @SuppressWarnings("deprecation")
    public static RenderType resizableCuboid() {
		CuboidRenderType.State.Builder stateBuilder = preset(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
              .alpha(CUBOID_ALPHA);//enableAlphaTest/alphaFunc(GL11.GL_GREATER, 0.1F)
        return makeType("resizable_cuboid", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 256, true, false,
              stateBuilder.build(true));
    }
	
}
