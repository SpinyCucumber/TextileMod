package spinyq.spinytextiles.client.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;

public class ClothingModelManager implements IFutureReloadListener {

	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager,
			IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor,
			Executor gameExecutor) {
		// File IO can run off-thread, so this can be ran asynchronously.
		return CompletableFuture.runAsync(() -> {
			// TODO Load clothing models
		}, backgroundExecutor)
				// Mark that we are finished with our background tasks
				.thenCompose(stage::markCompleteAwaitingOthers);
	}

	// Called when the mod is first constructed
	public void onModConstructed() {
		// Let Minecraft know we manage resources
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this);
	}

}
