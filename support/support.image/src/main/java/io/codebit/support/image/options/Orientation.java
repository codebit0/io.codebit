package io.codebit.support.image.options;


public enum Orientation
{
	//http://stackoverflow.com/questions/5905868/am-i-making-this-too-complicated-image-rotation
	//http://chunter.tistory.com/143
	TopLeft(1), TopRight(2), BottomRight(3), BottomLeft(4), LeftTop(5), RightTop(6), RightBottom(7), LeftBottom(8);

	private int code;

	public int code() {
		return code;
	}

	private Orientation(int code)
	{
		this.code = code;
	}
}
