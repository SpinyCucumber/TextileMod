package spinyq.spinytextiles.client.render;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Manages registering IItemColor handlers for items and such.
 * @author Elijah Hilty
 *
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ItemColorHelper {

	private static Map<Item, IItemColor> mappings = new HashMap<>();
	
	/**
	 * Should only be called client-side.
	 */
	public static void setItemColorHandler(Item item, IItemColor colorHandler) {
		mappings.put(item, colorHandler);
	}
	
	private static void registerColorHandlers(ItemColors manager) {
		mappings.forEach((item, colorHandler) -> {
			manager.register(colorHandler, item);
		});
	}
	
	@SubscribeEvent
	public static void onItemColorHandler(ColorHandlerEvent.Item event) {
		registerColorHandlers(event.getItemColors());
	}
	
}
