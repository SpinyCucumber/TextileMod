package spinyq.spinytextiles.utility.textile.clothing;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.minecraft.inventory.EquipmentSlotType;
import spinyq.spinytextiles.utility.textile.AbstractPattern;

public class ClothingPattern extends AbstractPattern<ClothingPattern> {

	private final EquipmentSlotType slot;
	private final ImmutableSet<Supplier<ClothingPart>> parts;
	
	private ClothingPattern(EquipmentSlotType slot, ImmutableSet<Supplier<ClothingPart>> parts) {
		this.slot = slot;
		this.parts = parts;
	}
	
	@SafeVarargs
	public ClothingPattern(EquipmentSlotType slot, Supplier<ClothingPart>...parts) {
		this(slot, ImmutableSet.copyOf(parts));
	}
	
	public EquipmentSlotType getSlot() {
		return slot;
	}

	public Stream<ClothingPart> getPartStream() {
		return parts.stream().map(Supplier::get);
	}
	
}
