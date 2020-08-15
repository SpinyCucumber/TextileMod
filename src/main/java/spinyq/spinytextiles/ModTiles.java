package spinyq.spinytextiles;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import spinyq.spinytextiles.client.render.BasinRenderer;
import spinyq.spinytextiles.client.render.SpinningWheelRenderer;
import spinyq.spinytextiles.tiles.BasinTile;
import spinyq.spinytextiles.tiles.SpinningWheelTile;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModTiles {

	public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, TextileMod.MODID);
	
	public static final RegistryObject<TileEntityType<BasinTile>> BASIN_TILE = TILES.register("basin",
			() -> TileEntityType.Builder.create(BasinTile::new, ModBlocks.BASIN_BLOCK.get()).build(null));
	
	public static final RegistryObject<TileEntityType<SpinningWheelTile>> SPINNING_WHEEL_TILE = TILES.register("spinning_wheel",
			() -> TileEntityType.Builder.create(SpinningWheelTile::new, ModBlocks.SPINNING_WHEEL_BLOCK.get()).build(null));
	
	/**
	 * Handles registering Tile Entity Renderers and such.
	 * TODO Add manager
	 * @param event
	 */
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onClientSetup(FMLClientSetupEvent event) {
		ClientRegistry.bindTileEntityRenderer(BASIN_TILE.get(), BasinRenderer::new);
		ClientRegistry.bindTileEntityRenderer(SPINNING_WHEEL_TILE.get(), SpinningWheelRenderer::new);
	}
	
	public static void init() {
		TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
}
