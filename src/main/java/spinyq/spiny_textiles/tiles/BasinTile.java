package spinyq.spiny_textiles.tiles;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import spinyq.spiny_textiles.ModTiles;
import spinyq.spiny_textiles.util.Color;

public class BasinTile extends TileEntity {

	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
	}

	private static final String KEY_DYE_LEVEL = "dyeLevel", KEY_COLOR = "color";
	
	private int dyeCapacity, dyeLevel;
	private Color color;
	
	@Override
	public void read(CompoundNBT compound) {
		dyeLevel = compound.getInt(KEY_DYE_LEVEL);
		color = Color.fromInt(compound.getInt(KEY_COLOR));
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt(KEY_DYE_LEVEL, dyeLevel);
		tag.putInt(KEY_COLOR, color.toInt());
		return tag;
	}

	public double getConcentration() {
		return (double) dyeLevel / (double) dyeCapacity;
	}
	
	public Color getColor() {
		return color;
	}

}
