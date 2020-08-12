package spinyq.spinytextiles.tiles;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.utility.Color3f;
import spinyq.spinytextiles.utility.Color3f.HSV;
import spinyq.spinytextiles.utility.ColorHelper;
import spinyq.spinytextiles.utility.Dyeable;

public class BasinTile extends TileEntity {

	public static final int MAX_WATER_LEVEL = 9, MAX_DYE_LEVEL = 9;

	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
		waterLevel = 0;
		color = new Color3f();
		dyeLevel = 0;
	}

	private static final String KEY_WATER_LEVEL = "WaterLevel", KEY_DYE_LEVEL = "DyeLevel", KEY_COLOR = "Color";

	private int waterLevel, dyeLevel;
	private Color3f color;

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		waterLevel = compound.getInt(KEY_WATER_LEVEL);
		dyeLevel = compound.getInt(KEY_DYE_LEVEL);
		color = Color3f.fromInt(compound.getInt(KEY_COLOR));
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		CompoundNBT result = super.write(tag);
		result.putInt(KEY_WATER_LEVEL, waterLevel);
		result.putInt(KEY_DYE_LEVEL, dyeLevel);
		result.putInt(KEY_COLOR, color.toInt());
		return result;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbtTag = new CompoundNBT();
		// Write your data into the nbtTag
		write(nbtTag);
		return new SUpdateTileEntityPacket(getPos(), -1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		CompoundNBT tag = pkt.getNbtCompound();
		// Handle your Data
		read(tag);
	}

	/*
	 * Creates a tag containing all of the TileEntity information, used by vanilla
	 * to transmit from server to client
	 */
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbtTagCompound = new CompoundNBT();
		write(nbtTagCompound);
		return nbtTagCompound;
	}

	/*
	 * Populates this TileEntity with information from the tag, used by vanilla to
	 * transmit from server to client
	 */
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		read(tag);
	}

	/**
	 * Syncs data with clients and marks this tile to be saved.
	 */
	private void update() {
		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);
		markDirty();
	}

	public int getWaterLevel() {
		return waterLevel;
	}

	public int getDyeLevel() {
		return dyeLevel;
	}

	/**
	 * Mixes a new dye into the basin. This increases the dye concentration and
	 * changes the color.
	 * 
	 * @param dyeColor
	 */
	public void mixDye(Color3f dyeColor) {
		// Mix the existing color with the new
		color = ColorHelper.mixRealistic(color, dyeColor, 1.0f / (float) (dyeLevel + 1));
		this.dyeLevel++;
		update();
	}
	
	/**
	 * Dyes an item, also consuming some water.
	 * @param stack
	 * @param dyeable
	 */
	public <T,C> void dye(T object, C context, Dyeable<T,C> dyeable) {
		// Calculate the new color by mixing the current color of the object and the basin's color
		// Interpolate between them using the dye concentration
		Color3f newColor = ColorHelper.mixRealistic(dyeable.getColor(object), color, getDyeConcentration());
		dyeable.setColor(object, context, newColor);
		int cost = dyeable.getDyeCost();
		if (waterLevel >= cost) drain(cost);
		else throw new RuntimeException("Attempted to dye an item without enough dye.");
	}

	/**
	 * Sets the water level to the max.
	 */
	public void fill() {
		waterLevel = MAX_WATER_LEVEL - 1;
		update();
	}
	
	/**
	 * Drains the specified amount of water from the basin.
	 * If the basin is emptied, its color and dye level is reset.
	 * @param amt
	 */
	public void drain(int amt) {
		if (waterLevel >= amt) {
			waterLevel -= amt;
			if (waterLevel == 0) {
				dyeLevel = 0;
				color = new Color3f();
			}
			update();
		}
		else throw new RuntimeException("Attempted to drain non-existent water.");
	}
	
	/**
	 * Boosts the saturation of the Basin's color.
	 * The current color is converted to HSV space,
	 * where 'amt' is added to the saturation, then converted back.
	 * The saturation won't exceed 1.0.
	 * @param amt
	 */
	public void boostColorSaturation(float amt) {
		HSV hsv = new HSV();
		color.toHSV(hsv);
		hsv.sat = Math.min(hsv.sat + amt, 1.0f);
		// Convert back to rgb
		color.fromHSV(hsv);
		update();
	}
	
	/**
	 * Whether the color saturation can be boosted further.
	 * Any saturation below 1.0 can be boosted.
	 */
	public boolean canBoostSaturation(float amt) {
		HSV hsv = new HSV();
		color.toHSV(hsv);
		return hsv.sat < 1.0f;
	}
	
	public boolean canDye(Dyeable<?,?> dyeable) {
		return (waterLevel >= dyeable.getDyeCost());
	}
	
	/**
	 * Whether the basin is heated. Heat is required to dye objects.
	 * @return
	 */
	public boolean isHeated() {
		return CampfireBlock.isLitCampfireInRange(world, pos, 2);
	}

	/**
	 * Whether the basin contains the maximum amount of water.
	 * 
	 * @return
	 */
	public boolean isFull() {
		return waterLevel == (MAX_WATER_LEVEL - 1);
	}

	/**
	 * Whether the basin contains any water.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return waterLevel == 0;
	}

	/**
	 * Whether the basin is "fully saturated". A saturated basin can't accept any
	 * more dyes.
	 * 
	 * @return
	 */
	public boolean isSaturated() {
		return dyeLevel == (MAX_DYE_LEVEL - 1);
	}

	public float getDyeConcentration() {
		return (float) dyeLevel / (float) (MAX_DYE_LEVEL - 1);
	}

	public Color3f getColor() {
		return color;
	}

}
