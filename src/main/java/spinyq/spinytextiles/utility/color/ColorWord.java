package spinyq.spinytextiles.utility.color;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public enum ColorWord {

	WHITE(new RGBColor().fromIntString("0xffffff")), BLACK(new RGBColor().fromIntString("0x000000")),
	RED(new RGBColor().fromIntString("0xff0000")), LIME(new RGBColor().fromIntString("0x00ff00")),
	BLUE(new RGBColor().fromIntString("0x0000ff")), DARKRED(new RGBColor().fromIntString("0x8b0000")),
	CRIMSON(new RGBColor().fromIntString("0xdc143c")), PINK(new RGBColor().fromIntString("0xffc0cb")),
	HOTPINK(new RGBColor().fromIntString("0xff69b4")), ORANGE(new RGBColor().fromIntString("0xffa500")),
	ORANGERED(new RGBColor().fromIntString("0xff4500")), GOLD(new RGBColor().fromIntString("0xffd700")),
	YELLOW(new RGBColor().fromIntString("0xffff00")), LIGHTYELLOW(new RGBColor().fromIntString("0xffffe0")),
	LAVENDER(new RGBColor().fromIntString("0xe6e6fa")), VIOLET(new RGBColor().fromIntString("0xee82ee")),
	FUCHSIA(new RGBColor().fromIntString("0xff00ff")), PURPLE(new RGBColor().fromIntString("0x800080")),
	INDIGO(new RGBColor().fromIntString("0x4b0082")), SLATEBLUE(new RGBColor().fromIntString("0x6a5acd")),
	SEAGREEN(new RGBColor().fromIntString("0x2e8b57")), GREEN(new RGBColor().fromIntString("0x008000")),
	DARKGREEN(new RGBColor().fromIntString("0x006400")), TEAL(new RGBColor().fromIntString("0x008080")),
	CYAN(new RGBColor().fromIntString("0x00ffff")), AQUAMARINE(new RGBColor().fromIntString("0x7fffd4")),
	TURQUOISE(new RGBColor().fromIntString("0x40e0d0")), LIGHTBLUE(new RGBColor().fromIntString("0xadd8e6")),
	SKYBLUE(new RGBColor().fromIntString("0x87ceeb")), CORNFLOWERBLUE(new RGBColor().fromIntString("0x6495ed")),
	ROYALBLUE(new RGBColor().fromIntString("0x4169e1")), NAVY(new RGBColor().fromIntString("0x000080")),
	TAN(new RGBColor().fromIntString("0xd2b48c")), SIENNA(new RGBColor().fromIntString("0xa0522d")),
	MAROON(new RGBColor().fromIntString("0x800000")), LIGHTGRAY(new RGBColor().fromIntString("0xd3d3d3")),
	SILVER(new RGBColor().fromIntString("0xc0c0c0")), GRAY(new RGBColor().fromIntString("0x808080"));

	public final RGBColor rgb;
	public final RYBKColor ryb;

	private ColorWord(RGBColor rgb) {
		this.rgb = rgb;
		ryb = rgb.toRYB(new RYBKColor(), Optional.empty());
	}

	/**
	 * Returns a friendly name.
	 * 
	 * @return
	 */
	public String getName() {
		return this.toString().toLowerCase();
	}

	public static ColorWord getClosest(RYBKColor to) {
		List<ColorWord> list = Arrays.asList(ColorWord.values());
		Comparator<ColorWord> comparator = new Comparator<ColorWord>() {

			@Override
			public int compare(ColorWord a, ColorWord b) {
				return (int) Math.signum(b.ryb.dist(to) - a.ryb.dist(to));
			}

		};
		return list.stream().max(comparator).get();
	}

}
