package spinyq.spinytextiles.utility.textile;

public interface IBleachable<T, C> {

	boolean bleach(T object, C context, IDyeProvider provider);
	
}
