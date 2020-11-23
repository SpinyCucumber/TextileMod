package spinyq.spinytextiles.utility;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockInteraction {

	public final BlockState blockState;
	public final BlockPos pos;
	public final World world;
	
	public final PlayerEntity player;
	public final Hand hand;
	public final BlockRayTraceResult hit;
	
	public final ItemStack itemstack;
	public final Item item;
	
	public BlockInteraction(BlockState blockState, World world, PlayerEntity player, Hand hand,
			BlockRayTraceResult hit) {
		this.blockState = blockState;
		this.pos = hit.getPos();
		this.world = world;
		this.player = player;
		this.hand = hand;
		this.hit = hit;
		this.itemstack = player.getHeldItem(hand);
		this.item = itemstack.getItem();
	}
	
}
