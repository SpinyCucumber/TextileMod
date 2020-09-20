package spinyq.spinytextiles;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spinytextiles.crafting.BrushRecipe;

@EventBusSubscriber(bus = Bus.MOD)
public class ModRecipes {

	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, TextileMod.MODID);
	
	public static final RegistryObject<BrushRecipe.Serializer> CRAFTING_BRUSH = RECIPE_SERIALIZERS.register(
			"crafting_brush",
			BrushRecipe.Serializer::new);
	
	@SubscribeEvent
	public static void createRegistry(RegistryEvent.NewRegistry event) {
		// Hook up deferred register
		RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
