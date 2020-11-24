package spinyq.spinytextiles.tiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.utility.BlockInteraction;
import spinyq.spinytextiles.utility.NBTHelper.ClassMapper;
import spinyq.spinytextiles.utility.StackFSM;

public class SpinningWheelTile extends TileEntity {

	/**
	 * A state that a spinning wheel may occupy.
	 * Handles player interactions.
	 */
	public abstract class SpinningWheelState extends StackFSM.State<SpinningWheelState> {
		
		public abstract ActionResultType onInteract(BlockInteraction interaction);
		
	}
	
	/**
	 * Used when the spinning wheel has no thread being spun.
	 *
	 */
	public class EmptyState extends SpinningWheelState {

		@Override
		public ActionResultType onInteract(BlockInteraction interaction) {
			
		}
		
	}
	
	private StackFSM<SpinningWheelState> fsm;
	
	public SpinningWheelTile() {
		super(ModTiles.SPINNING_WHEEL_TILE.get());
		ClassMapper mapper = new ClassMapper();
		fsm = new StackFSM<>(mapper);
	}
	
	public ActionResultType onInteract(BlockInteraction interaction) {
		return fsm.getState().onInteract(interaction);
	}

}
