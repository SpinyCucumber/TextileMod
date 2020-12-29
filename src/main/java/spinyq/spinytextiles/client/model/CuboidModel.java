package spinyq.spinytextiles.client.model;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryStack;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import spinyq.spinytextiles.utility.color.RGBAColor;

/**
 * A boring cube model.
 * Textures can be applied to different sides, similar to BlockModel.
 * CuboidModels must be baked before they can be rendered. This makes
 * rendering much faster.
 * @author Elijah Hilty
 *
 */
// TODO Better support for UVs. The current way of handling UV coordinates is sort of
// vestigial, and can be unpredictable.
@OnlyIn(Dist.CLIENT)
public class CuboidModel {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<Direction, CoordinatePlane> SIDE_PLANES = Maps
			.immutableEnumMap(new ImmutableMap.Builder<Direction, CoordinatePlane>()
					.put(Direction.DOWN, new CoordinatePlane(new Vector3f(0f, 0f, 0f), Direction.EAST, Direction.SOUTH))
					.put(Direction.UP, new CoordinatePlane(new Vector3f(0f, 1f, 1f), Direction.EAST, Direction.NORTH))
					.put(Direction.NORTH, new CoordinatePlane(new Vector3f(1f, 1f, 1f), Direction.WEST, Direction.DOWN))
					.put(Direction.SOUTH, new CoordinatePlane(new Vector3f(0f, 1f, 0f), Direction.EAST, Direction.DOWN))
					.put(Direction.WEST, new CoordinatePlane(new Vector3f(0f, 1f, 1f), Direction.NORTH, Direction.DOWN))
					.put(Direction.EAST, new CoordinatePlane(new Vector3f(1f, 1f, 0f), Direction.SOUTH, Direction.DOWN))
					.build());

	private static final Vec2f[] CORNERS = new Vec2f[] { new Vec2f(0, 0), new Vec2f(1, 0), new Vec2f(1, 1),
			new Vec2f(0, 1) };

	public static final UVProvider FULL_UV = new UVList(0f,0f,16f,16f);
	// Provides UV coordinates using the size and position of the cuboid.
	// The works by projecting the position of each vertex onto a plane, and
	// getting the UV coordinates from that plane.
	public static final UVProvider AUTO_UV = new UVProvider() {

		@Override
		public void provideUV(Direction side, TextureAtlasSprite sprite, PositionTextureVertex[] vertices, Vec2f[] corners) {
			CoordinatePlane uvPlane = SIDE_PLANES.get(side).copy();
			uvPlane.scale(16f);
			for (int i = 0; i < corners.length; i++) {
				Vec2f uv = uvPlane.project(vertices[i].pos);
				vertices[i].u = sprite.getInterpolatedU(uv.x);
				vertices[i].v = sprite.getInterpolatedU(uv.y);
			}
		}
		
	};

	/**
	 * Provides the UV coordinates for each corner on the face
	 * of a cuboid.
	 * @author SpinyQ
	 */
	@OnlyIn(Dist.CLIENT)
	public interface UVProvider {
		
		void provideUV(Direction side, TextureAtlasSprite sprite, PositionTextureVertex[] vertices, Vec2f[] corners);
		
	}
	
	/**
	 * Allows clients to provide their own UV coordinates for each corner
	 * on the face of a cuboid.
	 * UV coordinates in Minecraft are generally between 0 and 16.
	 * @author SpinyQ
	 *
	 */
	@OnlyIn(Dist.CLIENT)
	public static class UVList implements UVProvider {

		private float minU, minV, maxU, maxV;
		
		public UVList(float minU, float minV, float maxU, float maxV) {
			this.minU = minU;
			this.minV = minV;
			this.maxU = maxU;
			this.maxV = maxV;
		}

		@Override
		public void provideUV(Direction side, TextureAtlasSprite sprite, PositionTextureVertex[] vertices, Vec2f[] corners) {
			float sizeU = maxU - minU, sizeV = maxV - minV;
			for (int i = 0; i < corners.length; i++) {
				vertices[i].u = sprite.getInterpolatedU((sizeU * corners[i].x) + minU);
				vertices[i].v = sprite.getInterpolatedU((sizeV * corners[i].y) + minV);
			}
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class CuboidFace {
		
		private Material texture;
		private UVProvider uv = FULL_UV;
		
		public void setTexture(Material texture) {
			this.texture = texture;
		}
		
		public void setUV(UVProvider uv) {
			this.uv = uv;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class BakedCuboid {
	
		private ImmutableList<BakedQuad> quads;
	
		private BakedCuboid(ImmutableList<BakedQuad> quads) {
			this.quads = quads;
		}
	
		public void render(IVertexBuilder buffer, MatrixStack stack, RGBAColor color, int combinedLightIn,
				int combinedOverlayIn) {
			MatrixStack.Entry entry = stack.getLast();
			for (BakedQuad quad : quads) {
				renderQuad(buffer, quad, entry, color, combinedLightIn, combinedOverlayIn);
			}
		}
	
	}
	
	@OnlyIn(Dist.CLIENT)
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

		public Vector3f map(Vec2f coords) {
			Vector3f xPart = xAxis.copy();
			Vector3f yPart = yAxis.copy();
			xPart.mul(coords.x);
			yPart.mul(coords.y);
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
			origin.mul(vec.getX(), vec.getY(), vec.getZ());
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
	private static class PositionTextureVertex {

		public Vector3f pos;
		public float u, v;

		public String toString() {
			return MoreObjects.toStringHelper(this).add("pos", pos).add("u", u).add("v", v).toString();
		}
		
	}

	private Vector3f fromPosition, toPosition;
	private Map<Direction, CuboidFace> faces = new EnumMap<>(Direction.class);

	public void setFromPosition(Vector3f fromPosition) {
		this.fromPosition = fromPosition;
	}

	public void setToPosition(Vector3f toPosition) {
		this.toPosition = toPosition;
	}

	public CuboidFace getFace(Direction direction) {
		CuboidFace face = faces.get(direction);
		if (face == null) {
			face = new CuboidFace();
			faces.put(direction, face);
		}
		return face;
	}

	public Vector3f getSize() {
		Vector3f result = toPosition.copy();
		result.sub(fromPosition);
		return result;
	}

	public BakedCuboid bake(TransformationMatrix transform) {
		LOGGER.trace("Baking cuboid model...");
		// Start constructing quads
		ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
		// Construct the position and for each corner on the cube
		// Add quads for each face
		Vector3f size = getSize();
		for (Direction side : Direction.values()) {
			LOGGER.trace("Baking side: {}", side);
			// Get the face for the side
			// If we don't have any data for the side, don't render it
			CuboidFace face = faces.get(side);
			if (face == null)
				continue;
			// Get the sprite
			TextureAtlasSprite sprite = face.texture.getSprite();
			LOGGER.trace("Using sprite: {}", sprite);
			// Iterate over the four corners of the face
			// to construct the four vertices of the quad
			PositionTextureVertex[] vertices = new PositionTextureVertex[4];
			// Create the coordinate plane for this side
			// Get the coordinate plane associated with the side and transform it
			CoordinatePlane positionPlane = SIDE_PLANES.get(side).copy();
			positionPlane.scale(size);
			positionPlane.translate(fromPosition);
			LOGGER.trace("Position plane: {}", positionPlane);
			for (int i = 0; i < CORNERS.length; i++) {
				Vec2f corner = CORNERS[i];
				LOGGER.trace("Creating vertex for corner: {}", corner);
				// Initialize vertex
				vertices[i] = new PositionTextureVertex();
				// Get the position of the vertex
				vertices[i].pos = positionPlane.map(corner);
				LOGGER.trace("Completed vertex: {}", vertices[i]);
			}
			// Let the face handle providing UV
			face.uv.provideUV(side, sprite, vertices, CORNERS);
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
	
	// Adapted from IVertexBuilder to allow for alpha
	private static void renderQuad(IVertexBuilder buffer, BakedQuad quad, MatrixStack.Entry entry, RGBAColor color, int combinedLightIn,
				int combinedOverlayIn) {
		int[] aint = quad.getVertexData();
		Vector3f vector3f = quad.getFace().toVector3f();
		vector3f.transform(entry.getNormal());
		int numVertices = aint.length / 8;

		try (MemoryStack memorystack = MemoryStack.stackPush()) {
			ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormats.BLOCK.getSize());
			IntBuffer intbuffer = bytebuffer.asIntBuffer();

			for (int i = 0; i < numVertices; ++i) {
				((Buffer) intbuffer).clear();
				intbuffer.put(aint, i * 8, 8);
				float f = bytebuffer.getFloat(0);
				float f1 = bytebuffer.getFloat(4);
				float f2 = bytebuffer.getFloat(8);

				int l = buffer.applyBakedLighting(combinedLightIn, bytebuffer);
				float f9 = bytebuffer.getFloat(16);
				float f10 = bytebuffer.getFloat(20);
				Vector4f vector4f = new Vector4f(f, f1, f2, 1.0F);
				vector4f.transform(entry.getMatrix());
				buffer.applyBakedNormals(vector3f, bytebuffer, entry.getNormal());
				buffer.addVertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), color.r, color.g, color.b, color.a, f9, f10,
						combinedOverlayIn, l, vector3f.getX(), vector3f.getY(), vector3f.getZ());
			}
		}
	}

}
