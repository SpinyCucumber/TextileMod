package spinyq.spinytextiles.items;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

public class FabricItem extends Item implements IDyeableItem, IBleachableItem {

	public FabricItem(Properties properties) {
		super(properties);
		// TODO Set up color handler
	}

	@Override
	public boolean dye(ContainedItemStack<PlayerInventory> object, IDyeProvider provider) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean bleach(ContainedItemStack<PlayerInventory> object, IBleachProvider provider) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
