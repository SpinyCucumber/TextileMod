package spinyq.spinytextiles;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spinytextiles.blocks.BasinBlock;
import spinyq.spinytextiles.blocks.SpinningWheelBlock;

/**
 * Handles managing and registering all the blocks of the mod.
 * @author SpinyQ
 *
 */
public class ModBlocks {
 
	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, TextileMod.MODID);
	
	public static final RegistryObject<BasinBlock> BASIN_BLOCK = BLOCKS.register("basin",
			() -> new BasinBlock(Block.Properties.create(Material.IRON, MaterialColor.IRON).hardnessAndResistance(2.0f).notSolid()));
	
	public static final RegistryObject<SpinningWheelBlock> SPINNING_WHEEL_BLOCK = BLOCKS.register("spinning_wheel",
			() -> new SpinningWheelBlock(Block.Properties.create(Material.WOOD).notSolid().sound(SoundType.WOOD)));

	public static void init() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
