package spinyq.spinytextiles.utility.textile;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IGarmentComponent extends INBTSerializable<CompoundNBT> {

	public static class Type<T extends IGarmentComponent> {
		
		String id;
		Supplier<T> factory;
		
		public Type(String id, Supplier<T> factory) {
			this.id = id;
			this.factory = factory;
		}
		
	}
	
	Type<?> getType();
		
}
