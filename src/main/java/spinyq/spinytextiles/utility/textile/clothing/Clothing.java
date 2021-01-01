package spinyq.spinytextiles.utility.textile.clothing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.NBTHelper.ClassIdSpace;
import spinyq.spinytextiles.utility.NBTHelper.ObjectMapper;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.fabric.NBTFabric;

public class Clothing implements INBTSerializable<CompoundNBT> {

	private static final String PATTERN_TAG = "Pattern", PART_DATA_TAG = "PartData";
	private static final ClassIdSpace CLASSES = new ClassIdSpace(NBTFabric.class);
	private static final ObjectMapper MAPPER = new ObjectMapper(CLASSES).withSupplier(NBTFabric.class, NBTFabric::new);
	private static final IForgeRegistry<ClothingPart> PART_REGISTRY = LazyForgeRegistry.of(ClothingPart.class);
	private static final IForgeRegistry<ClothingPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(ClothingPattern.class);

	private Map<ClothingPart, INBTSerializable<CompoundNBT>> partData = new HashMap<>();
	private ClothingPattern pattern;
	
	public Clothing(ClothingPattern pattern) {
		this.pattern = pattern;
	}
	
	// Needed for deserialization
	public Clothing() {
	}

	@SuppressWarnings("unchecked")
	public <T extends INBTSerializable<CompoundNBT>> T getPartData(ClothingPart part) {
		return (T) partData.get(part);
	}

	public <T extends INBTSerializable<CompoundNBT>> void setPartData(ClothingPart part, T data) {
		partData.put(part, data);
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		// Write the pattern
		NBTHelper.putRegistryEntry(nbt, PATTERN_TAG, pattern);
		// Write part data
		// Start constructing new compound NBT
		CompoundNBT mapNBT = new CompoundNBT();
		// For each entry in the map, write to the map nbt
		for (Entry<ClothingPart, INBTSerializable<CompoundNBT>> entry : partData.entrySet()) {
			// Get the key from the registry entry
			String key = entry.getKey().getRegistryName().toString();
			// Convert the value into NBT as well
			INBT valueNBT = NBTHelper.writePolymorphic(entry.getValue(), MAPPER);
			mapNBT.put(key, valueNBT);
		}
		// Put map nbt into the compound
		nbt.put(PART_DATA_TAG, mapNBT);
		// Done
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		// Read the pattern
		pattern = NBTHelper.getRegistryEntry(nbt, PATTERN_TAG, PATTERN_REGISTRY);
		// Retrieve the part data
		partData.clear();
		// Get the map NBT
		CompoundNBT mapNBT = nbt.getCompound(PART_DATA_TAG);
		// For each key in the map NBT, put the entry into the map
		for (String key : mapNBT.keySet()) {
			// Get the registry entry using the registry
			ClothingPart part = PART_REGISTRY.getValue(new ResourceLocation(key));
			// Deserialize the value
			INBTSerializable<CompoundNBT> value = NBTHelper.readPolymorphic(mapNBT.getCompound(key), MAPPER);
			// Put into map
			partData.put(part, value);
		}
		// Finished
	}

}
