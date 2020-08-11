package spinyq.spiny_textiles.tiles;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import spinyq.spiny_textiles.ModTiles;
import spinyq.spiny_textiles.util.Color;

public class BasinTile extends TileEntity {

	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
	}

	private static final String KEY_WATER_LEVEL = "WaterLevel", KEY_DYE_LEVEL = "DyeLevel", KEY_COLOR = "Color";
	
	private int waterLevel, dyeLevel;
	private Color color;
	
	@Override
	public void read(CompoundNBT compound) {
		waterLevel = compound.getInt(KEY_WATER_LEVEL);
		dyeLevel = compound.getInt(KEY_DYE_LEVEL);
		color = Color.fromInt(compound.getInt(KEY_COLOR));
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt(KEY_WATER_LEVEL, waterLevel);
		tag.putInt(KEY_DYE_LEVEL, dyeLevel);
		tag.putInt(KEY_COLOR, color.toInt());
		return tag;
	}

	public double getDyeConcentration() {
		return (double) dyeLevel / (double) waterLevel;
	}
	
	public Color getColor() {
		return color;
	}

}
