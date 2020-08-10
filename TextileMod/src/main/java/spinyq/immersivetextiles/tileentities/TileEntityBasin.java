package spinyq.immersivetextiles.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import spinyq.immersivetextiles.util.Color;

public class TileEntityBasin extends TileEntity {

	private static final String KEY_DYE_LEVEL = "dyeLevel", KEY_COLOR = "color";
	
	private int dyeCapacity, dyeLevel;
	private Color color;
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		dyeLevel = compound.getInteger(KEY_DYE_LEVEL);
		color = Color.fromInt(compound.getInteger(KEY_COLOR));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger(KEY_DYE_LEVEL, dyeLevel);
		tag.setInteger(KEY_COLOR, color.toInt());
		return tag;
	}

	public double getConcentration() {
		return (double) dyeLevel / (double) dyeCapacity;
	}
	
	public Color getColor() {
		return color;
	}

}
