package spinyq.spiny_textiles;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spiny_textiles.tiles.BasinTile;

public class ModTiles {

	public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, TextileMod.MODID);
	
	public static final RegistryObject<TileEntityType<BasinTile>> BASIN_TILE = TILES.register("basin",
			() -> TileEntityType.Builder.create(BasinTile::new, ModBlocks.BASIN_BLOCK.get()).build(null));
	
	public static void init() {
		TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
