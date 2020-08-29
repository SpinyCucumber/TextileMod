package spinyq.spinytextiles.utility;

import spinyq.spinytextiles.utility.color.RYBColor;

public interface IDyeable<T, C> {

	void dye(T object, C context, RYBColor color);
	RYBColor getColor(T object);
	
	int getDyeCost();
	
}
