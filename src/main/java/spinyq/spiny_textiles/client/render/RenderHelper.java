package spinyq.spiny_textiles.client.render;
import java.util.Arrays;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import spinyq.spiny_textiles.utility.Color4f;

/**
 * Adapted from Mekanism (and Buildcraft)
 */
public class RenderHelper {

	public static class CuboidModel {

        public double minX, minY, minZ;
        public double maxX, maxY, maxZ;

        public TextureAtlasSprite[] textures = new TextureAtlasSprite[6];

        public boolean[] renderSides = new boolean[]{true, true, true, true, true, true, false};

        public double sizeX() {
            return maxX - minX;
        }

        public double sizeY() {
            return maxY - minY;
        }

        public double sizeZ() {
            return maxZ - minZ;
        }

        public void setSideRender(Direction side, boolean value) {
            renderSides[side.ordinal()] = value;
        }

        public boolean shouldSideRender(Direction side) {
            return renderSides[side.ordinal()];
        }

        public void setTexture(TextureAtlasSprite tex) {
            Arrays.fill(textures, tex);
        }

        public void setTextures(TextureAtlasSprite down, TextureAtlasSprite up, TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite west, TextureAtlasSprite east) {
            textures[0] = down;
            textures[1] = up;
            textures[2] = north;
            textures[3] = south;
            textures[4] = west;
            textures[5] = east;
        }
    }
	
    public static final RenderHelper INSTANCE = new RenderHelper();
    private static final Vector3f VEC_ZERO = new Vector3f(0, 0, 0);
    private static final int U_MIN = 0;
    private static final int U_MAX = 1;
    private static final int V_MIN = 2;
    private static final int V_MAX = 3;

    protected EntityRendererManager manager = Minecraft.getInstance().getRenderManager();

    private static Vector3f withValue(Vector3f vector, Axis axis, float value) {
        if (axis == Axis.X) {
            return new Vector3f(value, vector.getY(), vector.getZ());
        } else if (axis == Axis.Y) {
            return new Vector3f(vector.getX(), value, vector.getZ());
        } else if (axis == Axis.Z) {
            return new Vector3f(vector.getX(), vector.getY(), value);
        }
        throw new RuntimeException("Was given a null axis! That was probably not intentional, consider this a bug! (Vector = " + vector + ")");
    }

    public static double getValue(Vec3d vector, Axis axis) {
        if (axis == Axis.X) {
            return vector.x;
        } else if (axis == Axis.Y) {
            return vector.y;
        } else if (axis == Axis.Z) {
            return vector.z;
        }
        throw new RuntimeException("Was given a null axis! That was probably not intentional, consider this a bug! (Vector = " + vector + ")");
    }
    
    @SuppressWarnings("deprecation")
	public static TextureAtlasSprite getSprite(ResourceLocation spriteLocation) {
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(spriteLocation);
    }

    public void renderCube(CuboidModel cube, MatrixStack matrix, IVertexBuilder buffer, Color4f color, int light) {
        float red = color.getR(), green = color.getG(), blue = color.getB(), alpha = color.getA();
        Vec3d size = new Vec3d(cube.sizeX(), cube.sizeY(), cube.sizeZ());
        matrix.push();
        matrix.translate(cube.minX, cube.minY, cube.minZ);
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        for (Direction face : Direction.values()) {
            if (cube.shouldSideRender(face)) {
                int ordinal = face.ordinal();
                TextureAtlasSprite sprite = cube.textures[ordinal];
                if (sprite != null) {
                    Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
                    Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;
                    float other = face.getAxisDirection() == AxisDirection.POSITIVE ? (float) getValue(size, face.getAxis()) : 0;

                    //Swap the face if this is positive: the renderer returns indexes that ALWAYS are for the negative face, so light it properly this way
                    face = face.getAxisDirection() == AxisDirection.NEGATIVE ? face : face.getOpposite();
                    Direction opposite = face.getOpposite();

                    float minU = sprite.getMinU();
                    float maxU = sprite.getMaxU();
                    //Flip the v
                    float minV = sprite.getMaxV();
                    float maxV = sprite.getMinV();
                    double sizeU = getValue(size, u);
                    double sizeV = getValue(size, v);
                    //TODO: Look into this more, as it makes tiling of multiple objects not render properly if they don't fit the full texture.
                    // Example: Mechanical pipes rendering water or lava, makes it relatively easy to see the texture artifacts
                    for (int uIndex = 0; uIndex < sizeU; uIndex++) {
                        float[] baseUV = new float[]{minU, maxU, minV, maxV};
                        double addU = 1;
                        // If the size of the texture is greater than the cuboid goes on for then make sure the texture positions are lowered
                        if (uIndex + addU > sizeU) {
                            addU = sizeU - uIndex;
                            baseUV[U_MAX] = baseUV[U_MIN] + (baseUV[U_MAX] - baseUV[U_MIN]) * (float) addU;
                        }
                        for (int vIndex = 0; vIndex < sizeV; vIndex++) {
                            float[] uv = Arrays.copyOf(baseUV, 4);
                            double addV = 1;
                            if (vIndex + addV > sizeV) {
                                addV = sizeV - vIndex;
                                uv[V_MAX] = uv[V_MIN] + (uv[V_MAX] - uv[V_MIN]) * (float) addV;
                            }
                            float[] xyz = new float[]{uIndex, (float) (uIndex + addU), vIndex, (float) (vIndex + addV)};

                            renderPoint(matrix4f, buffer, face, u, v, other, uv, xyz, true, false, red, green, blue, alpha, light);
                            renderPoint(matrix4f, buffer, face, u, v, other, uv, xyz, true, true, red, green, blue, alpha, light);
                            renderPoint(matrix4f, buffer, face, u, v, other, uv, xyz, false, true, red, green, blue, alpha, light);
                            renderPoint(matrix4f, buffer, face, u, v, other, uv, xyz, false, false, red, green, blue, alpha, light);

                            renderPoint(matrix4f, buffer, opposite, u, v, other, uv, xyz, false, false, red, green, blue, alpha, light);
                            renderPoint(matrix4f, buffer, opposite, u, v, other, uv, xyz, false, true, red, green, blue, alpha, light);
                            renderPoint(matrix4f, buffer, opposite, u, v, other, uv, xyz, true, true, red, green, blue, alpha, light);
                            renderPoint(matrix4f, buffer, opposite, u, v, other, uv, xyz, true, false, red, green, blue, alpha, light);
                        }
                    }
                }
            }
        }
        matrix.pop();
    }

    private void renderPoint(Matrix4f matrix4f, IVertexBuilder buffer, Direction face, Axis u, Axis v, float other, float[] uv, float[] xyz, boolean minU, boolean minV,
          float red, float green, float blue, float alpha, int light) {
        int U_ARRAY = minU ? U_MIN : U_MAX;
        int V_ARRAY = minV ? V_MIN : V_MAX;
        Vector3f vertex = withValue(VEC_ZERO, u, xyz[U_ARRAY]);
        vertex = withValue(vertex, v, xyz[V_ARRAY]);
        vertex = withValue(vertex, face.getAxis(), other);
        buffer.pos(matrix4f, vertex.getX(), vertex.getY(), vertex.getZ()).color(red, green, blue, alpha).tex(uv[U_ARRAY], uv[V_ARRAY]).lightmap(light).endVertex();
    }
}