package spinyq.spinytextiles.utility.textile.fabric;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;

public class NBTFabric implements IFabric, INBTSerializable<CompoundNBT> {

	private static final String PATTERN_TAG = "Pattern", COLORS_TAG = "Colors";
	private static final IForgeRegistry<FabricPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(FabricPattern.class);

	// Internal NBT compound
	CompoundNBT nbt;

	public NBTFabric(CompoundNBT nbt) {
		this.nbt = nbt;
	}

	// Needed for deserialization
	public NBTFabric() {
	}

	public void setLayerColor(FabricLayer layer, RYBKColor color) {
		// Construct the key using the layer resource location
		String key = layer.getRegistryName().toString();
		NBTHelper.put(NBTHelper.getOrCreate(nbt, COLORS_TAG), key, color);
	}

	public RYBKColor getLayerColor(FabricLayer layer) {
		// Construct the key using the layer resource location
		String key = layer.getRegistryName().toString();
		return NBTHelper.get(NBTHelper.getOrCreate(nbt, COLORS_TAG), key, RYBKColor::new);
	}

	@Override
	public void setPattern(FabricPattern pattern) {
		// Write pattern
		NBTHelper.putRegistryEntry(nbt, PATTERN_TAG, pattern);
	}

	@Override
	public FabricPattern getPattern() {
		// Look up pattern using registry
		return NBTHelper.getRegistryEntry(nbt, PATTERN_TAG, PATTERN_REGISTRY);
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
