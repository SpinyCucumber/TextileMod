package spinyq.spinytextiles.utility.color;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum ColorWord {

	WHITE(new RYBKColor(0.0f, 0.0f, 0.0f, 0.0f)),
	BLACK(new RYBKColor(0.0f, 0.0f, 0.0f, 1.0f)),
	RED(new RYBKColor(1.0f, 0.0f, 0.0f, 0.0f)),
	YELLOW(new RYBKColor(0.0f, 1.0f, 0.0f, 0.0f)),
	BLUE(new RYBKColor(0.0f, 0.0f, 1.0f, 0.0f)),
	ORANGE(new RYBKColor(1.0f, 1.0f, 0.0f, 0.0f)),
	GREEN(new RYBKColor(0.0f, 1.0f, 1.0f, 0.0f)),
	PURPLE(new RYBKColor(1.0f, 0.0f, 1.0f, 0.0f)),
	BROWN(new RYBKColor(1.0f, 1.0f, 1.0f, 0.0f));

	public final RYBKColor color;
	private String translationKey;

	private ColorWord(RYBKColor color) {
		this.color = color;
	}

	/**
	 * Returns a translation key which can be translated
	 * to the name of this color.
	 */
	public String getTranslationKey() {
		if (translationKey == null) translationKey = "color." + toString().toLowerCase();
		return translationKey;
	}
	
	/**
	 * Returns a translation key which can be
	 * used to describe the color an object.
	 */
	public String getDescriptionTranslationKey() {
		return getTranslationKey() + ".description";
	}

	public static ColorWord getClosest(RYBKColor to) {
		List<ColorWord> list = Arrays.asList(ColorWord.values());
		Comparator<ColorWord> comparator = new Comparator<ColorWord>() {

			@Override
			public int compare(ColorWord a, ColorWord b) {
				return (int) Math.signum(b.color.dist(to) - a.color.dist(to));
			}

		};
		return list.stream().max(comparator).get();
	}

}
