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
	
}
