package spinyq.spinytextiles.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

/**
 * Decent amount of code adapted from shapeless recipe. Specifies a shapeless
 * recipe which uses a brush. Damages the brush upon use.
 * 
 * @author Elijah Hilty
 *
 */
public class BrushRecipe implements ICraftingRecipe {

	private static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * Represents possible inputs to a brush crafting recipe.
	 * @author Elijah Hilty
	 *
	 */
	public static class BrushInputs {
		
		public Optional<ItemStack> brush;
		public int brushPos; // Only valid if brush is present
		public List<ItemStack> ingredients;
		
	}
	
	private ResourceLocation id;
	// Used when displaying recipes to the user
	private String group;
	private ItemStack recipeOutput;
	private NonNullList<Ingredient> recipeItems;
	// Needed for damaging the brush item
	private Random random = new Random();

	public BrushRecipe(ResourceLocation id, String group, ItemStack recipeOutput, NonNullList<Ingredient> recipeItems) {
		super();
		this.id = id;
		this.group = group;
		this.recipeOutput = recipeOutput;
		this.recipeItems = recipeItems;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return this.recipeOutput;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Recipes with equal group are combined into one button in the recipe book
	 */
	public String getGroup() {
		return this.group;
	}

	private boolean isBrush(ItemStack stack) {
		// Check tag
		return stack.getItem().getTags().contains(ModTags.BRUSH_TAG);
	}
	
	/**
	 * Retrieve possible inputs to a brush crafting recipe from an inventory.
	 */
	private BrushInputs extractInputs(CraftingInventory inv) {
		BrushInputs result = new BrushInputs();
		// Set brush and ingredients to be empty initially
		result.brush = Optional.empty();
		result.ingredients = new ArrayList<>();
		// Scan items in inventory
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			// Skip empty items
			if (stack.isEmpty()) continue;
			// If the item is a brush and we don't have a brush already, set the item to be our brush.
			// Otherwise, add the item to the list of other ingredients.
			if(isBrush(stack) && !result.brush.isPresent()) {
				result.brush = Optional.of(stack);
				result.brushPos = i;
			}
			else result.ingredients.add(stack);
		}
		// Done
		return result;
	}
	
	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {

		// Extract possible inputs
		BrushInputs inputs = extractInputs(inv);
		
		// If brush is not present we can exit
		if (!inputs.brush.isPresent()) return false;

		// Next compare the other inputs with the recipe's inputs
		boolean matches = (inputs.ingredients.size() == this.recipeItems.size() && RecipeMatcher.findMatches(inputs.ingredients, this.recipeItems) != null);

		LOGGER.trace("inputs: {} recipeItems: {} matches: {}", inputs, recipeItems, matches);
		return matches;

	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		// Get initial list from supermethod
		NonNullList<ItemStack> result = ICraftingRecipe.super.getRemainingItems(inv);
		// Look for brush
		BrushInputs inputs = extractInputs(inv);
		// If there is a brush present, decrement the damage by one
		if (inputs.brush.isPresent()) {
			ItemStack stack = inputs.brush.get();
			// Damage it here
			boolean broken = stack.attemptDamageItem(1, random, null);
			// If the stack isn't broken, add it as a remaining item.
			LOGGER.trace("stack: {} broken: {} empty: {}", stack, broken, stack.isEmpty());
			if (!broken) {
				ItemStack newStack = stack.copy();
				newStack.setCount(1);
				result.set(inputs.brushPos, newStack);
			}
		}
		LOGGER.trace("result: {}", result);
		// Return list
		return result;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		LOGGER.trace("getCraftingResult....");
		return recipeOutput.copy();
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	public boolean canFit(int width, int height) {
		LOGGER.trace("canFit....");
		return width * height >= (this.recipeItems.size() + 1);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return ModRecipes.CRAFTING_BRUSH.get();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
			implements IRecipeSerializer<BrushRecipe> {

		public BrushRecipe read(ResourceLocation recipeId, JsonObject json) {
			String s = JSONUtils.getString(json, "group", "");
			NonNullList<Ingredient> nonnulllist = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
			if (nonnulllist.isEmpty()) {
				throw new JsonParseException("No ingredients for shapeless recipe");
				// There used to be a check here to make sure there weren't too many
				// ingredients, but it used fields that weren't visible.
				// Tried to use access transformers but they were a pain in the ass w/ Eclipse.
			} else {
				ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
				return new BrushRecipe(recipeId, s, itemstack, nonnulllist);
			}
		}

		private static NonNullList<Ingredient> readIngredients(JsonArray p_199568_0_) {
			NonNullList<Ingredient> nonnulllist = NonNullList.create();

			for (int i = 0; i < p_199568_0_.size(); ++i) {
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

			for (int j = 0; j < nonnulllist.size(); ++j) {
				nonnulllist.set(j, Ingredient.read(buffer));
			}

			ItemStack itemstack = buffer.readItemStack();
			return new BrushRecipe(recipeId, s, itemstack, nonnulllist);
		}

		public void write(PacketBuffer buffer, BrushRecipe recipe) {
			buffer.writeString(recipe.group);
			buffer.writeVarInt(recipe.recipeItems.size());

			for (Ingredient ingredient : recipe.recipeItems) {
				ingredient.write(buffer);
			}

			buffer.writeItemStack(recipe.recipeOutput);
		}

	}

}
