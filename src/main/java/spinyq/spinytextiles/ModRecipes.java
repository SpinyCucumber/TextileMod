package spinyq.spinytextiles;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spinytextiles.crafting.BrushRecipes;

public class ModRecipes {

	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, TextileMod.MODID);
	
	public static RegistryObject<SpecialRecipeSerializer<BrushRecipes>> CRAFTING_SPECIAL_BRUSH = RECIPE_SERIALIZERS.register(
			"crafting_special_brush",
			() -> new SpecialRecipeSerializer<>(BrushRecipes::new));
	
	public static void init() {
		TextileMod.LOGGER.info("ModRecipes init...");
		RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
