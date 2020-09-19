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
	 * Writes an Optional to a compound NBT
	 * @param <T> The type the optional contains
	 * @param <K> The type that the value deserializes to
	 * @param nbt The compound NBT
	 * @param key The key containing the Optional
	 */
	public static <T extends INBTSerializable<K>, K extends INBT> void putOptional(CompoundNBT nbt, String key, Optional<T> optional) {
		if (optional.isPresent()) {
			nbt.put(key, optional.get().serializeNBT());
		}
	}
	
	/**
	 * Reads an Optional type from a compound NBT
	 * @param <T> The type the optional contains
	 * @param <K> The type that the value deserializes to
	 * @param factory A factory to create a new object if the optional is present
	 * @param nbt The compound NBT
	 * @param key The key containing the Optional
	 */
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
	 * @param <T> The type of the map's values
	 * @param <K> The NBT type that the values serialize to
	 * @param nbt The compound NBT
	 * @param key The key that corresponds to the map
	 * @param map The map
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
	
	/**
	 * Reads a map from a key of a Compound NBT, where the map is stored as another compound NBT.
	 * @param <M> The type of map
	 * @param <T> The type of the map's values
	 * @param <K> The NBT type that the values serialize to
	 * @param mapFactory A factory to create the initial map
	 * @param objectFactory A factory to create empty objects, which the map's values will be deserialized to
	 * @param nbt The compound NBT
	 * @param key The key containing the map
	 * @return
	 */
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
	
	/**
	 * Writes a registry entry to a compound NBT by writing the resource location
	 * @param <T> The type of the registry entry
	 * @param nbt The compound NBT
	 * @param key The key containing the registry entry
	 * @param entry
	 */
	public static <T> void putRegistryEntry(CompoundNBT nbt, String key, IForgeRegistryEntry<T> entry) {
		// TODO Find is Minecraft supports this already
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
		// TODO Find is Minecraft supports this already
		return registry.getValue(new ResourceLocation(nbt.getString(key)));
	}
	
}
