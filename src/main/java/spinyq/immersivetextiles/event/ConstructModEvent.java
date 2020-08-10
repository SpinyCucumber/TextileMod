package spinyq.immersivetextiles.event;

import net.minecraftforge.eventbus.api.Event;
import spinyq.immersivetextiles.TextileMod;

public class ConstructModEvent extends Event {

	public final TextileMod mod;

	public ConstructModEvent(TextileMod mod) {
		this.mod = mod;
	}
	
}
