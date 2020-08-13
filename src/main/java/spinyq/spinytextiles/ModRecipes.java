package spinyq.spinytextiles;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spinytextiles.crafting.BrushRecipe;

public class ModRecipes {

	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, TextileMod.MODID);
	
	public static final RegistryObject<BrushRecipe.Serializer> CRAFTING_BRUSH = RECIPE_SERIALIZERS.register(
			"crafting_brush",
			BrushRecipe.Serializer::new);
	
	public static void init() {
		TextileMod.LOGGER.info("ModRecipes init...");
		RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
