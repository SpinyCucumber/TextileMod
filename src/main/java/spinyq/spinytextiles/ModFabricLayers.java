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
import spinyq.spinytextiles.utility.textile.fabric.FabricLayer;

@EventBusSubscriber(bus = Bus.MOD)
public class ModFabricLayers {

	public static final DeferredRegister<FabricLayer> FABRIC_LAYERS = new DeferredRegister<>(
			LazyForgeRegistry.of(FabricLayer.class), TextileMod.MODID);

	public static final RegistryObject<FabricLayer> BASE = FABRIC_LAYERS.register("base", FabricLayer::new),
			HORIZONTAL_STRIPES = FABRIC_LAYERS.register("horizontal_stripes", FabricLayer::new),
			VERTICAL_STRIPES = FABRIC_LAYERS.register("vertical_stripes", FabricLayer::new),
			HORIZONTAL_STRIPES_TRANSLUCENT = FABRIC_LAYERS.register("horizontal_stripes_translucent", FabricLayer::new),
			VERTICAL_STRIPES_TRANSLUCENT = FABRIC_LAYERS.register("vertical_stripes_translucent", FabricLayer::new),
			DIAGONAL_STRIPES = FABRIC_LAYERS.register("diagonal_stripes", FabricLayer::new),
			DOTS = FABRIC_LAYERS.register("dots", FabricLayer::new),
			CHECKERBOARD = FABRIC_LAYERS.register("checkerboard", FabricLayer::new);

	@SubscribeEvent
	public static void createRegistries(RegistryEvent.NewRegistry event) {
		// Create registry
		new RegistryBuilder<FabricLayer>().setType(FabricLayer.class)
				.setName(new ResourceLocation(TextileMod.MODID, "fabriclayer"))
				.create();
		// Hook up deferred register
		FABRIC_LAYERS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

}
