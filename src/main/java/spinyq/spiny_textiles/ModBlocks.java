package spinyq.spiny_textiles;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spiny_textiles.blocks.BasinBlock;

/**
 * Handles managing and registering all the blocks of the mod.
 * @author SpinyQ
 *
 */
public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, TextileMod.MODID);
	
	public static final RegistryObject<BasinBlock> BASIN_BLOCK = BLOCKS.register("basin",
			() -> new BasinBlock(Block.Properties.create(Material.IRON, MaterialColor.IRON).hardnessAndResistance(2.0f).notSolid()));

	public static void init() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
