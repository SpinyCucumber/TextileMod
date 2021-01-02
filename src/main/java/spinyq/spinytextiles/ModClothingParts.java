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
import spinyq.spinytextiles.utility.textile.clothing.ClothingPart;
import spinyq.spinytextiles.utility.textile.clothing.FabricClothingPart;

@EventBusSubscriber(bus = Bus.MOD)
public class ModClothingParts {

	public static final DeferredRegister<ClothingPart> CLOTHING_PARTS = new DeferredRegister<>(
			LazyForgeRegistry.of(ClothingPart.class), TextileMod.MODID);

	public static final RegistryObject<ClothingPart> TOP_HAT_BASE = CLOTHING_PARTS.register("top_hat_base",
			() -> new FabricClothingPart());
	
	@SubscribeEvent
	public static void createRegistries(RegistryEvent.NewRegistry event) {
		// Create registry
		new RegistryBuilder<ClothingPart>().setType(ClothingPart.class)
				.setName(new ResourceLocation(TextileMod.MODID, "clothingpart"))
				.create();
		// Hook up deferred register
		CLOTHING_PARTS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
