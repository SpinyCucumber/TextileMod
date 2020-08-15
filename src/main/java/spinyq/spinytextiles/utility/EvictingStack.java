package spinyq.spinytextiles.utility;

import java.util.Stack;

public class EvictingStack<E> extends Stack<E> {

	private int maximumSize;
	
	/**
	 * Creates an empty evicting stack.
	 */
	public EvictingStack(int maximumSize) {
		super();
		this.maximumSize = maximumSize;
	}
	
	public int getMaximumSize() {
		return maximumSize;
	}
	
	@Override
	public E push(E item) {
		E result = super.push(item);
		// Remove oldest element if max size exceeded
		if (this.size() > maximumSize) this.remove(0);
		return result;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4771778424320770615L;

}
