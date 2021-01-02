package spinyq.spinytextiles.utility.textile.clothing;

public interface IClothing {

	<T> T getPartData(ClothingPart part);
	<T> void setPartData(ClothingPart part, T data);
	ClothingPattern getPattern();
	void setPattern(ClothingPattern pattern);
	
}
