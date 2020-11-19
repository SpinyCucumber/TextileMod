package spinyq.spinytextiles.utility.textile;

public interface IDyeable<T> {

	boolean dye(T object, IDyeProvider provider);
	
}
