package spinyq.spinytextiles.utility.textile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import spinyq.spinytextiles.utility.textile.IGarmentComponent.Type;

/**
 * Keeps track of the different types of garment components, and supports polymorphic serialization/deserialization of garment components.
 * @author Elijah Hilty
 *
 */
public class ComponentManager {

	public static final String TAG_ID = "Id";

	private static final Map<String, Type<?>> TYPES = new HashMap<>();
	
	public static final Type<FiberInfo> FIBER = register(new Type<>("Fiber", FiberInfo::new));
	public static final Type<FabricInfo> FABRIC = register(new Type<>("Fabric", FabricInfo::new));

	private static <T extends IGarmentComponent> Type<T> register(Type<T> type) {
		// Add the component type to the registry
		TYPES.put(type.id, type);
		return type;
	}

	public static Type<?> getType(String id) {
		return TYPES.get(id);
	}

	public static IGarmentComponent deserialize(CompoundNBT nbt) {
		// Retrieve type of garment component using id
		String id = nbt.getString(TAG_ID);
		Type<?> type = getType(id);
		// Create new garment component using type and pass over the deserialization
		// logic
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

}
