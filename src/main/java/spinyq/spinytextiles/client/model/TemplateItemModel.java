package spinyq.spinytextiles.client.model;

import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;

// TODO Improvement: Let this class implement IModelGeometry, similar to ItemLayerModel.
// TODO Improvement: Skipping internal side quads would allow us to cut down on quads.
// Should rewrite this class eventually with these to things in mind.
// Most of this code is adapted from ItemLayerModel, with additional comments to clarify things.
// Right now it only contains static utility methods
public class TemplateItemModel {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Level LOG_LEVEL = Level.INFO;
	
	private static final Direction[] HORIZONTALS = {Direction.UP, Direction.DOWN};
    private static final Direction[] VERTICALS = {Direction.WEST, Direction.EAST};

    public static void generateQuads(int tint, TextureAtlasSprite template,
    		TextureAtlasSprite sprite, TransformationMatrix transform, ImmutableList.Builder<BakedQuad> builder)
    {

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
        boolean translucent = true; // set to true for testing

        LOGGER.log(LOG_LEVEL, "Scanning for edges...");
        
        // The following section calculates the faceData for the sprite.
        for(int f = 0; f < sprite.getFrameCount(); f++)
        {
        	// These variables keep track of pixel transparency.
        	// ptu tracks whether the previous pixel is transparent,
        	// and ptv keeps track of the transparency of the previous row of pixels.
            boolean ptu;
            boolean[] ptv = new boolean[uMax];
            Arrays.fill(ptv, true);
            // Iterate over each pixel, reading each row from left-to-right starting with the top row.
            for(int v = 0; v < vMax; v++)
            {
                ptu = true;
                for(int u = 0; u < uMax; u++)
                {
                	// In addition to checking if the sprite's pixel is transparent,
                	// we also check whether the template's pixel is transparent.
                	// If either of them is transparent, then the current pixel is transparent.
                    int alpha = sprite.getPixelRGBA(f, u, v) >> 24 & 0xFF;
                    boolean t = (alpha / 255f <= 0.1f) || template.isPixelTransparent(0, u, v);
                    
                    if (!t && alpha < 255)
                    {
                        translucent = true;
                    }

                    // If we've just moved from a transparent pixel to an opaque pixel,
                    // we've found a side facing to the left. (west)
                    if(ptu && !t) // left - transparent, right - opaque
                    {
                    	LOGGER.log(LOG_LEVEL, "Pixel at ({}, {}) has a left edge.", u, v);
                        faceData.set(Direction.WEST, u, v);
                    }
                    // Similarly, if we've moved from an opaque pixel to a transparent one,
                    // we've found a side facing to the right. (east)
                    if(!ptu && t) // left - opaque, right - transparent
                    {
                    	LOGGER.log(LOG_LEVEL, "Pixel at ({}, {}) has a right edge.", u-1, v);
                        faceData.set(Direction.EAST, u-1, v);
                    }
                    // Next, check the pixel to the top of the current one.
                    // If its transparent and the current pixel is not, we've found
                    // a side facing upwards. Vice versa for a downwards facing side.
                    if(ptv[u] && !t) // up - transparent, down - opaque
                    {
                    	LOGGER.log(LOG_LEVEL, "Pixel at ({}, {}) has an up edge.", u, v);
                        faceData.set(Direction.UP, u, v);
                    }
                    if(!ptv[u] && t) // up - opaque, down - transparent
                    {
                    	LOGGER.log(LOG_LEVEL, "Pixel at ({}, {}) has a down edge.", u, v-1);
                        faceData.set(Direction.DOWN, u, v-1);
                    }

                    ptu = t;
                    ptv[u] = t;
                }
                // If the last pixel in the row is opaque, then it also has a side to the right. (east)
                if(!ptu) // last - opaque
                {
                    faceData.set(Direction.EAST, uMax-1, v);
                }
            }
            // For every pixel in the final row, if it is opaque,
            // then it also has a side facing down.
            // last line
            for(int u = 0; u < uMax; u++)
            {
                if(!ptv[u])
                {
                    faceData.set(Direction.DOWN, u, vMax-1);
                }
            }
        }

        // The following section generates horizontal side quads.
        // Note that these quads are horizontally aligned, rather than facing a horizontal.
        // They face either up or down. Confusing, the HORIZONTALS variable contains the
        // directions UP and DOWN.
        // The process is performed for both the UP and DOWN direction.
        
        LOGGER.log(LOG_LEVEL, "Generating side quads...");
        
        // horizontal quads
        for (Direction facing : HORIZONTALS)
        {
        	// Iterate over each row of pixels.
            for (int v = 0; v < vMax; v++)
            {
            	// For each row, we keep track of whether we are currently building a quad.
            	// We also keep track of the start of the current quad, and the end of the current quad.
            	// These are only relevant if we are building a quad.
                int uStart = 0, uEnd = uMax;
                boolean building = false;
                // Iterate over every pixel in the row
                for (int u = 0; u < uMax; u++)
                {
                	// Retrieve whether the current pixel has a side or not 
                    boolean face = faceData.get(facing, u, v);
                    
                    // If the sprite doesn't contain any translucent pixels, we can afford to
                    // only worry about the first and last sides in the row of pixels.
                    // Why? Consider three possible cases:
                    // 1. The side is totally straight, in which case we would be fine caring about
                    // the geometry in the middle or not.
                    // 2. The side is convex; it has a "bump" in the middle. If the sprite doesn't have translucence,
                    // then we are okay generating only one quad for the row, since any bit of the quad below
                    // the "bump" is hidden by the front and back cover quads. If the sprite does have translucense,
                    // however, the player might see some weirdness going on.
                    // 3. The side is concave; it has a "dip" in the middle. We are okay generating only
                    // one quad no matter the translucense of the sprite. This is because the texture of the
                    // side quad is the same as the row of pixels it borders, so any transparent pixels
                    // won't show up on the side.
                    // Note that a side can have both a "bump" and a "dip."
                    // We only care about the second case, so all we need to do is track whether or not
                    // the sprite has translucency.
                    
                    if (!translucent)
                    {
                        if (face)
                        {
                            if (!building)
                            {
                                building = true;
                                uStart = u;
                            }
                            uEnd = u + 1;
                        }
                    }
                    else
                    {
                    	// If we're building a quad and the current pixel doesn't have a side,
                    	// we finish the current quad and send it to the list.
                        if (building && !face) // finish current quad
                        {
                            // make quad [uStart, u]
                            int off = facing == Direction.DOWN ? 1 : 0;
                        	LOGGER.log(LOG_LEVEL, "Building a horizontal quad facing {} at row v={} with start u={} and end u={}",
                        			facing, v, uStart, u);
                            builder.add(buildSideQuad(transform, facing, tint, sprite, uStart, v+off, u-uStart));
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
                }
                // If we're still building a quad at the end of the row,
                // finish the current quad and send it to the list.
                if (building) // build remaining quad
                {
                    // make quad [uStart, uEnd]
                    int off = facing == Direction.DOWN ? 1 : 0;
                	LOGGER.log(LOG_LEVEL, "Building a horizontal quad facing {} at row v={} with start u={} and end u={}",
                			facing, v, uStart, uEnd);
                    builder.add(buildSideQuad(transform, facing, tint, sprite, uStart, v+off, uEnd-uStart));
                }
            }
        }

        // This does the same thing as the above section, but for vertical quads instead.
        // This means scanning the WEST and EAST sides of each pixel and building quads
        // for contiguous sides.
        
        // vertical quads
        for (Direction facing : VERTICALS)
        {
            for (int u = 0; u < uMax; u++)
            {
                int vStart = 0, vEnd = vMax;
                boolean building = false;
                for (int v = 0; v < vMax; v++)
                {
                    boolean face = faceData.get(facing, u, v);
                    if (!translucent)
                    {
                        if (face)
                        {
                            if (!building)
                            {
                                building = true;
                                vStart = v;
                            }
                            vEnd = v + 1;
                        }
                    }
                    else
                    {
                        if (building && !face) // finish current quad
                        {
                            // make quad [vStart, v]
                            int off = facing == Direction.EAST ? 1 : 0;
                        	LOGGER.log(LOG_LEVEL, "Building a vertical quad facing {} at column u={} with start v={} and end v={}",
                        			facing, u, vStart, v);
                            builder.add(buildSideQuad(transform, facing, tint, sprite, u+off, vStart, v-vStart));
                            building = false;
                        }
                        else if (!building && face) // start new quad
                        {
                            building = true;
                            vStart = v;
                        }
                    }
                }
                if (building) // build remaining quad
                {
                    // make quad [vStart, vEnd]
                    int off = facing == Direction.EAST ? 1 : 0;
                	LOGGER.log(LOG_LEVEL, "Building a vertical quad facing {} at column u={} with start v={} and end v={}",
                			facing, u, vStart, vEnd);
                    builder.add(buildSideQuad(transform, facing, tint, sprite, u+off, vStart, vEnd-vStart));
                }
            }
        }

        LOGGER.log(LOG_LEVEL, "Generating face quads...");
        
        // Finally, we build the "cover" front and back quads.
        // We let ItemTextureQuadConverter handle this.
        builder.addAll(ItemTextureQuadConverter.convertTexture(transform, template, sprite,
        		7.5f / 16f, Direction.NORTH, 0xffffffff, tint));
        builder.addAll(ItemTextureQuadConverter.convertTexture(transform, template, sprite,
        		8.5f / 16f, Direction.SOUTH, 0xffffffff, tint));
    }

    private static class FaceData
    {
        private final EnumMap<Direction, BitSet> data = new EnumMap<>(Direction.class);

        private final int vMax;

        FaceData(int uMax, int vMax)
        {
            this.vMax = vMax;

            data.put(Direction.WEST, new BitSet(uMax * vMax));
            data.put(Direction.EAST, new BitSet(uMax * vMax));
            data.put(Direction.UP,   new BitSet(uMax * vMax));
            data.put(Direction.DOWN, new BitSet(uMax * vMax));
        }

        public void set(Direction facing, int u, int v)
        {
            data.get(facing).set(getIndex(u, v));
        }

        public boolean get(Direction facing, int u, int v)
        {
            return data.get(facing).get(getIndex(u, v));
        }

        private int getIndex(int u, int v)
        {
            return v * vMax + u;
        }
    }

    private static BakedQuad buildSideQuad(TransformationMatrix transform, Direction side, int tint, TextureAtlasSprite sprite, int u, int v, int size)
    {
        final float eps = 1e-2f;

        int width = sprite.getWidth();
        int height = sprite.getHeight();

        // These describe the position of the side quad.
        float x0 = (float) u / width;
        float y0 = (float) v / height;
        float x1 = x0, y1 = y0;
        float z0 = 7.5f / 16f, z1 = 8.5f / 16f;

        // Switch statements can be unclear.
        // In this case, the line y1 = (float) (v + size) / height line is executed
        // for both the WEST and EAST sides since the WEST branch doesn't have a break statement.
        // Similarly, the x1 = (float) (u + size) / width line is executed for
        // both the DOWN and UP sides.
        switch(side)
        {
        // If the direction is either WEST or DOWN we have to flip the z coordinates
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
        case DOWN:
            z0 = 8.5f / 16f;
            z1 = 7.5f / 16f;
        case UP:
            x1 = (float) (u + size) / width;
            break;
        default:
            throw new IllegalArgumentException("can't handle z-oriented side");
        }

        float dx = side.getDirectionVec().getX() * eps / width;
        float dy = side.getDirectionVec().getY() * eps / height;

        float u0 = 16f * (x0 - dx);
        float u1 = 16f * (x1 - dx);
        float v0 = 16f * (1f - y0 - dy);
        float v1 = 16f * (1f - y1 - dy);

        return buildQuad(
            transform, remap(side), sprite, tint,
            x0, y0, z0, sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0),
            x1, y1, z0, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1),
            x1, y1, z1, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1),
            x0, y0, z1, sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0)
        );
    }

    private static Direction remap(Direction side)
    {
        // getOpposite is related to the swapping of V direction
        return side.getAxis() == Direction.Axis.Y ? side.getOpposite() : side;
    }

    private static BakedQuad buildQuad(TransformationMatrix transform, Direction side, TextureAtlasSprite sprite, int tint,
        float x0, float y0, float z0, float u0, float v0,
        float x1, float y1, float z1, float u1, float v1,
        float x2, float y2, float z2, float u2, float v2,
        float x3, float y3, float z3, float u3, float v3)
    {
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

    private static void putVertex(IVertexConsumer consumer, Direction side, float x, float y, float z, float u, float v)
    {
        VertexFormat format = consumer.getVertexFormat();
        for(int e = 0; e < format.getElements().size(); e++)
        {
            switch(format.getElements().get(e).getUsage())
            {
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
                if(format.getElements().get(e).getIndex() == 0)
                {
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
