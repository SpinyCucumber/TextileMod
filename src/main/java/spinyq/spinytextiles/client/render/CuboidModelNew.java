package spinyq.spinytextiles.client.render;

import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import spinyq.spinytextiles.utility.color.RGBColor;

@OnlyIn(Dist.CLIENT)
public class CuboidModelNew {

	@OnlyIn(Dist.CLIENT)
	public static class BakedCuboid {

		private ImmutableList<BakedQuad> quads;

		private BakedCuboid(ImmutableList<BakedQuad> quads) {
			this.quads = quads;
		}

		public void render(MatrixStack stack, IVertexBuilder buffer, RGBColor color, int combinedLightIn,
				int combinedOverlayIn) {
			MatrixStack.Entry entry = stack.getLast();
			for (BakedQuad quad : quads) {
				buffer.addQuad(entry, quad, color.r, color.g, color.b, combinedLightIn, combinedOverlayIn);
			}
		}

	}

	@OnlyIn(Dist.CLIENT)
	private static class PositionTextureVertex {

		public float x, y, z, u, v;

		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("x", x)
					.add("y", y)
					.add("z", z)
					.add("u", u)
					.add("v", v)
					.toString();
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static class Vec2i {

		public int x, y;

		private Vec2i(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return MoreObjects.toStringHelper(this).add("x", x).add("y", y).toString();
		}

	}

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Vec2i[] CORNERS = new Vec2i[] { new Vec2i(-1, -1), new Vec2i(1, -1), new Vec2i(1, 1),
			new Vec2i(-1, 1) };

	public float minX, minY, minZ;
	public float maxX, maxY, maxZ;

	private Map<Direction, Material> sideTextures = new EnumMap<>(Direction.class);

	public Material getSideTexture(Direction side) {
		return sideTextures.get(side);
	}

	public void setSideTexture(Direction side, Material texture) {
		sideTextures.put(side, texture);
	}

	// Sets all side textures to this texture.
	public void setTexture(Material texture) {
		for (Direction side : Direction.values()) {
			sideTextures.put(side, texture);
		}
	}

	public BakedCuboid bake(TransformationMatrix transform) {
		LOGGER.trace("Baking cuboid model...");
		// Start constructing quads
		ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
		// Construct the position and for each corner on the cube
		// Add quads for each face
		for (Direction side : Direction.values()) {
			LOGGER.trace("Baking side: {}", side);
			// Get the texture for the side
			// If the side doesn't have a texture, skip this side
			Material texture = getSideTexture(side);
			if (texture == null)
				continue;
			// Get the sprite
			TextureAtlasSprite sprite = texture.getSprite();
			LOGGER.trace("Using sprite: {}", sprite);
			// For each side, get the normal, and the two vectors perpendicular
			// to the normal.
			Vec3i directionVec = side.getDirectionVec(), perpVec0 = getPerpendicular(directionVec),
					perpVec1 = getPerpendicular(perpVec0);
			// Iterate over the four corners of the face
			// to construct the four vertices of the quad
			PositionTextureVertex[] vertices = new PositionTextureVertex[4];
			int index = 0;

			for (Vec2i corner : CORNERS) {
				LOGGER.trace("Creating vertex for corner: {}", corner);
				PositionTextureVertex vertex = new PositionTextureVertex();
				// Get the position of the vertex in "cube space"
				// This means each component is either going to be -1 or 1
				Vec3i posCube = add(directionVec, add(scale(perpVec0, corner.x), scale(perpVec1, corner.y)));
				LOGGER.trace("Cube position: {}", posCube);
				// Next, get the actual position of the vertex
				vertex.x = (posCube.getX() == -1) ? minX : maxX;
				vertex.y = (posCube.getY() == -1) ? minY : maxY;
				vertex.z = (posCube.getZ() == -1) ? minZ : maxZ;
				// Next, get the uv coordinates of the vertex
				// We need to use the sprite to do this
				vertex.u = (corner.x == -1) ? sprite.getMinU() : sprite.getMaxU();
				vertex.v = (corner.y == -1) ? sprite.getMinV() : sprite.getMaxV();
				LOGGER.trace("Completed vertex: {}", vertex);
				vertices[index++] = vertex;
			}
			// Finally, construct a quad using the four vertices
			builder.add(buildQuad(transform, side, sprite, 0, vertices));
		}
		// Finished
		return new BakedCuboid(builder.build());
	}

	private static Vec3i getPerpendicular(Vec3i vec) {
		return new Vec3i(vec.getZ(), vec.getX(), vec.getY());
	}

	private static Vec3i add(Vec3i left, Vec3i right) {
		return new Vec3i(left.getX() + right.getX(), left.getY() + right.getY(), left.getZ() + right.getZ());
	}

	private static Vec3i scale(Vec3i vec, int scale) {
		return new Vec3i(scale * vec.getX(), scale * vec.getY(), scale * vec.getZ());
	}

	private static BakedQuad buildQuad(TransformationMatrix transform, Direction side, TextureAtlasSprite sprite,
			int tint, PositionTextureVertex[] vertices) {
		BakedQuadBuilder builder = new BakedQuadBuilder(sprite);

		builder.setQuadTint(tint);
		builder.setQuadOrientation(side);

		boolean hasTransform = !transform.isIdentity();
		IVertexConsumer consumer = hasTransform ? new TRSRTransformer(builder, transform) : builder;

		for (PositionTextureVertex vertex : vertices)
			putVertex(consumer, side, vertex);

		return builder.build();
	}

	private static void putVertex(IVertexConsumer consumer, Direction side, PositionTextureVertex vertex) {
		VertexFormat format = consumer.getVertexFormat();
		for (int e = 0; e < format.getElements().size(); e++) {
			switch (format.getElements().get(e).getUsage()) {
			case POSITION:
				consumer.put(e, vertex.x, vertex.y, vertex.z, 1f);
				break;
			case COLOR:
				consumer.put(e, 1f, 1f, 1f, 1f);
				break;
			case NORMAL:
				float offX = (float) side.getXOffset();
				float offY = (float) side.getYOffset();
				float offZ = (float) side.getZOffset();
				consumer.put(e, offX, offY, offZ, 0f);
				break;
			case UV:
				if (format.getElements().get(e).getIndex() == 0) {
					consumer.put(e, vertex.u, vertex.v, 0f, 1f);
					break;
				}
			default:
				consumer.put(e);
				break;
			}
		}
	}

}
