package spinyq.spinytextiles.utility.textile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;

public class FabricInfo implements IGarmentComponent {

	private static String TAG_PATTERN = "Pattern", TAG_COLORS = "Colors";
	
	private static IForgeRegistry<FabricPattern> REGISTRY = null;
	
	private static IForgeRegistry<FabricPattern> getPatternRegistry() {
		// Cache for more speed
		if (REGISTRY == null) REGISTRY = GameRegistry.findRegistry(FabricPattern.class);
		return REGISTRY;
	}
	
	private FabricPattern pattern;
	private Map<String, RYBKColor> colors;
	
	public FabricInfo() { }

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		// Write registry name of pattern to nbt
		// TODO Abstract this or find if Minecraft already has support
		nbt.putString(TAG_PATTERN, pattern.getRegistryName().toString());
		// Write colors
		NBTHelper.putMap(nbt, TAG_COLORS, colors);
		// Done
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		// Retrieve pattern registry
		// TODO Abstract this or find if Minecraft already has support
		IForgeRegistry<FabricPattern> registry = getPatternRegistry();
		// Retrieve the fabric's pattern by looking up registry name
		pattern = registry.getValue(new ResourceLocation(nbt.getString(TAG_PATTERN)));
		// Retrieve the colors
		colors = NBTHelper.getMap(HashMap::new, RYBKColor::new, nbt, TAG_COLORS);
	}

	@Override
	public Type<?> getType() {
		return ComponentManager.FABRIC;
	}

}
