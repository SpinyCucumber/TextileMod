package spinyq.spinytextiles.utility;

import spinyq.spinytextiles.utility.color.RYBKColor;

public interface IDyeable<T, C> {

	void dye(T object, C context, RYBKColor color);
	RYBKColor getColor(T object);
	
	int getDyeCost();
	
}
