package spinyq.spinytextiles.utility.textile;

public interface IBleachable<T> {

	boolean bleach(T object, IBleachProvider provider);
	
}
