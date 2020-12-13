package spinyq.spinytextiles.client.model;

import net.minecraft.client.renderer.model.Material;
import spinyq.spinytextiles.utility.color.RGBAColor;

public class ModelHelper {

	public static class Layer {
		
		public final Material texture;
		public final RGBAColor color;
		
		public Layer(Material texture, RGBAColor color) {
			this.texture = texture;
			this.color = color;
		}
		
	}
	
}
