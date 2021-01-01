package spinyq.spinytextiles.utility.textile;

import java.util.Iterator;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spinyq.spinytextiles.utility.color.RYBKColor;

public interface IFabric {

	static final Logger LOGGER = LogManager.getLogger();

	void setPattern(FabricPattern pattern);
	FabricPattern getPattern();
	void setLayerColor(FabricLayer layer, RYBKColor color);
	RYBKColor getLayerColor(FabricLayer layer);
	
	// Makes this fabric equal to another.
	default void set(IFabric other) {
		FabricPattern pattern = other.getPattern();
		setPattern(pattern);
		pattern.getLayerStream().forEach((layer) -> {
			setLayerColor(layer, other.getLayerColor(layer));
		});
	}
	
	default void setLayerColor(int index, RYBKColor color) {
		setLayerColor(getPattern().getLayer(index), color);
	}
	
	default RYBKColor getLayerColor(int index) {
		return getLayerColor(getPattern().getLayer(index));
	}
	
	/**
	 * Sets all layers of this fabric to the given color.
	 * 
	 * @param color The new color
	 */
	default void setColor(RYBKColor color) {
		getPattern().getLayerStream().forEach((layer) -> setLayerColor(layer, color));
	}
	
	default void reduceColors() {
		LOGGER.trace("Reducing colors...");
		// Determine if the fabric is monochrome
		Optional<RYBKColor> monochrome = getMonochrome();
		LOGGER.trace("Monochrome: {}", monochrome);
		// If the fabric is monochrome, look up what pattern we should
		// switch to using the fabric pattern
		// Otherwise simply return the same fabric
		if (monochrome.isPresent()) {
			// Only construct a new fabric if the fabric pattern
			// supports it
			Optional<FabricPattern> newPattern = getPattern().getMonochromePattern();
			LOGGER.trace("New pattern: {}", newPattern.map(FabricPattern::getRegistryName));
			if (newPattern.isPresent()) {
				// Change to the new pattern and set all colors to the
				// monochrome color
				setPattern(newPattern.get());
				setColor(monochrome.get());
			}
		}
	}
	
	/**
	 * Returns an optional color that is only present if the fabric is monochrome.
	 * That is, if all the layers are the same color, this method returns that
	 * color.
	 */
	default Optional<RYBKColor> getMonochrome() {
		Iterator<RYBKColor> colorIterator = getPattern().getLayerStream().map(this::getLayerColor).iterator();
		// Get the first color
		RYBKColor toMatch = colorIterator.next();
		LOGGER.trace("Color to match: {}", toMatch);
		// If any other color doesn't match the first color, fail
		// Otherwise return the color
		while (colorIterator.hasNext()) {
			RYBKColor next = colorIterator.next();
			boolean match = toMatch.equalsRGB(next);
			LOGGER.trace("Comparing: {} Match: {}", next, match);
			if (!match)
				return Optional.empty();
		}
		return Optional.of(toMatch);
	}
	
}
