package spinyq.spinytextiles.utility.textile;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

public class ClothingPattern extends AbstractPattern<ClothingPattern> {

	private final ImmutableSet<Supplier<ClothingPart>> parts;

	private ClothingPattern(ImmutableSet<Supplier<ClothingPart>> parts) {
		this.parts = parts;
	}
	
	@SafeVarargs
	public ClothingPattern(Supplier<ClothingPart>...parts) {
		this(ImmutableSet.copyOf(parts));
	}
	
	public Stream<ClothingPart> getPartStream() {
		return parts.stream().map(Supplier::get);
	}
	
}
