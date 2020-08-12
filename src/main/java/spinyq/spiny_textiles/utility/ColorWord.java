package spinyq.spiny_textiles.utility;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum ColorWord {

	WHITE(Color3f.fromIntString("0xffffff")),
	BLACK(Color3f.fromIntString("0x000000")),
	RED(Color3f.fromIntString("0xff0000")),
	LIME(Color3f.fromIntString("0x00ff00")),
	BLUE(Color3f.fromIntString("0x0000ff")),
	DARKRED(Color3f.fromIntString("0x8b0000")),
	CRIMSON(Color3f.fromIntString("0xdc143c")),
	PINK(Color3f.fromIntString("0xffc0cb")),
	HOTPINK(Color3f.fromIntString("0xff69b4")),
	ORANGE(Color3f.fromIntString("0xffa500")),
	ORANGERED(Color3f.fromIntString("0xff4500")),
	GOLD(Color3f.fromIntString("0xffd700")),
	YELLOW(Color3f.fromIntString("0xffff00")),
	LIGHTYELLOW(Color3f.fromIntString("0xffffe0")),
	LAVENDER(Color3f.fromIntString("0xe6e6fa")),
	VIOLET(Color3f.fromIntString("0xee82ee")),
	FUCHSIA(Color3f.fromIntString("0xff00ff")),
	PURPLE(Color3f.fromIntString("0x800080")),
	INDIGO(Color3f.fromIntString("0x4b0082")),
	SLATEBLUE(Color3f.fromIntString("0x6a5acd")),
	SEAGREEN(Color3f.fromIntString("0x2e8b57")),
	GREEN(Color3f.fromIntString("0x008000")),
	DARKGREEN(Color3f.fromIntString("0x006400")),
	TEAL(Color3f.fromIntString("0x008080")),
	CYAN(Color3f.fromIntString("0x00ffff")),
	AQUAMARINE(Color3f.fromIntString("0x7fffd4")),
	TURQUOISE(Color3f.fromIntString("0x40e0d0")),
	LIGHTBLUE(Color3f.fromIntString("0xadd8e6")),
	SKYBLUE(Color3f.fromIntString("0x87ceeb")),
	CORNFLOWERBLUE(Color3f.fromIntString("0x6495ed")),
	ROYALBLUE(Color3f.fromIntString("0x4169e1")),
	NAVY(Color3f.fromIntString("0x000080")),
	TAN(Color3f.fromIntString("0xd2b48c")),
	SIENNA(Color3f.fromIntString("0xa0522d")),
	MAROON(Color3f.fromIntString("0x800000")),
	LIGHTGRAY(Color3f.fromIntString("0xd3d3d3")),
	SILVER(Color3f.fromIntString("0xc0c0c0")),
	GRAY(Color3f.fromIntString("0x808080"));

	private Color3f color;

	private ColorWord(Color3f color) {
		this.color = color;
	}

	/**
	 * Returns a friendly name.
	 * @return
	 */
	public String getName() {
		return this.toString().toLowerCase();
	}
	
	public Color3f getColor() {
		return color;
	}
	
	public static ColorWord getClosest(Color3f to) {
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
