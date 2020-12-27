package spinyq.spinytextiles.items;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ClothingItem extends Item {

	public ClothingItem(Properties properties) {
		super(properties);
	}

	@Override
	public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack,
			EquipmentSlotType armorSlot, A _default) {
		// TODO Auto-generated method stub
		// Should look up a model from a cache
		// Could cache models based on garment pattern and minimum number of quad layers?
		return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
	}
	
}
