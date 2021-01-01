package spinyq.spinytextiles.utility.textile.fabric;

import java.util.Map;

import spinyq.spinytextiles.utility.color.RYBKColor;

public class Fabric implements IFabric {

	private FabricPattern pattern;
	private Map<FabricLayer, RYBKColor> colors;
	
	@Override
	public void setPattern(FabricPattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public FabricPattern getPattern() {
		return pattern;
	}

	@Override
	public void setLayerColor(FabricLayer layer, RYBKColor color) {
		colors.put(layer, color);
	}

	@Override
	public RYBKColor getLayerColor(FabricLayer layer) {
		return colors.get(layer);
	}

}
