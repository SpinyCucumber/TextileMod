package spinyq.spinytextiles.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import spinyq.spinytextiles.utility.textile.clothing.IClothing;
import spinyq.spinytextiles.utility.textile.clothing.NBTClothing;

public class ClothingItem extends Item {

	public ClothingItem(Properties properties) {
		super(properties);
	}

	public IClothing getClothing(ItemStack item) {
		return new NBTClothing(item.getOrCreateTag());
	}
	
	@Override
	public boolean canEquip(ItemStack stack, EquipmentSlotType armorType, Entity entity) {
		// Entity must be a player
		if (!(entity instanceof PlayerEntity)) return false;
		// Ensure slot matches pattern's slot
		return (armorType == getClothing(stack).getPattern().getSlot());
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		// TODO
		return super.getDisplayName(stack);
	}
	
}
