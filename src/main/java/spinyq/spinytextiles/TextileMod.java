package spinyq.spinytextiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;

@Mod(TextileMod.MODID)
public class TextileMod
{
	
	public static final String MODID = "spinytextiles";
    
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    public TextileMod() {
		TextileMod.LOGGER.info("TextileMod Constructor...");
		// TODO Abstract this in some way
    	// ModBlocks.init();
    	ModTiles.init();
    	ModItems.init();
    	ModRecipes.init();
    	ModSounds.init();
    	// ModPatterns.init();
    }
    
}
