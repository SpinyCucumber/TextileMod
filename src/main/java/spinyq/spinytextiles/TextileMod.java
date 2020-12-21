package spinyq.spinytextiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import spinyq.spinytextiles.client.model.FabricTextureManager;

@Mod(TextileMod.MODID)
public class TextileMod
{
	
	public static final String MODID = "spinytextiles";
    
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    public TextileMod() {
    	// Send message to other classes when the mod has been constructed
    	FabricTextureManager.onModConstructed();
    }
    
}
