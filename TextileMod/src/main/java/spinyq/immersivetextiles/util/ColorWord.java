package spinyq.immersivetextiles.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum ColorWord {

	WHITE(Color.fromIntString("0xffffff")),
	BLACK(Color.fromIntString("0x000000")),
	RED(Color.fromIntString("0xff0000")),
	LIME(Color.fromIntString("0x00ff00")),
	BLUE(Color.fromIntString("0x0000ff")),
	DARKRED(Color.fromIntString("0x8b0000")),
	CRIMSON(Color.fromIntString("0xdc143c")),
	PINK(Color.fromIntString("0xffc0cb")),
	HOTPINK(Color.fromIntString("0xff69b4")),
	ORANGE(Color.fromIntString("0xffa500")),
	ORANGERED(Color.fromIntString("0xff4500")),
	GOLD(Color.fromIntString("0xffd700")),
	YELLOW(Color.fromIntString("0xffff00")),
	LIGHTYELLOW(Color.fromIntString("0xffffe0")),
	LAVENDER(Color.fromIntString("0xe6e6fa")),
	VIOLET(Color.fromIntString("0xee82ee")),
	FUCHSIA(Color.fromIntString("0xff00ff")),
	PURPLE(Color.fromIntString("0x800080")),
	INDIGO(Color.fromIntString("0x4b0082")),
	SLATEBLUE(Color.fromIntString("0x6a5acd")),
	SEAGREEN(Color.fromIntString("0x2e8b57")),
	GREEN(Color.fromIntString("0x008000")),
	DARKGREEN(Color.fromIntString("0x006400")),
	TEAL(Color.fromIntString("0x008080")),
	CYAN(Color.fromIntString("0x00ffff")),
	AQUAMARINE(Color.fromIntString("0x7fffd4")),
	TURQUOISE(Color.fromIntString("0x40e0d0")),
	LIGHTBLUE(Color.fromIntString("0xadd8e6")),
	SKYBLUE(Color.fromIntString("0x87ceeb")),
	CORNFLOWERBLUE(Color.fromIntString("0x6495ed")),
	ROYALBLUE(Color.fromIntString("0x4169e1")),
	NAVY(Color.fromIntString("0x000080")),
	TAN(Color.fromIntString("0xd2b48c")),
	SIENNA(Color.fromIntString("0xa0522d")),
	MAROON(Color.fromIntString("0x800000")),
	LIGHTGRAY(Color.fromIntString("0xd3d3d3")),
	SILVER(Color.fromIntString("0xc0c0c0")),
	GRAY(Color.fromIntString("0x808080"));

	private Color color;

	private ColorWord(Color color) {
		this.color = color;
	}

	/**
	 * Returns a friendly name.
	 * @return
	 */
	public String getName() {
		return this.toString().toLowerCase();
	}
	
	public Color getColor() {
		return color;
	}
	
	public static ColorWord getClosest(Color to) {
		List<ColorWord> list = Arrays.asList(ColorWord.values());
		Comparator<ColorWord> comparator = new Comparator<ColorWord>() {

			@Override
			public int compare(ColorWord a, ColorWord b) {
				return (int) Math.signum(b.getColor().distanceTo(to) - a.getColor().distanceTo(to));
			}
			
		};
		return list.stream().max(comparator).get();
	}
	
}
