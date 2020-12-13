package spinyq.spinytextiles.utility.textile;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.model.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.client.model.ModelHelper.Layer;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RGBAColor;
import spinyq.spinytextiles.utility.color.RYBKColor;

public class FabricInfo implements IGarmentComponent {

	private static final String TAG_PATTERN = "Pattern", TAG_COLORS = "Colors";
	
	private static IForgeRegistry<FabricPattern> REGISTRY = null;
	
	private FabricPattern pattern;
	private Map<String, RYBKColor> colors;
	
	// Needed for deserialization
	public FabricInfo() { }

	public List<Layer> getLayers() {
		// Create new list using streams
		return pattern.layers.stream().map((layer) -> {
			// Convert RYBKColor to RGBA then construct layer
			RGBAColor color = colors.get(layer).toRGBA(new RGBAColor(), null);
			return new Layer(pattern.textures.get(layer), color);
		}).collect(Collectors.toList());
	}
	
	public Collection<Material> getTextures() {
		return pattern.textures.values();
	}

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

}
