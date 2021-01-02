package spinyq.spinytextiles.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import spinyq.spinytextiles.ModClothingParts;
import spinyq.spinytextiles.ModClothingPatterns;
import spinyq.spinytextiles.ModFabricLayers;
import spinyq.spinytextiles.ModFabricPatterns;
import spinyq.spinytextiles.utility.color.ColorWord;
import spinyq.spinytextiles.utility.textile.clothing.IClothing;
import spinyq.spinytextiles.utility.textile.clothing.NBTClothing;
import spinyq.spinytextiles.utility.textile.fabric.IFabric;
import spinyq.spinytextiles.utility.textile.fabric.NBTFabric;

public class ClothingItem extends Item {

	// private static final IForgeRegistry<ClothingPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(ClothingPattern.class);
	
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
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		// TODO For every clothing pattern, make a new clothing item
		// The following code is just to test things
		ItemStack item = new ItemStack(this);
		IClothing clothing = getClothing(item);
		// Set pattern
		clothing.setPattern(ModClothingPatterns.TOP_HAT.get());
		// Set part data
		// Have to construct fabric first
		IFabric fabric = new NBTFabric();
		fabric.setPattern(ModFabricPatterns.SOLID.get());
		fabric.setLayerColor(ModFabricLayers.BASE.get(), ColorWord.WHITE.color);
		clothing.setPartData(ModClothingParts.TOP_HAT_BASE.get(), fabric);
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		// TODO
		return super.getDisplayName(stack);
	}
	
}
