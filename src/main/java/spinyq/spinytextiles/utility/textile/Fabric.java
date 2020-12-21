package spinyq.spinytextiles.utility.textile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;

public class Fabric implements IGarmentComponent {

	private static final String TAG_PATTERN = "Pattern", TAG_COLORS = "Colors";
	
	private static IForgeRegistry<FabricPattern> REGISTRY = null;
	
	private FabricPattern pattern;
	private Map<String, RYBKColor> colors;
	
	// Needed for deserialization
	public Fabric() { }

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		// Write registry name of pattern to nbt
		NBTHelper.putRegistryEntry(nbt, TAG_PATTERN, pattern);
		// Write colors
		NBTHelper.putMap(nbt, TAG_COLORS, colors);
		// Done
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		// Cache the registry for more speed
		if (REGISTRY == null) REGISTRY = GameRegistry.findRegistry(FabricPattern.class);
		// Retrieve the fabric's pattern by looking up registry name
		pattern = NBTHelper.getRegistryEntry(nbt, TAG_PATTERN, REGISTRY);
		// Retrieve the colors
		colors = NBTHelper.getMap(HashMap::new, RYBKColor::new, nbt, TAG_COLORS);
	}

	public FabricPattern getPattern() {
		return pattern;
	}

	@Override
	public int hashCode() {
		return Objects.hash(colors, pattern);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fabric other = (Fabric) obj;
		return Objects.equals(colors, other.colors) && Objects.equals(pattern, other.pattern);
	}

}