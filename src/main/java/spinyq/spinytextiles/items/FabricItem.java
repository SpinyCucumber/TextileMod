package spinyq.spinytextiles.items;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.ContainedItemStack;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.textile.Fabric;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

public class FabricItem extends Item implements IDyeableItem, IBleachableItem {

	private static final String FABRIC_TAG = "Fabric";
	
	public Fabric getFabric(ItemStack stack) {
		return NBTHelper.getOrNull(Fabric::new, stack.getOrCreateTag(), FABRIC_TAG);
	}
	
	public void setFabric(ItemStack stack, Fabric info) {
		NBTHelper.put(stack.getOrCreateTag(), FABRIC_TAG, info);
	}
	
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
