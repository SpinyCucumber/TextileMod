package spinyq.spinytextiles.client.render;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import spinyq.spinytextiles.TextileMod;

/**
 * A central way to specify blocks' render types.
 * @author Elijah Hilty
 *
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class RenderTypeHelper {

	public static enum BlockRenderMode {
		
		SOLID {
			@OnlyIn(Dist.CLIENT)
			@Override
			public RenderType getRenderType() {
				return RenderType.getSolid();
			}
		},
		CUTOUT {
			@OnlyIn(Dist.CLIENT)
			@Override
			public RenderType getRenderType() {
				return RenderType.getCutout();
			}
		},
		CUTOUT_MIPPED {
			@OnlyIn(Dist.CLIENT)
			@Override
			public RenderType getRenderType() {
				return RenderType.getCutoutMipped();
			}
		},
		TRANSLUCENT {
			@OnlyIn(Dist.CLIENT)
			@Override
			public RenderType getRenderType() {
				return RenderType.getTranslucent();
			}
		};
		
		@OnlyIn(Dist.CLIENT)
		public abstract RenderType getRenderType();
		
	}
	
	@OnlyIn(Dist.CLIENT)
	private static Map<Block, BlockRenderMode> mappings = new HashMap<>();
	
	/**
	 * Associates a block with a rendermode.
	 * Doesn't do anything server-side.
	 */
	public static void setRenderMode(Block block, BlockRenderMode mode) {
		TextileMod.LOGGER.trace("RenderTypeHelper setRenderMode... block: {} mode: {}", block, mode);
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			mappings.put(block, mode);
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	private static void registerRenderTypes() {
		TextileMod.LOGGER.trace("RenderTypeHelper registerRenderTypes... mappings: {}", mappings);
		mappings.forEach((block, renderMode) -> {
			RenderTypeLookup.setRenderLayer(block, renderMode.getRenderType());
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {
		// Let Minecraft know of each blocks render type
		registerRenderTypes();
	}
	
}
