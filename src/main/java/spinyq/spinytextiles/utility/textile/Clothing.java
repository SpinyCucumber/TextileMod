package spinyq.spinytextiles.utility.textile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class Clothing implements INBTSerializable<CompoundNBT> {

	private Map<ClothingPart, Object> partData = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public <T> T getPartData(ClothingPart part) {
		return (T) partData.get(part);
	}
	
	public <T> void setPartData(ClothingPart part, T data) {
		partData.put(part, data);
	}
	
	@Override
	public CompoundNBT serializeNBT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		// TODO Auto-generated method stub
		
	}

}
