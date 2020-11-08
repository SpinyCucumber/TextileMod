package spinyq.spinytextiles.utility.textile;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import spinyq.spinytextiles.utility.NBTHelper.INBTPolymorphic;

public interface IGarmentComponent extends INBTPolymorphic<IGarmentComponent> {

	static final Map<String, Supplier<IGarmentComponent>> factoryMap = new ImmutableMap.Builder<String, Supplier<IGarmentComponent>>()
			.put("fiber", FiberInfo::new)
			.put("fabric", FabricInfo::new)
			.build();
		
}
