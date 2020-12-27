package spinyq.spinytextiles.items;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ClothingItem extends Item {

	public ClothingItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canEquip(ItemStack stack, EquipmentSlotType armorType, Entity entity) {
		// TODO
		return super.canEquip(stack, armorType, entity);
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		// TODO
		return super.getDisplayName(stack);
	}
	
}
