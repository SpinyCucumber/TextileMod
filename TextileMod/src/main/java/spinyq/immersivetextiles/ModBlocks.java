package spinyq.immersivetextiles;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.immersivetextiles.blocks.BlockBasin;

/**
 * Handles managing and registering all the blocks of the mod.
 * @author SpinyQ
 *
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, TextileMod.MODID);
	
	public static final RegistryObject<BlockBasin> BLOCK_BASIN = BLOCKS.register("basin", BlockBasin::new);
	
	// Registers block items corresponding to each block.
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		BLOCKS.getEntries().forEach((block) -> {
			BlockItem item = new BlockItem(block.get(), new Item.Properties().group(ItemGroupTextiles.instance));
			event.getRegistry().register(item);
		});
	}

	public static void init() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
