package spinyq.spinytextiles.client.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;
import spinyq.spinytextiles.utility.registry.LazyForgeRegistry;
import spinyq.spinytextiles.utility.textile.Fabric;
import spinyq.spinytextiles.utility.textile.FabricPattern;

/**
 * Assigns fabric patterns textures for each of their layers.
 * 
 * @author SpinyQ
 *
 */
@OnlyIn(Dist.CLIENT)
public class FabricTextureManager implements IFutureReloadListener {

	public static final FabricTextureManager INSTANCE = new FabricTextureManager();
	private static final IForgeRegistry<FabricPattern> PATTERN_REGISTRY = LazyForgeRegistry.of(FabricPattern.class);
	private static final Gson SERIALIZER = new GsonBuilder()
			.registerTypeAdapter(FabricTextures.class, new FabricTextures.Deserializer()).create();

	// TODO Will need to change this.
	// We should probably use our own atlas.
	@SuppressWarnings("deprecation")
	private static final ResourceLocation ATLAS_LOCATION = AtlasTexture.LOCATION_BLOCKS_TEXTURE;

	/**
	 * Provides textures for each layer of a fabric pattern.
	 * 
	 * @author SpinyQ
	 *
	 */
	private static class FabricTextures {

		private static class Deserializer implements JsonDeserializer<FabricTextures> {

			@Override
			public FabricTextures deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				// Start constructing a map
				ImmutableMap.Builder<String, Material> builder = new ImmutableMap.Builder<>();
				// Retrieve the "textures" field
				JsonObject texturesObject = json.getAsJsonObject().get("textures").getAsJsonObject();
				// For each entry in the textures field, treat the key as the layer id
				// and treat the value as the location of a texture
				for (Entry<String, JsonElement> entry : texturesObject.entrySet()) {
					// Parse the right side as a resource location
					String textureStr = entry.getValue().getAsString();
					ResourceLocation textureLocation = ResourceLocation.tryCreate(textureStr);
					// Check if the texture location was parsed or not
					// If not, throw an exception
					// Otherwise, construct a new material and put the entry in the map
					if (textureLocation == null) {
						throw new JsonParseException(textureStr + " is not valid resource location");
					} else {
						String layer = entry.getKey();
						Material texture = new Material(ATLAS_LOCATION, textureLocation);
						builder.put(layer, texture);
					}
				}
				// Build the map and construct a new FabricTextures
				return new FabricTextures(builder.build());
			}

		}

		private final ImmutableMap<String, Material> map;

		private FabricTextures(ImmutableMap<String, Material> textures) {
			this.map = textures;
		}

		private Material get(String layer) {
			return map.get(layer);
		}

		private Collection<Material> values() {
			return map.values();
		}

	}

	// The internal map between fabric patterns and textures
	private Map<FabricPattern, FabricTextures> map = new HashMap<>();

	/**
	 * Returns a list of the corresponding texture for each layer in the fabric
	 * pattern. The list is in order of layers and is unmodifiable.
	 * 
	 * @param pattern The fabric pattern
	 * @return The list
	 */
	public List<Material> getTextureList(FabricPattern pattern) {
		// Use streams to create a new list
		// We use the pattern's list to ensure that the textures are in the right order
		// First, look up the fabric textures
		FabricTextures textures = map.get(pattern);
		return Collections
				.unmodifiableList(pattern.getLayers().stream().map(textures::get).collect(Collectors.toList()));
	}

	/**
	 * Returns of collection of textures that the fabric pattern uses. The
	 * collection has no order is unmodifiable. If the user wants a list of textures
	 * ordered by layer, they should use getTextureList.
	 * 
	 * @param pattern The fabric pattern
	 * @return The collection
	 */
	public Collection<Material> getTextures(FabricPattern pattern) {
		// Lookup textures and return values
		FabricTextures textures = map.get(pattern);
		return Collections.unmodifiableCollection(textures.values());
	}

	// The following are convenience methods that retrieve the textures for a
	// fabric.

	public List<Material> getTextureList(Fabric fabric) {
		return getTextureList(fabric.getPattern());
	}

	public Collection<Material> getTextures(Fabric fabric) {
		return getTextures(fabric.getPattern());
	}

	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager,
			IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor,
			Executor gameExecutor) {
		// File IO can run off-thread, so this can be ran asynchronously.
		return CompletableFuture.runAsync(() -> {
			// Load fabric textures.
			// For each fabric pattern in the registry, load the list of textures from a
			// JSON file.
			for (FabricPattern pattern : PATTERN_REGISTRY.getValues()) {
				// Get the location of the textures file
				ResourceLocation texturesLocation = getTexturesLocation(pattern);
				// Get the resource at the location and create a reader to load it
				try {
					IResource resource = resourceManager.getResource(texturesLocation);
					Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
					// Parse JSON and put textures into the map
					FabricTextures textures = JSONUtils.fromJson(SERIALIZER, reader, FabricTextures.class);
					map.put(pattern, textures);
				} catch (IOException e) {
					throw new RuntimeException("Failed to load textures file.", e);
				}
			}
			
		}, backgroundExecutor).thenCompose(stage::markCompleteAwaitingOthers);
	}

	/**
	 * Gets the location of a fabric pattern's textures file.
	 * 
	 * @param pattern The fabric pattern
	 * @return The location of the textures file
	 */
	private ResourceLocation getTexturesLocation(FabricPattern pattern) {
		ResourceLocation id = pattern.getRegistryName();
		return new ResourceLocation(id.getNamespace(), "fabrics/" + id.getPath() + ".json");
	}

	// Called when the mod is first constructed
	public void onModConstructed() {
		// Let Minecraft know we manage resources
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this);
	}

}
