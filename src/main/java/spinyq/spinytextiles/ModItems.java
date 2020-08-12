package spinyq.spinytextiles;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spinytextiles.items.ThreadItem;

/**
 * Handles managing and registering all the items of the mod.
 * @author SpinyQ
 *
 */
public class ModItems {
	
	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, TextileMod.MODID);
	
	public static final RegistryObject<ThreadItem> THREAD_ITEM = ITEMS.register("thread",
			() -> new ThreadItem(new Item.Properties().group(ItemGroupTextiles.instance)));
	
	public static final RegistryObject<BlockItem> BASIN_ITEM = ITEMS.register("basin",
			() -> new BlockItem(ModBlocks.BASIN_BLOCK.get(), new Item.Properties().group(ItemGroupTextiles.instance)));

	// TODO Might move "Mod..." classes to a single "Registration" class to simplify things. Or not.
	public static void init() {
		TextileMod.LOGGER.info("ModItems init...");
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
