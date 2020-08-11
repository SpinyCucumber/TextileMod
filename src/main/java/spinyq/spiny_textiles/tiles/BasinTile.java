package spinyq.spiny_textiles.tiles;

import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import spinyq.spiny_textiles.ModTiles;
import spinyq.spiny_textiles.util.Color3f;

public class BasinTile extends TileEntity {

	private static final Random RANDOM = new Random();
	
	public static final int MAX_WATER_LEVEL = 8, MAX_DYE_LEVEL = 8;
	
	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
		// TODO Remove this, just for testing
		waterLevel = MAX_WATER_LEVEL - 1;
		color = Color3f.random(RANDOM);
		dyeLevel = RANDOM.nextInt(MAX_DYE_LEVEL);
	}

	private static final String KEY_WATER_LEVEL = "WaterLevel", KEY_DYE_LEVEL = "DyeLevel", KEY_COLOR = "Color";
	
	private int waterLevel, dyeLevel;
	private Color3f color;
	
	@Override
	public void read(CompoundNBT compound) {
		waterLevel = compound.getInt(KEY_WATER_LEVEL);
		dyeLevel = compound.getInt(KEY_DYE_LEVEL);
		color = Color3f.fromInt(compound.getInt(KEY_COLOR));
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt(KEY_WATER_LEVEL, waterLevel);
		tag.putInt(KEY_DYE_LEVEL, dyeLevel);
		tag.putInt(KEY_COLOR, color.toInt());
		return tag;
	}

	public int getWaterLevel() {
		return waterLevel;
	}

	public int getDyeLevel() {
		return dyeLevel;
	}

	public float getDyeConcentration() {
		return (float) dyeLevel / (float) MAX_DYE_LEVEL;
	}
	
	public Color3f getColor() {
		return color;
	}

}
