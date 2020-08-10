package spinyq.immersivetextiles;

import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.immersivetextiles.event.ConstructModEvent;
import spinyq.immersivetextiles.items.ThreadItem;

/**
 * Handles managing and registering all the items of the mod.
 * @author SpinyQ
 *
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModItems {
	
	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, TextileMod.MODID);
	
	public static final RegistryObject<ThreadItem> THREAD_ITEM = ITEMS.register("thread",
			() -> new ThreadItem(new Item.Properties().group(ItemGroupTextiles.instance)));
	
	// TODO Model registry

	// TODO Might move "Mod..." classes to a single "Registration" class to simplify things. Or not.
	@SubscribeEvent
	public static void onConstructMod(ConstructModEvent event) {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
