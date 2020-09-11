package spinyq.spinytextiles.utility.textile;

import com.google.common.collect.ImmutableList;

public class FabricPattern extends AbstractPattern<FabricPattern> {

	private ImmutableList<String> paramaters;

	public FabricPattern(ImmutableList<String> paramaters) {
		this.paramaters = paramaters;
	}

	public ImmutableList<String> getParamaters() {
		return paramaters;
	} 
	
}
