package spinyq.spiny_textiles.tiles;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import spinyq.spiny_textiles.ModTiles;
import spinyq.spiny_textiles.util.Color3f;

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
	 * Sets the water level. Sends data to clients.
	 * 
	 * @param waterLevel
	 */
	public void setWaterLevel(int waterLevel) {
		this.waterLevel = waterLevel;
		update();
	}

	/**
	 * Sets the dye level. Sends data to clients.
	 * 
	 * @param dyeLevel
	 */
	public void setDyeLevel(int dyeLevel) {
		this.dyeLevel = dyeLevel;
		update();
	}

	/**
	 * Mixes a new dye into the basin. This increases the dye concentration and
	 * changes the color.
	 * 
	 * @param dyeColor
	 */
	public void mixDye(Color3f dyeColor) {
		// TODO Add mixing algorithm
		this.color = dyeColor;
		this.dyeLevel++;
		update();
	}

	/**
	 * Sets the water level to the max.
	 */
	public void fill() {
		setWaterLevel(MAX_WATER_LEVEL - 1);
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
		return (float) dyeLevel / (float) MAX_DYE_LEVEL;
	}

	public Color3f getColor() {
		return color;
	}

}
