package spinyq.spinytextiles.client.render;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;

public class CuboidModelNew implements IModelGeometry<CuboidModelNew> {

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

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
			ItemOverrideList overrides, ResourceLocation modelLocation) {
		// This code is adapted from BlockModel
		// Start constructing a new simple baked model
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(owner, overrides);
		// Construct the position and for each corner on the cube
		Vector3f[] vertices = new Vector3f[8];
		for (int x = 0; x <= 1; x++) {
			for (int y = 0; y <= 1; y++) {
				for (int z = 0; z <= 1; z++) {
					int index = getIndex(x, y, z);
					float posX = (x == 0) ? minX : maxX;
					float posY = (y == 0) ? minY : maxY;
					float posZ = (z == 0) ? minZ : maxZ;
					vertices[index] = new Vector3f(posX, posY, posZ);
				}
			}
		}
		// Add quads for each face
		for (Direction side : Direction.values()) {
			Vec3i directionVec = side.getDirectionVec(),
					perpVec0 = getPerpendicular(directionVec),
					perpVec1 = getPerpendicular(perpVec0);
		}
		// Finished
		return builder.build();
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		// Return all side textures
		return sideTextures.values();
	}
	
	private static int getIndex(int x, int y, int z) {
		return 4*x + 2*y + z;
	}
	
	private static Vec3i getPerpendicular(Vec3i vec) {
		return new Vec3i(vec.getZ(), vec.getX(), vec.getY());
	}

	private static class PositionTextureVertex {

		public Vector3f position;
		public float textureU;
		public float textureV;

	}

	private static BakedQuad buildQuad(TransformationMatrix transform, Direction side, TextureAtlasSprite sprite,
			int tint, float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1, float u1,
			float v1, float x2, float y2, float z2, float u2, float v2, float x3, float y3, float z3, float u3,
			float v3) {
		BakedQuadBuilder builder = new BakedQuadBuilder(sprite);

		builder.setQuadTint(tint);
		builder.setQuadOrientation(side);

		boolean hasTransform = !transform.isIdentity();
		IVertexConsumer consumer = hasTransform ? new TRSRTransformer(builder, transform) : builder;

		putVertex(consumer, side, x0, y0, z0, u0, v0);
		putVertex(consumer, side, x1, y1, z1, u1, v1);
		putVertex(consumer, side, x2, y2, z2, u2, v2);
		putVertex(consumer, side, x3, y3, z3, u3, v3);

		return builder.build();
	}

	private static void putVertex(IVertexConsumer consumer, Direction side, float x, float y, float z, float u,
			float v) {
		VertexFormat format = consumer.getVertexFormat();
		for (int e = 0; e < format.getElements().size(); e++) {
			switch (format.getElements().get(e).getUsage()) {
			case POSITION:
				consumer.put(e, x, y, z, 1f);
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
					consumer.put(e, u, v, 0f, 1f);
					break;
				}
				// else fallthrough to default
			default:
				consumer.put(e);
				break;
			}
		}
	}

}
