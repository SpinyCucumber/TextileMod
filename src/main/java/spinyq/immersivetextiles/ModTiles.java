package spinyq.immersivetextiles;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.immersivetextiles.event.ConstructModEvent;
import spinyq.immersivetextiles.tiles.BasinTile;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModTiles {

	public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, TextileMod.MODID);
	
	public static final RegistryObject<TileEntityType<BasinTile>> BASIN_TILE = TILES.register("basin",
			() -> TileEntityType.Builder.create(BasinTile::new, ModBlocks.BASIN_BLOCK.get()).build(null));
	
	@SubscribeEvent
	public void onConstructMod(ConstructModEvent event) {
		TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
