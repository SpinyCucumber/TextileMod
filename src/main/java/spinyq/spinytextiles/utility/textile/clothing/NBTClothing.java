package spinyq.spinytextiles.utility.textile.clothing;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.NBTHelper.ClassIdSpace;
import spinyq.spinytextiles.utility.NBTHelper.ObjectMapper;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.fabric.NBTFabric;

public class NBTClothing implements IClothing, INBTSerializable<CompoundNBT> {

	private static final String PATTERN_TAG = "Pattern", PART_DATA_TAG = "PartData";
	private static final ClassIdSpace CLASSES = new ClassIdSpace(NBTFabric.class);
	private static final ObjectMapper MAPPER = new ObjectMapper(CLASSES).withSupplier(NBTFabric.class, NBTFabric::new);
	private static final IForgeRegistry<ClothingPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(ClothingPattern.class);

	private CompoundNBT nbt;
	
	// Needed for deserialization
	public NBTClothing() {
	}
	
	public NBTClothing(CompoundNBT nbt) {
		this.nbt = nbt;
	}

	@Override
	public ClothingPattern getPattern() {
		return NBTHelper.getRegistryEntry(nbt, PATTERN_TAG, PATTERN_REGISTRY);
	}

	@Override
	public void setPattern(ClothingPattern pattern) {
		NBTHelper.putRegistryEntry(nbt, PATTERN_TAG, pattern);
	}

	public <T extends INBTSerializable<CompoundNBT>> T getPartData(ClothingPart part) {
		// Get key using resource location of clothing part
		String key = part.getRegistryName().toString();
		return NBTHelper.getPolymorphic(NBTHelper.getOrCreate(nbt, PART_DATA_TAG), key, MAPPER);
	}

	public <T extends INBTSerializable<CompoundNBT>> void setPartData(ClothingPart part, T data) {
		// Get key using resource location of clothing part
		String key = part.getRegistryName().toString();
		NBTHelper.putPolymorphic(NBTHelper.getOrCreate(nbt, PART_DATA_TAG), key, data, MAPPER);
	}

	@Override
	public CompoundNBT serializeNBT() {
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.nbt = nbt;
	}

}
