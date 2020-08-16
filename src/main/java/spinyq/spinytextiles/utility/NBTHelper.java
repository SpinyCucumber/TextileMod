package spinyq.spinytextiles.utility;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

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
	
}
