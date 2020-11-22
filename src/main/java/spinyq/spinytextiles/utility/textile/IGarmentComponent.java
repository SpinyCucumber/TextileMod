package spinyq.spinytextiles.utility.textile;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IGarmentComponent extends INBTSerializable<CompoundNBT> {

	static final Map<String, Supplier<IGarmentComponent>> factoryMap = new ImmutableMap.Builder<String, Supplier<IGarmentComponent>>()
			.put("fiber", FiberInfo::new)
			.put("fabric", FabricInfo::new)
			.build();
		
}
