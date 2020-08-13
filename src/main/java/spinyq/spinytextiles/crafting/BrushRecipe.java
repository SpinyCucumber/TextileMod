package spinyq.spinytextiles.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.ForgeRegistryEntry;
import spinyq.spinytextiles.ModRecipes;
import spinyq.spinytextiles.ModTags;
import spinyq.spinytextiles.TextileMod;

/**
 * Decent amount of code adapted from shapeless recipe.
 * Specifies a shapeless recipe which uses a brush.
 * Damages the brush upon use.
 * 
 * @author Elijah Hilty
 *
 */
public class BrushRecipe implements ICraftingRecipe {
	
	private ResourceLocation id;
	// Used when displaying recipes to the user
	private String group;
	private ItemStack recipeOutput;
	private NonNullList<Ingredient> recipeItems;
	// Needed for damaging the brush item
	private Random random = new Random();

	public BrushRecipe(ResourceLocation id, String group, ItemStack recipeOutput,
			NonNullList<Ingredient> recipeItems) {
		super();
		this.id = id;
		this.group = group;
		this.recipeOutput = recipeOutput;
		this.recipeItems = recipeItems;
	}

	public ItemStack getRecipeOutput() {
		return this.recipeOutput;
	}

	/**
	 * Recipes with equal group are combined into one button in the recipe book
	 */
	public String getGroup() {
		return this.group;
	}

	public NonNullList<Ingredient> getRecipeItems() {
		return recipeItems;
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {

		// First, check if there is a brush
		int brushPos = findBrush(inv);

		if (brushPos == -1)
			return false;
		else {
			// Compile a list of the other inputs
			// Make sure to skip the brush
			List<ItemStack> inputs = new ArrayList<>();

			for (int j = 0; j < inv.getSizeInventory(); ++j) {
				if (brushPos == j)
					continue;
				ItemStack itemstack = inv.getStackInSlot(j);
				if (!itemstack.isEmpty())
					inputs.add(itemstack);
			}

			// Return true if the remaining inputs match the inputs specified in the recipe
			return (inputs.size() == this.recipeItems.size())
					&& (RecipeMatcher.findMatches(inputs, this.recipeItems) != null);
		}

	}

	/**
	 * Attempts to find a brush item in the given crafting inventory.
	 * 
	 * @param inv
	 * @return The integer of the brush, if there is one. (Else -1)
	 */
	private int findBrush(CraftingInventory inv) {
		for (int j = 0; j < inv.getSizeInventory(); ++j) {
			ItemStack itemstack = inv.getStackInSlot(j);
			if (itemstack.getItem().getTags().contains(ModTags.BRUSH_TAG))
				return j;
		}
		return -1;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		// Get initial list from supermethod
		NonNullList<ItemStack> result = ICraftingRecipe.super.getRemainingItems(inv);
		// Look for brush
		int brushPos = findBrush(inv);
		// If there is a brush present, decrement the damage by one
		TextileMod.LOGGER.info("BrushRecipe getRemainingItems... brushPos: {}", brushPos);
		if (brushPos != -1) {
			ItemStack stack = inv.getStackInSlot(brushPos);
			boolean broken = false;
			// If the stack isn't broken, add it as a remaining item.
			TextileMod.LOGGER.info("stack: {} broken: {}", stack, broken);
			if (!broken)
				result.set(brushPos, stack);
		}
		TextileMod.LOGGER.info("result: {}", result);
		// Return list
		return result;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		return recipeOutput;
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	public boolean canFit(int width, int height) {
		return width * height >= this.recipeItems.size();
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return ModRecipes.CRAFTING_BRUSH.get();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<BrushRecipe> {

	    public BrushRecipe read(ResourceLocation recipeId, JsonObject json) {
	       String s = JSONUtils.getString(json, "group", "");
	       NonNullList<Ingredient> nonnulllist = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
	       if (nonnulllist.isEmpty()) {
	          throw new JsonParseException("No ingredients for shapeless recipe");
	       // There used to be a check here to make sure there weren't too many ingredients, but it used fields that weren't visible.
	       // Tried to use access transformers but they were a pain in the ass w/ Eclipse.
	       } else {
	          ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
	          return new BrushRecipe(recipeId, s, itemstack, nonnulllist);
	       }
	    }

	    private static NonNullList<Ingredient> readIngredients(JsonArray p_199568_0_) {
	       NonNullList<Ingredient> nonnulllist = NonNullList.create();

	       for(int i = 0; i < p_199568_0_.size(); ++i) {
	          Ingredient ingredient = Ingredient.deserialize(p_199568_0_.get(i));
	          if (!ingredient.hasNoMatchingItems()) {
	             nonnulllist.add(ingredient);
	          }
	       }

	       return nonnulllist;
	    }

	    public BrushRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
	       String s = buffer.readString(32767);
	       int i = buffer.readVarInt();
	       NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

	       for(int j = 0; j < nonnulllist.size(); ++j) {
	          nonnulllist.set(j, Ingredient.read(buffer));
	       }

	       ItemStack itemstack = buffer.readItemStack();
	       return new BrushRecipe(recipeId, s, itemstack, nonnulllist);
	    }

	    public void write(PacketBuffer buffer, BrushRecipe recipe) {
	       buffer.writeString(recipe.group);
	       buffer.writeVarInt(recipe.recipeItems.size());

	       for(Ingredient ingredient : recipe.recipeItems) {
	          ingredient.write(buffer);
	       }

	       buffer.writeItemStack(recipe.recipeOutput);
	    }

	}

}
