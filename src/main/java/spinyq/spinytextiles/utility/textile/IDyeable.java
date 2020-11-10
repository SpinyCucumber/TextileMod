package spinyq.spinytextiles.utility.textile;

import spinyq.spinytextiles.tiles.BasinTile;

public interface IDyeable<T, C> {

	void dye(T object, C context, BasinTile basin);
	void bleach(T object, C context, BasinTile basin);
	
	boolean canDye(T object, C context, BasinTile basin);
	boolean canBleach(T object, C context, BasinTile basin);
	
}
