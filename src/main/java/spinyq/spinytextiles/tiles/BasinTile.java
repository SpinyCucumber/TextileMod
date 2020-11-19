package spinyq.spinytextiles.tiles;

import java.util.Stack;

import net.minecraft.tileentity.TileEntity;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.tiles.BasinTile.State.BaseState;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.IBleachProvider;
import spinyq.spinytextiles.utility.textile.IDyeProvider;

/**
 * Uses a stack-based FSM
 * @author Elijah Hilty
 *
 */
public class BasinTile extends TileEntity {

	public static interface State {
		
		public static class ChildState<T extends State> implements State {
			
			private T parent;

			public ChildState(T parent) {
				this.parent = parent;
			}

			public T getParent() {
				return parent;
			}
			
		}
		
		public static class BaseState implements State {
			
		}
		
		public static class FilledState extends ChildState<BaseState> {

			public FilledState(BaseState parent) {
				super(parent);
			}
			
			public boolean drain(int amount) {
				// TODO
				return false;
			}
			
		}
		
		public static class DyeState extends ChildState<FilledState> implements IDyeProvider {

			public DyeState(FilledState parent) {
				super(parent);
			}

			@Override
			public RYBKColor getColor() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean drain(int amount) {
				return getParent().drain(amount);
			}
			
		}
		
		public static class BleachState extends ChildState<FilledState> implements IBleachProvider {

			public BleachState(FilledState parent) {
				super(parent);
			}

			@Override
			public float getBleachLevel() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean drain(int amount) {
				return getParent().drain(amount);
			}
			
		}
		
	}
	
	private Stack<State> stateStack = new Stack<>();
	
	public void pushState(State state) {
		stateStack.add(state);
	}
	
	public void popState() {
		stateStack.pop();
	}
	
	public State getState() {
		return stateStack.peek();
	}
	
	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
		// Push the base state onto the state stack
		pushState(new BaseState());
	}

}
