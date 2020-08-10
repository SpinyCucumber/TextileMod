package spinyq.immersivetextiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;

@Mod(TextileMod.MODID)
public class TextileMod
{
	
	public static final String MODID = "spiny_textiles";
    
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    public TextileMod() {
    	ModItems.init();
    	ModBlocks.init();
    }
    
}
