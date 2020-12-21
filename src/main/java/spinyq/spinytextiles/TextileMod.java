package spinyq.spinytextiles;

import net.minecraftforge.fml.common.Mod;
import spinyq.spinytextiles.client.model.FabricTextureManager;

@Mod(TextileMod.MODID)
public class TextileMod
{
	
	public static final String MODID = "spinytextiles";
    
    public TextileMod() {
    	// Send message to other classes when the mod has been constructed
    	FabricTextureManager.INSTANCE.onModConstructed();
    }
    
}
