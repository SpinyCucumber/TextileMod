package spinyq.immersivetextiles;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.immersivetextiles.items.ItemThread;

/**
 * Handles managing and registering all the items of the mod.
 * @author SpinyQ
 *
 */
public class ModItems {
	
	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, TextileMod.MODID);
	
	public static final RegistryObject<ItemThread> ITEM_THREAD = ITEMS.register("thread", ItemThread::new);
	
	// TODO Model registry

	// TODO Might move "Mod..." classes to a single "Registration" class to simplify things.
	public static void init() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
