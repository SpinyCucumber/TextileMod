package spinyq.spinytextiles.utility;

public interface IDyeable<T, C> {

	void dye(T object, C context, Color3f color);
	Color3f getColor(T object);
	
	int getDyeCost();
	
}
