package spinyq.spinytextiles;

import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spinytextiles.items.FiberItem;
import spinyq.spinytextiles.items.ThreadItem;
import spinyq.spinytextiles.utility.color.RYBKColor;

/**
 * Handles managing and registering all the items of the mod.
 * 
 * @author SpinyQ
 *
 */
@EventBusSubscriber(bus = Bus.MOD)
public class ModItems {

	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, TextileMod.MODID);

	public static final RegistryObject<ThreadItem> THREAD_ITEM = ITEMS.register("thread",
			() -> new ThreadItem(new Item.Properties().group(ItemGroupTextiles.instance)));

	public static final RegistryObject<FiberItem> WHITE_WOOL_FIBER_ITEM = ITEMS.register("white_wool_fiber",
			() -> new FiberItem(new Item.Properties().group(ItemGroupTextiles.instance),
					new RYBKColor().fromDye(DyeColor.WHITE), 1)),
			BROWN_WOOL_FIBER_ITEM = ITEMS.register("brown_wool_fiber",
					() -> new FiberItem(new Item.Properties().group(ItemGroupTextiles.instance),
							new RYBKColor().fromDye(DyeColor.BROWN), 1)),
			SILK_FIBER_ITEM = ITEMS.register("silk_fiber",
					() -> new FiberItem(new Item.Properties(),
							new RYBKColor().fromDye(DyeColor.WHITE), 1));

	public static final RegistryObject<Item> BRUSH_ITEM = ITEMS.register("brush",
			() -> new Item(new Item.Properties().maxDamage(128).group(ItemGroupTextiles.instance))),
			SPINDLE_ITEM = ITEMS.register("spindle",
					() -> new Item(new Item.Properties().group(ItemGroupTextiles.instance))),
			LYE_ITEM = ITEMS.register("lye",
					() -> new Item(new Item.Properties().group(ItemGroupTextiles.instance)));
	
	@SubscribeEvent
	public static void createRegistry(RegistryEvent.NewRegistry event) {
		// Hook up deferred register
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
