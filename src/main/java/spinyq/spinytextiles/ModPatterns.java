package spinyq.spinytextiles;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ModPatterns {

	// TODO Create deferred registers
	
	public static void init() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.register(ModPatterns.class);
	}
	
	@SubscribeEvent
	public static void createRegistries(RegistryEvent.NewRegistry event) {
		// TODO Create fabric pattern registry
		// TODO Create garment pattern registry
		// TODO Create general pattern registry
	}
	
}
