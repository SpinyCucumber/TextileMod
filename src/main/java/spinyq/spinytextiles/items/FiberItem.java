package spinyq.spinytextiles.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.Color3f;

public class FiberItem extends Item implements IFiberItem {

	private int fiberValue;
	private Color3f color;
	
	public FiberItem(Properties properties, int fiberValue, Color3f color) {
		super(properties);
		this.fiberValue = fiberValue;
		this.color = color;
	}

	@Override
	public int getFiberValue(ItemStack stack) {
		return fiberValue;
	}

	@Override
	public Color3f getColor(ItemStack stack) {
		return color;
	}
	
}
