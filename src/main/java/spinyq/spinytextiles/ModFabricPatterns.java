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
import spinyq.spinytextiles.utility.textile.FabricPattern;

@EventBusSubscriber(bus = Bus.MOD)
public class ModFabricPatterns {

	public static final DeferredRegister<FabricPattern> FABRIC_PATTERNS = new DeferredRegister<>(
			LazyForgeRegistry.of(FabricPattern.class), TextileMod.MODID);

	public static final RegistryObject<FabricPattern> SOLID = FABRIC_PATTERNS.register("solid",
			() -> new FabricPattern(ModFabricLayers.BASE)),
			HORIZONTAL_STRIPES = FABRIC_PATTERNS.register("horizontal_stripes",
					() -> new FabricPattern(ModFabricLayers.BASE, ModFabricLayers.HORIZONTAL_STRIPES)),
			VERTICAL_STRIPES = FABRIC_PATTERNS.register("vertical_stripes",
					() -> new FabricPattern(ModFabricLayers.BASE, ModFabricLayers.VERTICAL_STRIPES)),
			DIAGONAL_STRIPES = FABRIC_PATTERNS.register("diagonal_stripes",
					() -> new FabricPattern(ModFabricLayers.BASE, ModFabricLayers.DIAGONAL_STRIPES)),
			DOTS = FABRIC_PATTERNS.register("dots",
					() -> new FabricPattern(ModFabricLayers.BASE, ModFabricLayers.DOTS));

	@SubscribeEvent
	public static void createRegistries(RegistryEvent.NewRegistry event) {
		// Create registry
		new RegistryBuilder<FabricPattern>().setType(FabricPattern.class)
				.setName(new ResourceLocation(TextileMod.MODID, "fabricpattern")).create();
		// Hook up deferred register
		FABRIC_PATTERNS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

}
