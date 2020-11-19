package spinyq.spinytextiles.utility.textile;

public interface IDyeable<T, C> {

	boolean dye(T object, C context, IDyeProvider provider);
	boolean bleach(T object, C context, IDyeProvider provider);
	
}
