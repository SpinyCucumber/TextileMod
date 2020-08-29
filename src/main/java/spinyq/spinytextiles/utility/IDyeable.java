package spinyq.spinytextiles.utility;

import spinyq.spinytextiles.utility.color.RGBColor;

public interface IDyeable<T, C> {

	void dye(T object, C context, RGBColor color);
	RGBColor getColor(T object);
	
	int getDyeCost();
	
}
