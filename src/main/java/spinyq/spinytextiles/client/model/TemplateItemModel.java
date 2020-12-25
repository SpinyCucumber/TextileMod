package spinyq.spinytextiles.client.model;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelTransformComposition;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;

/**
 * A layered item model, similar to ItemLayerModel, with the additional feature
 * that each layer has a template texture as well. The template texture
 * determines what part of the normal texture is visible. Most of this code is
 * adapted from ItemLayerModel and ItemTextureQuadConverter, with additional
 * comments for clarifying things. This class is a Frankenstein monster made
 * from already dodgy code; the algorithms used in here are by no means the most
 * efficient way of doing things. ItemTextureQuadConverter didn't work for an
 * edge case, which I had to fix. And strangely, ItemLayerModel generated the
 * side quads upside down, which I changed. ItemCameraTransform type is
 * deprecated, but BakedItemModel uses it so we are forced to use it as well. Oh
 * well
 * 
 * @author SpinyQ
 *
 */
//TODO Improvement: Skipping internal side quads would allow us to cut down on quads.
//TODO Handle particle texture
//TODO Rewrite this eventually
@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public class TemplateItemModel implements IModelGeometry<TemplateItemModel> {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final Direction[] HORIZONTALS = { Direction.UP, Direction.DOWN };
	private static final Direction[] VERTICALS = { Direction.WEST, Direction.EAST };
	private static final float NUDGE_INCREMENT = 0.0001f;

	private List<TemplateLayer> layers;

	/**
	 * A template layer described a layer of a template item model. It has a texture
	 * as well as a template texture.
	 * 
	 * @author Elijah Hilty
	 *
	 */
	public static class TemplateLayer {

		private final Material texture, template;
		private int tint;

		public TemplateLayer(Material texture, Material template, int tint) {
			this.texture = texture;
			this.template = template;
			this.tint = tint;
		}

		@Override
		public String toString() {
			return "TemplateLayer [texture=" + texture + ", template=" + template + ", tint=" + tint + "]";
		}

	}

	private static class FaceData {
		private final EnumMap<Direction, BitSet> data = new EnumMap<>(Direction.class);
	
		private final int vMax;
	
		FaceData(int uMax, int vMax) {
			this.vMax = vMax;
	
			data.put(Direction.WEST, new BitSet(uMax * vMax));
			data.put(Direction.EAST, new BitSet(uMax * vMax));
			data.put(Direction.UP, new BitSet(uMax * vMax));
			data.put(Direction.DOWN, new BitSet(uMax * vMax));
		}
	
		public void set(Direction facing, int u, int v) {
			data.get(facing).set(getIndex(u, v));
		}
	
		public boolean get(Direction facing, int u, int v) {
			return data.get(facing).get(getIndex(u, v));
		}
	
		private int getIndex(int u, int v) {
			return v * vMax + u;
		}
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
			ItemOverrideList overrides, ResourceLocation modelLocation) {
		// Do some logging
		LOGGER.info("Baking a template item model for layers: {}", layers);
		// Get transforms
		IModelTransform transformsFromModel = owner.getCombinedTransform();
		ImmutableMap<TransformType, TransformationMatrix> transformMap = transformsFromModel != null
				? PerspectiveMapWrapper
						.getTransforms(new ModelTransformComposition(transformsFromModel, modelTransform))
				: PerspectiveMapWrapper.getTransforms(modelTransform);
		TransformationMatrix transform = modelTransform.getRotation();

		ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
		float nudge = 0f;
		for (TemplateLayer layer : getLayers(owner)) {
			// Get the sprites
			TextureAtlasSprite sprite = spriteGetter.apply(layer.texture),
					templateSprite = spriteGetter.apply(layer.template);
			generateQuads(layer.tint, nudge, sprite, templateSprite, transform, builder);
			nudge += NUDGE_INCREMENT;
		}

		ImmutableList<BakedQuad> quads = builder.build();
		LOGGER.info("Total Quads: {}", quads.size());
		// Construct the baked model
		// The override handler for this model is arbitrary
		return new BakedItemModel(quads, null, Maps.immutableEnumMap(transformMap), overrides, transform.isIdentity(),
				owner.isSideLit());
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		// Create a set of all the textures that the layers use
		return getLayers(owner).stream().map((layer) -> Stream.of(layer.texture, layer.template))
				.flatMap(Function.identity()).collect(Collectors.toSet());
	}

	public List<TemplateLayer> createLayers(IModelConfiguration owner) {
		// TODO Improvement: create default behavior
		return ImmutableList.of();
	}

	private List<TemplateLayer> getLayers(IModelConfiguration owner) {
		// We cache the layers to use later when baking the model
		if (layers == null)
			layers = createLayers(owner);
		return layers;
	}

	public static void generateQuads(int tint, float nudge, TextureAtlasSprite sprite, TextureAtlasSprite template,
			TransformationMatrix transform, ImmutableList.Builder<BakedQuad> builder) {

		LOGGER.trace("Generating quads for sprite: {} with template: {} with nudge: {}", sprite, template, nudge);

		int uMax = sprite.getWidth();
		int vMax = sprite.getHeight();

		// FaceData contains info about the sides of the sprite.
		// A "side" occurs when a transparent pixel borders an opaque pixel.
		// For each of the four flat directions, FaceData contains a bit set
		// marking where the sprite has a side facing that direction.
		// For example, if faceData.get(Direction.EAST, 5, 6) is true,
		// then the pixel at (5,6) has a side to the right.
		FaceData faceData = new FaceData(uMax, vMax);

		// This flag marks whether the sprite contains "translucent" pixels -
		// pixels that are not fully transparent, but not fully opaque.

		LOGGER.trace("Scanning for edges...");

		// The following section calculates the faceData for the sprite.
		for (int f = 0; f < sprite.getFrameCount(); f++) {
			// These variables keep track of pixel transparency.
			// ptu tracks whether the previous pixel is transparent,
			// and ptv keeps track of the transparency of the previous row of pixels.
			boolean ptu;
			boolean[] ptv = new boolean[uMax];
			Arrays.fill(ptv, true);
			// Iterate over each pixel, reading each row from left-to-right starting with
			// the top row.
			for (int v = 0; v < vMax; v++) {
				ptu = true;
				for (int u = 0; u < uMax; u++) {
					// In addition to checking if the sprite's pixel is transparent,
					// we also check whether the template's pixel is transparent.
					// If either of them is transparent, then the current pixel is transparent.
					boolean t = !isVisible(sprite, f, u, v) || !isVisible(template, 0, u, v);

					// If we've just moved from a transparent pixel to an opaque pixel,
					// we've found a side facing to the left. (west)
					if (ptu && !t) // left - transparent, right - opaque
					{
						faceData.set(Direction.WEST, u, v);
					}
					// Similarly, if we've moved from an opaque pixel to a transparent one,
					// we've found a side facing to the right. (east)
					if (!ptu && t) // left - opaque, right - transparent
					{
						faceData.set(Direction.EAST, u - 1, v);
					}
					// Next, check the pixel to the top of the current one.
					// If its transparent and the current pixel is not, we've found
					// a side facing upwards. Vice versa for a downwards facing side.
					if (ptv[u] && !t) // up - transparent, down - opaque
					{
						faceData.set(Direction.UP, u, v);
					}
					if (!ptv[u] && t) // up - opaque, down - transparent
					{
						faceData.set(Direction.DOWN, u, v - 1);
					}

					ptu = t;
					ptv[u] = t;
				}
				// If the last pixel in the row is opaque, then it also has a side to the right.
				// (east)
				if (!ptu) // last - opaque
				{
					faceData.set(Direction.EAST, uMax - 1, v);
				}
			}
			// For every pixel in the final row, if it is opaque,
			// then it also has a side facing down.
			// last line
			for (int u = 0; u < uMax; u++) {
				if (!ptv[u]) {
					faceData.set(Direction.DOWN, u, vMax - 1);
				}
			}
		}

		// DEBUG
		for (Direction facing : new Direction[] { Direction.UP, Direction.DOWN, Direction.WEST, Direction.EAST }) {

			String output = IntStream.range(0, vMax).mapToObj((v) -> {
				return IntStream.range(0, uMax).mapToObj((u) -> faceData.get(facing, u, v) ? "1" : "0")
						.collect(Collectors.joining(" "));
			}).collect(Collectors.joining("\n"));

			LOGGER.trace("Direction: {}", facing);
			LOGGER.trace("FaceData:\n{}", output);

		}

		// The following section generates horizontal side quads.
		// Note that these quads are horizontally aligned, rather than facing a
		// horizontal.
		// They face either up or down. Confusing, the HORIZONTALS variable contains the
		// directions UP and DOWN.
		// The process is performed for both the UP and DOWN direction.

		LOGGER.trace("Generating side quads...");

		// horizontal quads
		for (Direction facing : HORIZONTALS) {
			LOGGER.trace("\n=========================\n{} QUADS\n=========================",
					facing.toString().toUpperCase());
			// Iterate over each row of pixels.
			for (int v = 0; v < vMax; v++) {
				// For each row, we keep track of whether we are currently building a quad.
				// We also keep track of the start of the current quad, and the end of the
				// current quad.
				// These are only relevant if we are building a quad.
				int uStart = 0, uEnd = uMax;
				boolean building = false;
				// Iterate over every pixel in the row
				for (int u = 0; u < uMax; u++) {
					// Retrieve whether the current pixel has a side or not
					boolean face = faceData.get(facing, u, v);

					// If we're building a quad and the current pixel doesn't have a side,
					// we finish the current quad and send it to the list.
					if (building && !face) // finish current quad
					{
						// make quad [uStart, u]
						int off = facing == Direction.DOWN ? 1 : 0;
						LOGGER.trace("Building a horizontal quad facing {} at row v={} with start u={} and end u={}",
								facing, v, uStart, u);
						builder.add(buildSideQuad(transform, facing, tint, nudge, sprite, uStart, v + off, u - uStart));
						building = false;
					}
					// If we're not already building a quad and the current pixel has a side,
					// we start building a new quad.
					else if (!building && face) // start new quad
					{
						building = true;
						uStart = u;
					}
				}
				// If we're still building a quad at the end of the row,
				// finish the current quad and send it to the list.
				if (building) // build remaining quad
				{
					// make quad [uStart, uEnd]
					int off = facing == Direction.DOWN ? 1 : 0;
					LOGGER.trace("Building a horizontal quad facing {} at row v={} with start u={} and end u={}",
							facing, v, uStart, uEnd);
					builder.add(buildSideQuad(transform, facing, tint, nudge, sprite, uStart, v + off, uEnd - uStart));
				}
			}
		}

		// This does the same thing as the above section, but for vertical quads
		// instead.
		// This means scanning the WEST and EAST sides of each pixel and building quads
		// for contiguous sides.

		// vertical quads
		for (Direction facing : VERTICALS) {
			LOGGER.trace("\n=========================\n{} QUADS\n=========================",
					facing.toString().toUpperCase());
			for (int u = 0; u < uMax; u++) {
				int vStart = 0, vEnd = vMax;
				boolean building = false;
				for (int v = 0; v < vMax; v++) {
					boolean face = faceData.get(facing, u, v);

					if (building && !face) // finish current quad
					{
						// make quad [vStart, v]
						int off = facing == Direction.EAST ? 1 : 0;
						LOGGER.trace("Building a vertical quad facing {} at column u={} with start v={} and end v={}",
								facing, u, vStart, v);
						builder.add(buildSideQuad(transform, facing, tint, nudge, sprite, u + off, vStart, v - vStart));
						building = false;
					} else if (!building && face) // start new quad
					{
						building = true;
						vStart = v;
					}
				}
				if (building) // build remaining quad
				{
					// make quad [vStart, vEnd]
					int off = facing == Direction.EAST ? 1 : 0;
					LOGGER.trace("Building a vertical quad facing {} at column u={} with start v={} and end v={}",
							facing, u, vStart, vEnd);
					builder.add(buildSideQuad(transform, facing, tint, nudge, sprite, u + off, vStart, vEnd - vStart));
				}
			}
		}

		LOGGER.trace("Generating face quads...");

		// Finally, we build the "cover" front and back quads.

		List<BakedQuad> backQuads = convertTexture(transform, sprite, template, 7.5f / 16f - nudge, Direction.NORTH,
				0xffffffff, tint);
		List<BakedQuad> frontQuads = convertTexture(transform, sprite, template, 8.5f / 16f + nudge, Direction.SOUTH,
				0xffffffff, tint);
		LOGGER.trace("Total back quads: {} Total front quads: {}", backQuads.size(), frontQuads.size());
		builder.addAll(backQuads);
		builder.addAll(frontQuads);

	}

	/**
	 * Takes a texture and converts it into BakedQuads. The conversion is done by
	 * scanning the texture horizontally and vertically and creating "strips" of the
	 * texture. Strips that are of the same size and follow each other are converted
	 * into one bigger quad. </br>
	 * The resulting list of quads is the texture represented as a list of
	 * horizontal OR vertical quads, depending on which creates less quads. If the
	 * amount of quads is equal, horizontal is preferred.
	 *
	 * @param format
	 * @param template The input texture to convert
	 * @param sprite   The texture whose UVs shall be used
	 * @return The generated quads.
	 */
	public static List<BakedQuad> convertTexture(TransformationMatrix transform, TextureAtlasSprite sprite,
			TextureAtlasSprite template, float z, Direction facing, int color, int tint) {
		List<BakedQuad> horizontal = convertTextureHorizontal(transform, sprite, template, z, facing, color, tint);
		List<BakedQuad> vertical = convertTextureVertical(transform, sprite, template, z, facing, color, tint);

		return horizontal.size() <= vertical.size() ? horizontal : vertical;
	}

	/**
	 * Scans a texture and converts it into a list of horizontal strips stacked on
	 * top of each other. The height of the strips is as big as possible.
	 */
	public static List<BakedQuad> convertTextureHorizontal(TransformationMatrix transform, TextureAtlasSprite sprite,
			TextureAtlasSprite template, float z, Direction facing, int color, int tint) {
		int w = template.getWidth();
		int h = template.getHeight();
		float wScale = 16f / (float) w;
		float hScale = 16f / (float) h;
		List<BakedQuad> quads = Lists.newArrayList();

		// the upper left x-position of the current quad
		int start = -1;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x <= w; x++) {
				// current pixel
				boolean isVisible = isVisible(template, 0, x, y);

				// no current quad but found a new one
				if (start < 0 && isVisible) {
					start = x;
				}
				// got a current quad, but it ends here
				if (start >= 0 && !isVisible) {
					// we now check if the visibility of the next row matches the one fo the current
					// row
					// if they are, we can extend the quad downwards
					int endY = y + 1;
					boolean sameRow = true;
					while (sameRow && endY < h) {
						for (int i = 0; i < w; i++) {
							if (isVisible(template, 0, i, y) != isVisible(template, 0, i, endY)) {
								sameRow = false;
								break;
							}
						}
						if (sameRow) {
							endY++;
						}
					}

					// create the quad
					quads.add(ItemTextureQuadConverter.genQuad(transform, (float) start * wScale, (float) y * hScale,
							(float) x * wScale, (float) endY * hScale, z, sprite, facing, color, tint));

					// update Y if all the rows match. no need to rescan
					if (endY - y > 1) {
						y = endY - 1;
					}
					// clear current quad
					start = -1;
				}
			}
		}

		return quads;
	}

	/**
	 * Scans a texture and converts it into a list of vertical strips stacked next
	 * to each other from left to right. The width of the strips is as big as
	 * possible.
	 */
	public static List<BakedQuad> convertTextureVertical(TransformationMatrix transform, TextureAtlasSprite sprite,
			TextureAtlasSprite template, float z, Direction facing, int color, int tint) {
		int w = template.getWidth();
		int h = template.getHeight();
		float wScale = 16f / (float) w;
		float hScale = 16f / (float) h;
		List<BakedQuad> quads = Lists.newArrayList();

		// the upper left y-position of the current quad
		int start = -1;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y <= h; y++) {
				// current pixel
				boolean isVisible = isVisible(template, 0, x, y);

				// no current quad but found a new one
				if (start < 0 && isVisible) {
					start = y;
				}
				// got a current quad, but it ends here
				if (start >= 0 && !isVisible) {
					// we now check if the visibility of the next column matches the one fo the
					// current row
					// if they are, we can extend the quad downwards
					int endX = x + 1;
					boolean sameColumn = true;
					while (sameColumn && endX < w) {
						for (int i = 0; i < h; i++) {
							if (isVisible(template, 0, x, i) != isVisible(template, 0, endX, i)) {
								sameColumn = false;
								break;
							}
						}
						if (sameColumn) {
							endX++;
						}
					}

					// create the quad
					quads.add(ItemTextureQuadConverter.genQuad(transform, (float) x * wScale, (float) start * hScale,
							(float) endX * wScale, (float) y * hScale, z, sprite, facing, color, tint));

					// update X if all the columns match. no need to rescan
					if (endX - x > 1) {
						x = endX - 1;
					}
					// clear current quad
					start = -1;
				}
			}
		}

		return quads;
	}

	private static BakedQuad buildSideQuad(TransformationMatrix transform, Direction side, int tint, float nudge,
			TextureAtlasSprite sprite, int u, int v, int size) {
		final float eps = 1e-3f;
	
		int width = sprite.getWidth();
		int height = sprite.getHeight();
	
		// These describe the position of the side quad.
		float x0 = (float) u / width;
		float y0 = (float) v / height;
		float x1 = x0, y1 = y0;
		float z0 = 7.5f / 16f, z1 = 8.5f / 16f;
	
		// Switch statements can be unclear.
		// In this case, the line y1 = (float) (v + size) / height line is executed
		// for both the WEST and EAST sides since the WEST branch doesn't have a break
		// statement.
		// Similarly, the x1 = (float) (u + size) / width line is executed for
		// both the DOWN and UP sides.
		switch (side) {
		// If the direction is either WEST or UP we have to flip the z coordinates
		// in order to preserve the winding of the vertices. The winding determines
		// which direction the quad faces.
		// If the quad is vertical, we set the add the size to the y coordinate of the
		// first corner to retrieve the y coordinate of the second corner.
		case WEST:
			z0 = 8.5f / 16f;
			z1 = 7.5f / 16f;
		case EAST:
			y1 = (float) (v + size) / height;
			break;
		// If the quad is horizontal, we set the add the size to the x coordinate of the
		// first corner to retrieve the x coordinate of the second corner.
		case UP:
		case DOWN:
			x1 = (float) (u + size) / width;
			break;
		default:
			throw new IllegalArgumentException("can't handle z-oriented side");
		}
	
		float dx = side.getDirectionVec().getX() * eps / width;
		float dy = side.getDirectionVec().getY() * eps / height;
	
		float u0 = 16f * (x0 - dx);
		float u1 = 16f * (x1 - dx);
		float v0 = 16f * (y0 + dy);
		float v1 = 16f * (y1 + dy);
	
		// Because Minecraft is weird, we also have to flip the y coordinates.
		float tmp = y0;
		y0 = 1f - y1;
		y1 = 1f - tmp;
	
		// Nudge the x and y coordinates slightly to prevent depth fighting
		float xNudge = (float) side.getXOffset() * nudge;
		float yNudge = (float) side.getYOffset() * nudge;
		x0 += xNudge;
		x1 += xNudge;
		y0 += yNudge;
		y1 += yNudge;
	
		LOGGER.trace("SIDE QUAD\n(x0,y0,z0): ({},{},{}) (x1,y1,z1): ({},{},{})\n(u0,v0): ({},{}) (u1,v1): ({},{})\n",
				x0, y0, z0, x1, y1, z1, u0, v0, u1, v1);
	
		return buildQuad(transform, side, sprite, tint, x0, y0, z0, sprite.getInterpolatedU(u0),
				sprite.getInterpolatedV(v0), x1, y1, z0, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1), x1,
				y1, z1, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1), x0, y0, z1,
				sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0));
	}

	private static boolean isVisible(TextureAtlasSprite sprite, int frame, int u, int v) {
		// If the coordinates are not on the sprite return false
		if (u < 0 || u >= sprite.getWidth() || v < 0 || v >= sprite.getHeight())
			return false;
		// Else look up the pixel
		return (sprite.getPixelRGBA(frame, u, v) >> 24 & 255) / 255f > 0.1f;
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
