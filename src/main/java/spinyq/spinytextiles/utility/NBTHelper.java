package spinyq.spinytextiles.utility;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.util.TriConsumer;

import com.google.common.collect.ImmutableMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Utility class that makes manipulating NBT easier.
 * @author Elijah Hilty
 *
 */
public class NBTHelper {

	public static final String TYPE_TAG = "Type", KEY_TAG = "Key", VALUE_TAG = "Value", DIRTY_TAG = "Dirty";
	
	public static class ClassIdSpace {
		
		private final ImmutableMap<Class<?>, Integer> map;
		private int size;

		public ClassIdSpace(Class<?>... classes) {
			int idCounter = 0;
			ImmutableMap.Builder<Class<?>, Integer> builder = new ImmutableMap.Builder<>();
			for (Class<?> clazz : classes) {
				builder.put(clazz, idCounter++);
			}
			map = builder.build();
			size = idCounter;
		}
		
	}
	
	public static class ObjectMapper {
		
		private Supplier<?>[] suppliers;
		private ClassIdSpace classIds;
		
		private ObjectMapper() { }
		
		public ObjectMapper(ClassIdSpace classIds) {
			this.classIds = classIds;
			this.suppliers = new Supplier<?>[classIds.size];
		}
		
		public <T> ObjectMapper withSupplier(Class<T> clazz, Supplier<T> supplier) {
			// Get id and associate id with supplier
			int id = classIds.map.get(clazz);
			suppliers[id] = supplier;
			return this;
		}
		
		public Object createObject(int id) {
			return suppliers[id].get();
		}
		
		public int getId(Object object) {
			return classIds.map.get(object.getClass());
		}
		
		public ObjectMapper copy() {
			ObjectMapper copy = new ObjectMapper();
			copy.classIds = classIds;
			copy.suppliers = suppliers.clone();
			return copy;
		}
		
	}
	
	public static class CalculatedValue<T> {
		
		private String tag;
		private BiFunction<CompoundNBT, String, T> getter;
		private TriConsumer<CompoundNBT, String, T> setter;
		private Function<ItemStack, T> calculator;
		
		private CalculatedValue(String tag, BiFunction<CompoundNBT, String, T> getter,
				TriConsumer<CompoundNBT, String, T> setter, Function<ItemStack, T> calculator) {
			this.tag = tag;
			this.getter = getter;
			this.setter = setter;
			this.calculator = calculator;
		}
		
		public T get(ItemStack item) {
			T value;
			// Check if dirty
			// If the value is dirty, we have to recalculate it
			CompoundNBT compound = item.getOrCreateChildTag(tag);
			if (isDirty(item)) {
				value = calculator.apply(item);
				// Remove dirty flag
				compound.putBoolean(DIRTY_TAG, false);
				// Store value
				setter.accept(compound, VALUE_TAG, value);
			}
			else {
				// Retrieve stored value
				value = getter.apply(compound, VALUE_TAG);
			}
			return value;
		}

		public void markDirty(ItemStack item) {
			// Set the dirty flag
			item.getOrCreateChildTag(tag).putBoolean(DIRTY_TAG, true);
		}
		
		private boolean isDirty(ItemStack item) {
			CompoundNBT compound = item.getOrCreateChildTag(tag);
			boolean dirty;
			if (!compound.contains(DIRTY_TAG)) {
				dirty = true;
				compound.putBoolean(DIRTY_TAG, dirty);
			}
			else {
				dirty = compound.getBoolean(DIRTY_TAG);
			}
			return dirty;
		}
		
	}
	
	public static CalculatedValue<String> createCalculatedStringValue(String tag, Function<ItemStack, String> calculator) {
		return new CalculatedValue<>(tag, CompoundNBT::getString, CompoundNBT::putString, calculator);
	}
	
	public static <T extends Enum<T>> CalculatedValue<T> createCalculatedEnumValue(String tag, Class<T> clazz, Function<ItemStack, T> calculator) {
		return new CalculatedValue<T>(tag,
				(nbt, key) -> {
					return clazz.getEnumConstants()[nbt.getInt(key)];
				},
				(nbt, key, value) -> {
					nbt.putInt(key, value.ordinal());
				},
				calculator);
	}
	
	public static <T extends INBTSerializable<K>, K extends INBT> void put(CompoundNBT nbt, String key, T object) {
		nbt.put(key, object.serializeNBT());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends INBTSerializable<K>, K extends INBT> T get(CompoundNBT nbt, String key, Supplier<T> supplier) {
		T object = supplier.get();
		object.deserializeNBT((K) nbt.get(key));
		return object;
	}

	/**
	 * Writes a polymorphic object to a Compount NBT tag.
	 * @param <T> The type of the polymorphic NBT object
	 * @param nbt The compound nbt
	 * @param key The key containing the object
	 * @param object The polymorphic NBT object
	 */
	public static <T extends INBTSerializable<CompoundNBT>> void putPolymorphic(CompoundNBT nbt, String key, T object,
			ObjectMapper mapper) {
		nbt.put(key, writePolymorphic(object, mapper));
	}
	
	/**
	 * Reads a polymorphic object from a Compound NBT tag.
	 * @param <T> The type of the polymorphic NBT object
	 * @param nbt The compound nbt
	 * @param key The key containing the object
	 */
	public static <T extends INBTSerializable<CompoundNBT>> T getPolymorphic(CompoundNBT nbt, String key, ObjectMapper mapper) {
		return readPolymorphic(nbt.getCompound(key), mapper);
	}
	
	public static <T extends INBTSerializable<CompoundNBT>> CompoundNBT writePolymorphic(T object,
			ObjectMapper mapper) {
		// Create a new compound NBT and write type ID
		CompoundNBT objectNBT = object.serializeNBT();
		int id = mapper.getId(object);
		objectNBT.putInt(TYPE_TAG, id);
		return objectNBT;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends INBTSerializable<CompoundNBT>> T readPolymorphic(CompoundNBT objectNBT, ObjectMapper mapper) {
		// Read the object's type and create a new object
		int id = objectNBT.getInt(TYPE_TAG);
		T object = (T) mapper.createObject(id);
		// Deserialize and return object
		object.deserializeNBT(objectNBT);
		return object;
	}

	/**
	 * Converts a collection to a ListNBT and writes it to a compound nbt
	 * @param <T> The type that the collection contains
	 * @param <K> The type of NBT that the collection values serialize to
	 * @param nbt The compound NBT
	 * @param key The key containing the collection
	 * @param collection The collection
	 */
	public static <T extends INBTSerializable<K>, K extends INBT> void putCollection(CompoundNBT nbt, String key, Collection<T> collection) {
		// Construct a list and add each serialized element to it
		ListNBT list = new ListNBT();
		collection.forEach((object) -> {
			list.add(object.serializeNBT());
		});
		// Put list into compound
		nbt.put(key, list);
	}
	
	/**
	 * Reads a collection from a compound NBT. The key must correspond to a ListNBT
	 * @param <C> The type of collection
	 * @param <T> The type of that the collection contains
	 * @param <K> The type of NBT that the collection values serialize to
	 * @param factoryC A factory to create an initial collection
	 * @param factoryT A factory to create empty objects to deserialize to
	 * @param nbt The compound NBT
	 * @param key The key containing the collection
	 */
	@SuppressWarnings("unchecked")
	public static <C extends Collection<T>, T extends INBTSerializable<K>, K extends INBT> C getCollection(Supplier<C> factoryC, Supplier<T> factoryT, CompoundNBT nbt, String key) {
		// Get a ListNBT and send its values to the collection
		C collection = factoryC.get();
		ListNBT list = (ListNBT) nbt.get(key);
		list.forEach((listItem) -> {
			T newObject = factoryT.get();
			newObject.deserializeNBT((K) listItem);
			collection.add(newObject);
		});
		return collection;
	}
	
	/**
	 * Writes a nullable value to a compound NBT
	 * @param <T> The type the optional contains
	 * @param <K> The type that the value deserializes to
	 * @param nbt The compound NBT
	 * @param key The key
	 */
	public static <T extends INBTSerializable<K>, K extends INBT> void putNullable(CompoundNBT nbt, String key, T value) {
		CompoundNBT compound = new CompoundNBT();
		if (value != null) compound.put(VALUE_TAG, value.serializeNBT());
		nbt.put(key, compound);
	}
	
	/**
	 * Reads an nullable value from a compound NBT
	 * @param <T> The nullable type
	 * @param <K> The type that the value deserializes to
	 * @param factory A factory to create a new object if the value is present
	 * @param nbt The compound NBT
	 * @param key The key
	 */
	@SuppressWarnings("unchecked")
	public static <T extends INBTSerializable<K>, K extends INBT> T getNullable(Supplier<T> factory, CompoundNBT nbt, String key) {
		CompoundNBT compound = nbt.getCompound(key);
		if (compound.contains(VALUE_TAG)) {
			T newObject = factory.get();
			newObject.deserializeNBT((K) compound.get(VALUE_TAG));
			return newObject;
		}
		else return null;
	}
	
	/**
	 * Reads a value from a compound NBT, or returns null if the value is not present.
	 * This should be distinguished from getNullable, where null represents a "valid state"
	 * such as the absence of a value.
	 * @param <T> The object type
	 * @param <K> The type that the value deserializes to
	 * @param factory A factory to create a new object if the value is present
	 * @param nbt The compound NBT
	 * @param key The key
	 */
	@SuppressWarnings("unchecked")
	public static <T extends INBTSerializable<K>, K extends INBT> T getOrNull(Supplier<T> factory, CompoundNBT nbt, String key) {
		if (nbt.contains(key)) {
			T newObject = factory.get();
			newObject.deserializeNBT((K) nbt.get(key));
			return newObject;
		}
		else return null;
	}
	
	public static CompoundNBT getOrCreate(CompoundNBT nbt, String key) {
		CompoundNBT result;
		if (!nbt.contains(key, 10)) {
			result = new CompoundNBT();
			nbt.put(key, result);
		}
		else {
			result = nbt.getCompound(key);
		}
		return result;
	}
	
	/**
	 * Writes a registry entry to a compound NBT by writing the resource location
	 * @param <T> The type of the registry entry
	 * @param nbt The compound NBT
	 * @param key The key containing the registry entry
	 * @param entry
	 */
	public static <T> void putRegistryEntry(CompoundNBT nbt, String key, IForgeRegistryEntry<T> entry) {
		nbt.putString(key, entry.getRegistryName().toString());
	}
	
	/**
	 * Reads a registry entry from a compound NBT by reading a resource location. Must provide a registry as well to promote caching.
	 * @param <T> The type of the registry entry
	 * @param nbt The compound NBT
	 * @param key The key containing the registry entry
	 * @param registry The Forge Registry the contains the registry entry
	 */
	public static <T extends IForgeRegistryEntry<T>> T getRegistryEntry(CompoundNBT nbt, String key, IForgeRegistry<T> registry) {
		return registry.getValue(new ResourceLocation(nbt.getString(key)));
	}
	
}
