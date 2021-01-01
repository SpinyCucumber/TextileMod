package spinyq.spinytextiles.utility.textile.clothing;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IClothing {

	<T extends INBTSerializable<CompoundNBT>> T getPartData(ClothingPart part);
	<T extends INBTSerializable<CompoundNBT>> void setPartData(ClothingPart part, T data);
	ClothingPattern getPattern();
	void setPattern(ClothingPattern pattern);
	
}
