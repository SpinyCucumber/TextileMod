package spinyq.spinytextiles.utility.textile;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IGarmentComponent extends INBTSerializable<CompoundNBT> {

	public static class Type<T extends IGarmentComponent> {
		
		private String id;
		private Supplier<T> factory;
		private Class<T> clazz;
		
		public Type(String id, Supplier<T> factory, Class<T> clazz) {
			this.id = id;
			this.factory = factory;
			this.clazz = clazz;
		}
		
		public boolean match(T component) {
			return clazz.isAssignableFrom(component.getClass());
		}
		
	}
	
	public static final String TAG_ID = "Id";
	
	public static final Type<FiberInfo> FIBER = new Type<>("Fiber", FiberInfo::new, FiberInfo.class);
	public static final Type<FabricInfo> FABRIC = new Type<>("Fabric", FabricInfo::new, FabricInfo.class);
	
	public static final ImmutableList<Type<?>> TYPES = ImmutableList.of(FIBER, FABRIC);
	
	public static Type<?> getType(String id) {
		for (Type<?> type : TYPES) {
			if (type.id.equals(id)) return type;
		}
		throw new RuntimeException(String.format("'%s' is not a valid garment component type.", id));
	}
	
	public static IGarmentComponent deserialize(CompoundNBT nbt) {
		// Retrieve type of garment component using id
		String id = nbt.getString(TAG_ID);
		Type<?> type = getType(id);
		// Create new garment component using type and pass over the deserialization logic
		IGarmentComponent component = type.factory.get();
		component.deserializeNBT(nbt);
		// Done
		return component;
	}
	
	public static CompoundNBT serialize(IGarmentComponent component) {
		// Let component create new nbt tag
		CompoundNBT nbt = component.serializeNBT();
		// Write ID of component's type
		nbt.putString(TAG_ID, component.getType().id);
		// Done
		return nbt;
	}
	
	Type<?> getType();
		
}
