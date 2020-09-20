package spinyq.spinytextiles;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(bus = Bus.MOD)
public class ModSounds {

	public static final DeferredRegister<SoundEvent> SOUNDS = new DeferredRegister<>(ForgeRegistries.SOUND_EVENTS, TextileMod.MODID);
	
	public static final RegistryObject<SoundEvent> BLOCK_SPINNING_WHEEL_SPIN = create("block.spinning_wheel.spin");
	
	private static RegistryObject<SoundEvent> create(String key) {
		return SOUNDS.register(key, () -> new SoundEvent(new ResourceLocation(TextileMod.MODID, key)));
	}
	
	@SubscribeEvent
	public static void createRegistry(RegistryEvent.NewRegistry event) {
		// Hook up deferred register
		SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
			
}
