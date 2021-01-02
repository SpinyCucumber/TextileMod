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
import spinyq.spinytextiles.utility.textile.clothing.ClothingPattern;

@EventBusSubscriber(bus = Bus.MOD)
public class ModClothingPatterns {

	public static final DeferredRegister<ClothingPattern> CLOTHING_PATTERNS = new DeferredRegister<>(
			LazyForgeRegistry.of(ClothingPattern.class), TextileMod.MODID);

	public static final RegistryObject<ClothingPattern> TOP_HAT = CLOTHING_PATTERNS.register("top_hat",
			() -> new ClothingPattern(ModClothingParts.TOP_HAT_BASE));
	
	@SubscribeEvent
	public static void createRegistries(RegistryEvent.NewRegistry event) {
		// Create registry
		new RegistryBuilder<ClothingPattern>().setType(ClothingPattern.class)
				.setName(new ResourceLocation(TextileMod.MODID, "clothingpattern"))
				.create();
		// Hook up deferred register
		CLOTHING_PATTERNS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
