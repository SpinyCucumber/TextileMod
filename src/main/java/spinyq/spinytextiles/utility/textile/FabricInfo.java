package spinyq.spinytextiles.utility.textile;

import net.minecraft.nbt.CompoundNBT;

public class FabricInfo implements IGarmentComponent {

	@Override
	public CompoundNBT serializeNBT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Type<?> getType() {
		return IGarmentComponent.FABRIC;
	}

}
