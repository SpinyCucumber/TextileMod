package spinyq.immersivetextiles;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ItemGroupTextiles extends ItemGroup {

	public static final ItemGroupTextiles instance = new ItemGroupTextiles(TextileMod.MODID);
	
	public ItemGroupTextiles(String label) {
		super(label);
	}

	@Override
	public ItemStack createIcon() {
		// TODO Auto-generated method stub
		return null;
	}

}
