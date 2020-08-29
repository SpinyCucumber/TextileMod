package spinyq.spinytextiles;

import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spinytextiles.items.FiberItem;
import spinyq.spinytextiles.items.ThreadItem;
import spinyq.spinytextiles.utility.color.RGBColor;

/**
 * Handles managing and registering all the items of the mod.
 * 
 * @author SpinyQ
 *
 */
public class ModItems {

	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, TextileMod.MODID);

	public static final RegistryObject<ThreadItem> THREAD_ITEM = ITEMS.register("thread",
			() -> new ThreadItem(new Item.Properties().group(ItemGroupTextiles.instance)));

	public static final RegistryObject<FiberItem> WHITE_WOOL_FIBER_ITEM = ITEMS.register("white_wool_fiber",
			() -> new FiberItem(new Item.Properties().group(ItemGroupTextiles.instance),
					new RGBColor().fromDye(DyeColor.WHITE), 1)),
			BROWN_WOOL_FIBER_ITEM = ITEMS.register("brown_wool_fiber",
					() -> new FiberItem(new Item.Properties().group(ItemGroupTextiles.instance),
							new RGBColor().fromDye(DyeColor.BROWN), 1)),
			SILK_FIBER_ITEM = ITEMS.register("silk_fiber",
					() -> new FiberItem(new Item.Properties().group(ItemGroupTextiles.instance),
							new RGBColor().fromDye(DyeColor.WHITE), 1));

	public static final RegistryObject<Item> BRUSH_ITEM = ITEMS.register("brush",
			() -> new Item(new Item.Properties().maxDamage(128).group(ItemGroupTextiles.instance))),
			SPINDLE_ITEM = ITEMS.register("spindle",
					() -> new Item(new Item.Properties().group(ItemGroupTextiles.instance)));

	// TODO Might move "Mod..." classes to a single "Registration" class to simplify
	// things. Or not.
	public static void init() {
		TextileMod.LOGGER.info("ModItems init...");
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

}
