package spinyq.spinytextiles;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import spinyq.spinytextiles.utility.textile.FabricPattern;
import spinyq.spinytextiles.utility.textile.GarmentPattern;

public class ModPatterns {
	
	@SubscribeEvent
	public static void createRegistries(RegistryEvent.NewRegistry event) {
		// TODO Create general pattern registry?
		new RegistryBuilder<FabricPattern>().setType(FabricPattern.class).setName(new ResourceLocation(TextileMod.MODID, "fabric_pattern")).create();
		new RegistryBuilder<GarmentPattern>().setType(GarmentPattern.class).setName(new ResourceLocation(TextileMod.MODID, "garment_pattern")).create();
	}
	
	public static final DeferredRegister<FabricPattern> FABRIC_PATTERNS = new DeferredRegister<>(GameRegistry.findRegistry(FabricPattern.class), TextileMod.MODID);
	public static final DeferredRegister<GarmentPattern> GARMENT_PATTERNS = new DeferredRegister<>(GameRegistry.findRegistry(GarmentPattern.class), TextileMod.MODID);
	
	public static void init() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.register(ModPatterns.class);
		FABRIC_PATTERNS.register(bus);
		GARMENT_PATTERNS.register(bus);
	}
	
}
