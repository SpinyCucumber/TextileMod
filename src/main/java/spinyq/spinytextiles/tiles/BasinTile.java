package spinyq.spinytextiles.tiles;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import spinyq.spinytextiles.ModTiles;
import spinyq.spinytextiles.TextileMod;
import spinyq.spinytextiles.utility.IDyeable;
import spinyq.spinytextiles.utility.color.HSVColor;
import spinyq.spinytextiles.utility.color.RYBKColor;

// TODO Should probably use an FSM
public class BasinTile extends TileEntity {

	public static final int MAX_WATER_LEVEL = 9;
	public static final float DYE_MULTIPLIER = 0.25f, BLEACH_MULTIPLIER = 0.25f;
	private static final String TAG_WATER_LEVEL = "WaterLevel", TAG_COLOR = "Color", TAG_BLEACH_LEVEL = "BleachLevel";

	public BasinTile() {
		super(ModTiles.BASIN_TILE.get());
		waterLevel = 0;
		bleachLevel = 0;
		color = new RYBKColor();
	}

	private int waterLevel;
	private RYBKColor color;
	private float bleachLevel;

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		waterLevel = compound.getInt(TAG_WATER_LEVEL);
		color = new RYBKColor().fromInt(compound.getInt(TAG_COLOR));
		bleachLevel = compound.getFloat(TAG_BLEACH_LEVEL);
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		CompoundNBT result = super.write(tag);
		result.putInt(TAG_WATER_LEVEL, waterLevel);
		result.putInt(TAG_COLOR, color.toInt());
		result.putFloat(TAG_BLEACH_LEVEL, bleachLevel);
		return result;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbtTag = new CompoundNBT();
		// Write your data into the nbtTag
		write(nbtTag);
		return new SUpdateTileEntityPacket(getPos(), -1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		CompoundNBT tag = pkt.getNbtCompound();
		// Handle your Data
		read(tag);
	}

	/*
	 * Creates a tag containing all of the TileEntity information, used by vanilla
	 * to transmit from server to client
	 */
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbtTagCompound = new CompoundNBT();
		write(nbtTagCompound);
		return nbtTagCompound;
	}

	/*
	 * Populates this TileEntity with information from the tag, used by vanilla to
	 * transmit from server to client
	 */
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		read(tag);
	}

	/**
	 * Syncs data with clients and marks this tile to be saved.
	 */
	private void update() {
		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);
		markDirty();
	}

	public int getWaterLevel() {
		return waterLevel;
	}

	/**
	 * Mixes a new dye into the basin. This increases the dye concentration and
	 * changes the color.
	 * 
	 * @param dyeColor
	 */
	public void addDye(RYBKColor dyeColor) {
		// Add the new color
		// Clamp it as well
		color.add(dyeColor.scaledBy(DYE_MULTIPLIER));
		color.clamp();
		// DEBUG
		TextileMod.LOGGER.info("BasinTile mixDye... dyeColor: {} new color: {}", dyeColor, color);
		update();
	}
	
	public void addBleach(float amount) {
		bleachLevel = Math.min(1.0f, bleachLevel + BLEACH_MULTIPLIER * amount);
		update();
	}
	
	/**
	 * Dyes an item, also consuming some water.
	 * @param stack
	 * @param dyeable
	 */
	public <T,C> void dye(T object, C context, IDyeable<T,C> dyeable) {
		// Calculate the new color by mixing the current color of the object and the basin's color
		// Interpolate between them using the dye concentration
		RYBKColor newColor = dyeable.getColor(object).plus(color);
		newColor.clamp();
		dyeable.dye(object, context, newColor);
		int cost = dyeable.getDyeCost();
		if (waterLevel >= cost) drain(cost);
		else throw new RuntimeException("Attempted to dye an item without enough dye.");
	}

	public <T,C> void bleach(T object, C context, IDyeable<T,C> dyeable) {
		RYBKColor newColor = dyeable.getColor(object).plus(new RYBKColor(-bleachLevel, -bleachLevel, -bleachLevel, -bleachLevel));
		newColor.clamp();
		dyeable.dye(object, context, newColor);
		// Apply cost
		int cost = dyeable.getDyeCost();
		drain(cost);
	}

	/**
	 * Sets the water level to the max.
	 */
	public void fill() {
		waterLevel = MAX_WATER_LEVEL - 1;
		update();
	}
	
	/**
	 * Drains the specified amount of water from the basin.
	 * If the basin is emptied, its color and dye level is reset.
	 * @param amt
	 */
	public void drain(int amt) {
		if (waterLevel >= amt) {
			waterLevel -= amt;
			if (waterLevel == 0) {
				// Reset bleach level, color
				bleachLevel = 0.0f;
				color = new RYBKColor();
			}
			update();
		}
		else throw new RuntimeException("Attempted to drain non-existent water.");
	}
	
	/**
	 * Boosts the saturation of the Basin's color.
	 * The current color is converted to HSV space,
	 * where 'amt' is added to the saturation, then converted back.
	 * The saturation won't exceed 1.0.
	 * @param amt
	 */
	public void boostColorSaturation(float amt) {
		HSVColor hsv = new HSVColor();
		color.toHSV(hsv);
		hsv.sat = Math.min(hsv.sat + amt, 1.0f);
		// Convert back to rgb
		color.fromHSV(hsv);
		update();
	}
	
	/**
	 * Whether the color saturation can be boosted further.
	 * Any saturation below 1.0 can be boosted.
	 */
	public boolean canBoostSaturation(float amt) {
		HSVColor hsv = new HSVColor();
		color.toHSV(hsv);
		return hsv.sat < 1.0f;
	}
	
	public boolean canDye(IDyeable<?,?> dyeable) {
		return (waterLevel >= dyeable.getDyeCost());
	}
	
	public <T, C> boolean canBleach(T object, C context, IDyeable<T,C> dyeable) {
		if (waterLevel < dyeable.getDyeCost()) return false;
		// If any of the colors components is above zero, we can still apply bleach
		// Retrieve color
		RYBKColor dye = dyeable.getColor(object);
		for (RYBKColor.Axis axis : RYBKColor.Axis.values()) {
			if (dye.project(axis.direction) > 0.0f) return true;
		}
		return false;
	}
	
	/**
	 * Whether the basin is heated. Heat is required to dye objects.
	 * @return
	 */
	public boolean isHeated() {
		return CampfireBlock.isLitCampfireInRange(world, pos, 2);
	}

	/**
	 * Whether the basin contains the maximum amount of water.
	 * 
	 * @return
	 */
	public boolean isFull() {
		return waterLevel == (MAX_WATER_LEVEL - 1);
	}

	/**
	 * Whether the basin contains any water.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return waterLevel == 0;
	}
	
	public boolean hasDye() {
		return color.hasValue();
	}
	
	public boolean hasBleach() {
		return bleachLevel > 0;
	}

	public boolean canAcceptDye(RYBKColor dye) {
		// If the dye contains a component that we're already maxed out in, reject it.
		// Iterate over components
		for (RYBKColor.Axis axis : RYBKColor.Axis.values()) {
			if (dye.project(axis.direction) > 0.0f && color.project(axis.direction) >= 1.0f) return false;
		}
		return true;
	}
	
	public boolean canAcceptBleach(float amount) {
		// Reject bleach if we already have the max level
		return (bleachLevel < 1.0f);
	}

	public RYBKColor getColor() {
		return color;
	}

	public float getBleachLevel() {
		return bleachLevel;
	}

	public double getWaterHeight() {
		return 0.2 + ((double) waterLevel / (double) MAX_WATER_LEVEL) * 0.875;
	}
	
}
