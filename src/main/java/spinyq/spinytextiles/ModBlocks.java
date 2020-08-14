package spinyq.spinytextiles;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
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
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.blocks.BasinBlock;
import spinyq.spinytextiles.blocks.SpinningWheelBlock;

/**
 * Handles managing and registering all the blocks of the mod.
 * Subscribe to mod event bus to register block item.
 * @author SpinyQ
 *
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModBlocks {
 
	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, TextileMod.MODID);
	
	public static final RegistryObject<BasinBlock> BASIN_BLOCK = BLOCKS.register("basin",
			() -> new BasinBlock(Block.Properties.create(Material.IRON, MaterialColor.IRON).hardnessAndResistance(2.0f).notSolid()));
	
	public static final RegistryObject<SpinningWheelBlock> SPINNING_WHEEL_BLOCK = BLOCKS.register("spinning_wheel",
			() -> new SpinningWheelBlock(Block.Properties.create(Material.WOOD).hardnessAndResistance(2.5F).notSolid().sound(SoundType.WOOD)));

	/**
	 * Handles registering BlockItems for each block declared in this class.
	 * @param registry
	 */
	private static void registerBlockItems(IForgeRegistry<Item> registry) {
		BLOCKS.getEntries().forEach((block) -> {
			registry.register(createBlockItem(block.get()));
		});
	}
	
	private static BlockItem createBlockItem(Block block) {
		// Construct the item
		BlockItem item = new BlockItem(block, new Item.Properties().group(ItemGroupTextiles.instance));
		// Set the registry name to be the same as the block
		item.setRegistryName(block.getRegistryName());
		// Let Minecraft know this block corresponds to the new item
		Item.BLOCK_TO_ITEM.put(block, item);
		// Done
		return item;
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		registerBlockItems(event.getRegistry());
	}
	
	// TODO It would be cool if forge made loot tables registry objects.
	
	public static void init() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
