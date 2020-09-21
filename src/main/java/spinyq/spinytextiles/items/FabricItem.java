package spinyq.spinytextiles.items;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.color.RYBKColor;

public class FabricItem extends Item implements IDyeableItem {

	public FabricItem(Properties properties) {
		super(properties);
		// TODO Set up color handler
	}

	@Override
	public void dye(ItemStack object, IInventory context, RYBKColor color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RYBKColor getColor(ItemStack object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDyeCost() {
		// TODO Auto-generated method stub
		return 0;
	}

}
