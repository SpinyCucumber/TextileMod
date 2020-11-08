package spinyq.spinytextiles.utility.textile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;

public class FabricInfo implements IGarmentComponent {

	private static final String TAG_PATTERN = "Pattern", TAG_COLORS = "Colors";
	
	private static IForgeRegistry<FabricPattern> REGISTRY = null;
	
	private FabricPattern pattern;
	private Map<String, RYBKColor> colors;
	
	public FabricInfo() { }

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

	@Override
	public Supplier<IGarmentComponent> getFactory() {
		return FabricInfo::new;
	}

}
