package spinyq.spinytextiles;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.FabricLayer;

@EventBusSubscriber(bus = Bus.MOD)
public class ModFabricLayers {

	public static final DeferredRegister<FabricLayer> FABRIC_LAYER = new DeferredRegister<>(
			LazyForgeRegistry.of(FabricLayer.class), TextileMod.MODID);

	public static final RegistryObject<FabricLayer> BASE = FABRIC_LAYER.register("base", FabricLayer::new);

	@SubscribeEvent
	public static void createRegistries(RegistryEvent.NewRegistry event) {
		// Create registry
		new RegistryBuilder<FabricLayer>().setType(FabricLayer.class)
				.setName(new ResourceLocation(TextileMod.MODID, "fabriclayer"))
				.create();
		// Hook up deferred register
		FABRIC_LAYER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

}
