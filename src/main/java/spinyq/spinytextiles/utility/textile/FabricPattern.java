package spinyq.spinytextiles.utility.textile;

import com.google.common.collect.ImmutableList;

public class FabricPattern extends AbstractPattern<FabricPattern> {

	private ImmutableList<String> colors;

	public FabricPattern(ImmutableList<String> colors) {
		this.colors = colors;
	}

	public ImmutableList<String> getColors() {
		return colors;
	} 
	
}
