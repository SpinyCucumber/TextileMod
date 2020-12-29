package spinyq.spinytextiles.client.render;

import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import spinyq.spinytextiles.utility.color.RGBColor;

@OnlyIn(Dist.CLIENT)
public class CuboidModelNew {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<Direction, CoordinatePlane> SIDE_PLANES = Maps.immutableEnumMap(
			new ImmutableMap.Builder<Direction, CoordinatePlane>()
			.put(Direction.DOWN, new CoordinatePlane(new Vector3f(0f,0f,0f), Direction.EAST, Direction.SOUTH))
			.put(Direction.UP, new CoordinatePlane(new Vector3f(0f,1f,1f), Direction.EAST, Direction.NORTH))
			.put(Direction.NORTH, new CoordinatePlane(new Vector3f(1f,1f,1f), Direction.WEST, Direction.DOWN))
			.put(Direction.SOUTH, new CoordinatePlane(new Vector3f(0f,1f,0f), Direction.EAST, Direction.DOWN))
			.put(Direction.WEST, new CoordinatePlane(new Vector3f(0f,1f,1f), Direction.SOUTH, Direction.DOWN))
			.put(Direction.EAST, new CoordinatePlane(new Vector3f(1f,1f,0f), Direction.NORTH, Direction.DOWN))
			.build());
	
	private static final Vec2i[] CORNERS = new Vec2i[] { new Vec2i(0,0), new Vec2i(1,0), new Vec2i(1,1),
	new Vec2i(0,1) };

	public static class CoordinatePlane {
		
		private Vector3f origin, xAxis, yAxis;

		private CoordinatePlane(Vector3f origin, Vector3f xAxis, Vector3f yAxis) {
			this.origin = origin;
			this.xAxis = xAxis;
			this.yAxis = yAxis;
		}

		private CoordinatePlane(Vector3f origin, Direction xAxis, Direction yAxis) {
			this(origin, xAxis.toVector3f(), yAxis.toVector3f());
		}
		
		public Vector3f map(Vec2i vec) {
			Vector3f xPart = xAxis.copy();
			Vector3f yPart = yAxis.copy();
			xPart.mul(vec.x);
			yPart.mul(vec.y);
			Vector3f point = origin.copy();
			point.add(xPart);
			point.add(yPart);
			return point;
		}
		
		public Vec2f project(Vector3f vectorIn) {
			Vector3f vec = vectorIn.copy();
			vec.sub(origin);
			return new Vec2f(xAxis.dot(vec), yAxis.dot(vec));
		}
		
		public CoordinatePlane copy() {
			return new CoordinatePlane(origin.copy(), xAxis.copy(), yAxis.copy());
		}
		
		public void translate(Vector3f vec) {
			origin.add(vec);
		}
		
		public void scale(Vector3f vec) {
			xAxis.mul(vec.getX(), vec.getY(), vec.getZ());
			yAxis.mul(vec.getX(), vec.getY(), vec.getZ());
		}
		
		public void scale(float s) {
			xAxis.mul(s);
			yAxis.mul(s);
		}
		
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("origin", origin)
					.add("xAxis", xAxis)
					.add("yAxis", yAxis)
					.toString();
		}
		
	}
	
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

		public Vector3f pos;
		public float u, v;

		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("pos", pos)
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

	public Vector3f positionFrom = new Vector3f(), positionTo = new Vector3f();

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
	
	public Vector3f getSize() {
		Vector3f result = positionTo.copy();
		result.sub(positionFrom);
		return result;
	}

	public BakedCuboid bake(TransformationMatrix transform) {
		LOGGER.info("Baking cuboid model...");
		// Start constructing quads
		ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
		// Construct the position and for each corner on the cube
		// Add quads for each face
		Vector3f size = getSize();
		for (Direction side : Direction.values()) {
			LOGGER.info("Baking side: {}", side);
			// Get the texture for the side
			// If the side doesn't have a texture, skip this side
			Material texture = getSideTexture(side);
			if (texture == null)
				continue;
			// Get the sprite
			TextureAtlasSprite sprite = texture.getSprite();
			LOGGER.info("Using sprite: {}", sprite);
			// Iterate over the four corners of the face
			// to construct the four vertices of the quad
			PositionTextureVertex[] vertices = new PositionTextureVertex[4];
			int index = 0;
			// Create the coordinate plane for this side
			// Get the coordinate plane associated with the side and transform it
			CoordinatePlane sidePlane = SIDE_PLANES.get(side).copy();
			CoordinatePlane positionPlane = sidePlane.copy(), uvPlane = sidePlane.copy();
			positionPlane.scale(size);
			positionPlane.translate(positionFrom);
			uvPlane.scale(16f);
			LOGGER.info("Position plane: {}", positionPlane);
			LOGGER.info("UV plane: {}", uvPlane);
			for (Vec2i corner : CORNERS) {
				LOGGER.info("Creating vertex for corner: {}", corner);
				PositionTextureVertex vertex = new PositionTextureVertex();
				// Get the position of the vertex
				vertex.pos = positionPlane.map(corner);
				// Next, get the uv coordinates of the vertex
				// We project the vertex position onto the face to get the uv coordinates
				// We have to multiply the uv by 16 since Minecraft is weird
				Vec2f uv = uvPlane.project(vertex.pos);
				vertex.u = sprite.getInterpolatedU(uv.x);
				vertex.v = sprite.getInterpolatedV(uv.y);
				LOGGER.info("Using u: {} and v: {}", uv.x, uv.y);
				
				LOGGER.info("Completed vertex: {}", vertex);
				vertices[index++] = vertex;
			}
			// Finally, construct a quad using the four vertices
			builder.add(buildQuad(transform, side, sprite, 0, vertices));
		}
		// Finished
		return new BakedCuboid(builder.build());
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
				consumer.put(e, vertex.pos.getX(), vertex.pos.getY(), vertex.pos.getZ(), 1f);
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
