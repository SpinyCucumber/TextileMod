package spinyq.spiny_textiles;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spiny_textiles.blocks.BasinBlock;

/**
 * Handles managing and registering all the blocks of the mod.
 * @author SpinyQ
 *
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, TextileMod.MODID);
	
	public static final RegistryObject<BasinBlock> BASIN_BLOCK = BLOCKS.register("basin",
			() -> new BasinBlock(Block.Properties.create(Material.IRON, MaterialColor.IRON)));
	
	// Registers block items corresponding to each block.
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		BLOCKS.getEntries().forEach((block) -> {
			BlockItem item = new BlockItem(block.get(), new Item.Properties().group(ItemGroupTextiles.instance));
			// Copy resource ID
			item.setRegistryName(block.getId());
			event.getRegistry().register(item);
		});
	}

	public static void init() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
