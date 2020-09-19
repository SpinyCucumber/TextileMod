package spinyq.spinytextiles.utility;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class NBTHelper {

	/**
	 * Converts a collection to a ListNBT and writes it to a compound nbt
	 * @param <T>
	 * @param <K>
	 * @param nbt
	 * @param key
	 * @param collection
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
	 * Reads a ListNBT from a compound NBT and collects its values in a collection
	 * TODO Verify stuff
	 * @param <T>
	 * @param <K>
	 * @param collection
	 * @param nbt
	 * @param key
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
	
	public static <T extends INBTSerializable<K>, K extends INBT> void putOptional(CompoundNBT nbt, String key, Optional<T> optional) {
		if (optional.isPresent()) {
			nbt.put(key, optional.get().serializeNBT());
		}
	}
	
	// TODO Verify stuff
	@SuppressWarnings("unchecked")
	public static <T extends INBTSerializable<K>, K extends INBT> Optional<T> getOptional(Supplier<T> factory, CompoundNBT nbt, String key) {
		if (nbt.contains(key)) {
			T newObject = factory.get();
			newObject.deserializeNBT((K) nbt.get(key));
			return Optional.of(newObject);
		}
		else return Optional.empty();
	}
	
	/**
	 * Writes a map to a key of a CompoundNBT by creating another compound nbt and populating it with entries from the map.
	 * @param <T>
	 * @param <K>
	 * @param nbt
	 * @param key
	 * @param map
	 */
	public static <T extends INBTSerializable<K>, K extends INBT> void putMap(CompoundNBT nbt, String key, Map<String, T> map) {
		// Create sub-tag
		CompoundNBT mapNBT = new CompoundNBT();
		// Write each entry in map to the new tag
		map.entrySet().forEach((entry) -> {
			mapNBT.put(entry.getKey(), entry.getValue().serializeNBT());
		});
		// Add sub-tag to main tag
		nbt.put(key, mapNBT);
	}
	
	@SuppressWarnings("unchecked")
	public static <M extends Map<String, T>, T extends INBTSerializable<K>, K extends INBT> Map<String, T> getMap(Supplier<M> mapFactory, Supplier<T> objectFactory, CompoundNBT nbt, String key) {
		// Create new map
		M map = mapFactory.get();
		// Get sub-tag
		CompoundNBT mapNBT = nbt.getCompound(key);
		// Populate map with entries in sub-tag
		mapNBT.keySet().forEach((mapKey) -> {
			// Create new object and deserialize value
			T newObject = objectFactory.get();
			newObject.deserializeNBT((K) mapNBT.get(mapKey));
			// Add to map
			map.put(mapKey, newObject);
		});
		// Done
		return map;
	}
	
	public static <T> void putRegistryEntry(CompoundNBT nbt, String key, IForgeRegistryEntry<T> entry) {
		// TODO Find is Minecraft supports this already
		nbt.putString(key, entry.getRegistryName().toString());
	}
	
	public static <T extends IForgeRegistryEntry<T>> T getRegistryEntry(CompoundNBT nbt, String key, IForgeRegistry<T> registry) {
		// TODO Find is Minecraft supports this already
		return registry.getValue(new ResourceLocation(nbt.getString(key)));
	}
	
}
