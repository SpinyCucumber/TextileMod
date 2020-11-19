package spinyq.spinytextiles.utility.textile;

import spinyq.spinytextiles.utility.color.RYBKColor;

public interface IDyeProvider {

	RYBKColor getColor();
	float getBleachLevel();
	boolean drain(int amount);
	
}
