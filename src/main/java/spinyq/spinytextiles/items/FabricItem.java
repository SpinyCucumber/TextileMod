package spinyq.spinytextiles.items;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

public class FabricItem extends Item implements IDyeableItem, IBleachableItem {

	public FabricItem(Properties properties) {
		super(properties);
		// TODO Set up color handler
	}

	@Override
	public boolean dye(ItemStack object, IInventory context, IDyeProvider provider) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean bleach(ItemStack object, IInventory context, IBleachProvider provider) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
