package spinyq.spinytextiles.utility;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;
import java.util.function.Consumer;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;
import spinyq.spinytextiles.utility.NBTHelper.ClassMapper;
import spinyq.spinytextiles.utility.StackFSM.State;

public class StackFSM<T extends State<T>> implements INBTSerializable<ListNBT> {

	public static abstract class State<T extends State<T>> implements INBTSerializable<CompoundNBT> {

		protected StackFSM<T> fsm;
		protected T superState, subState;
		
		public StackFSM<T> getFsm() {
			return fsm;
		}

		public T getSuperState() {
			return superState;
		}

		public T getSubState() {
			return subState;
		}

		@Override
		public CompoundNBT serializeNBT() {	return new CompoundNBT(); }
		
		@Override
		public void deserializeNBT(CompoundNBT nbt) { }

	}

	private Stack<T> stack = new Stack<>();
	private Collection<Consumer<T>> pushObservers = new LinkedList<>();
	private Collection<Consumer<T>> popObservers = new LinkedList<>();
	private ClassMapper mapper;

	public StackFSM(ClassMapper mapper) {
		this.mapper = mapper;
	}
	
	public StackFSM<T> addPushObserver(Consumer<T> observer) {
		pushObservers.add(observer);
		return this;
	}
	
	public StackFSM<T> addPopObserver(Consumer<T> observer) {
		popObservers.add(observer);
		return this;
	}

	public void pushState(T state) {
		// Give the state a reference to the basin so they can change state and such
		state.fsm = this;
		// If there is already a state in the stack, hook up substate/superstate
		// references
		if (!stack.empty()) {
			T superState = stack.peek();
			superState.subState = state;
			state.superState = superState;
		}
		stack.add(state);
		// Notify observers
		pushObservers.forEach((observer) -> observer.accept(state));
	}

	public void popState(T state) {
		T popped = null;
		while (!popped.equals(state))
			popped = stack.pop();
		// Notify observers
		popObservers.forEach((observer) -> observer.accept(state));
	}

	public void swapState(T oldState, T newState) {
		popState(oldState);
		pushState(newState);
	}

	public T getState() {
		return stack.peek();
	}

	@Override
	public ListNBT serializeNBT() {
		// Create a new ListNBT
		ListNBT listNBT = new ListNBT();
		// Write each state to the list
		for (T state : stack) {
			CompoundNBT objectNBT = NBTHelper.writePolymorphic(state, mapper);
			listNBT.add(objectNBT);
		}
		return listNBT;
	}

	@Override
	public void deserializeNBT(ListNBT nbt) {
		// Convert each list element to a new state, and add them to our stack
		// Clear stack first
		stack.clear();
		for (INBT elementNBT : nbt) {
			CompoundNBT objectNBT = (CompoundNBT) elementNBT;
			T state = NBTHelper.readPolymorphic(objectNBT, mapper);
			pushState(state);
		}
	}

}
