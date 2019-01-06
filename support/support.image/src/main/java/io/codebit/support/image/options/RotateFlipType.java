package io.codebit.support.image.options;

/**
 * 이미지가 회전되는 양과 이미지를 대칭 이동하는 데 사용할 축을 지정합니다.
 */
public enum RotateFlipType
{
	// 1
	/**
	 * 수평(vertical) 및 수직(horizontal) 이동 후 180도 시계 방향 회전을 지정합니다.
	 * RotateNoneFlipNone 동일
	 */
	Rotate180FlipXY(Orientation.TopLeft),
	/** 시계 방향 회전 안 함과 대칭 이동 안 함을 지정합니다. Rotate180FlipXY 동일 */
	RotateNoneFlipNone(Orientation.TopLeft),

	// 8
	/** 대칭 이동 없는 90도 시계 방향 회전을 지정합니다. Rotate270FlipXY 동일 */
	Rotate90FlipNone(Orientation.LeftBottom),
	/**
	 * 수평(vertical) 및 수직(horizontal) 이동 후 270도 시계 방향 회전을 지정합니다.
	 * Rotate90FlipNone동일
	 */
	Rotate270FlipXY(Orientation.LeftBottom),

	// 3
	/** 대칭 이동 없는 180도 시계 방향 회전을 지정합니다. RotateNoneFlipXY 동일 */
	Rotate180FlipNone(Orientation.BottomRight),
	/**
	 * 수평(vertical) 및 수직(horizontal) 대칭 이동 후 시계 방향 회전 안 함을 지정합니다.
	 * Rotate180FlipNone 동일
	 */
	RotateNoneFlipXY(Orientation.BottomRight),

	// 6
	/** 대칭 이동 없는 270도 시계 방향 회전을 지정합니다. Rotate90FlipXY 동일 */
	Rotate270FlipNone(Orientation.RightTop),
	/**
	 * 수평(vertical) 및 수직(horizontal) 이동 후 90도 시계 방향 회전을 지정합니다.
	 * Rotate270FlipNone 동일
	 */
	Rotate90FlipXY(Orientation.RightTop),

	// 2
	/** 수직(horizontal) 대칭 이동 후 시계 방향 회전 안 함을 지정합니다. Rotate180FlipX 동일 */
	RotateNoneFlipY(Orientation.TopRight),
	/** 수평(vertical) 대칭 이동 후 180도 시계 방향 회전을 지정합니다. RotateNoneFlipY와 동일 */
	Rotate180FlipX(Orientation.TopRight),

	// 7
	/** 수직(horizontal) 대칭 이동 후 90도 시계 방향 회전을 지정합니다. Rotate270FlipX 동일 */
	Rotate90FlipY(Orientation.RightBottom),
	/** 수평(vertical) 대칭 이동 후 270도 시계 방향 회전을 지정합니다. Rotate90FlipY 동일 */
	Rotate270FlipX(Orientation.RightBottom),

	// 4
	/** 수직 대칭 이동 후 180도 시계 방향 회전을 지정합니다. RotateNoneFlipX 동일 */
	Rotate180FlipY(Orientation.BottomLeft),
	/** 수평(vertical) 대칭 이동 후 시계 방향 회전 안 함을 지정합니다. Rotate180FlipY 동일 */
	RotateNoneFlipX(Orientation.BottomLeft),

	// 5
	/** 수직(horizontal) 대칭 이동 후 270도 시계 방향 회전을 지정합니다. Rotate270FlipY 동일 */
	Rotate270FlipY(Orientation.LeftTop),
	/** 수평(vertical) 대칭 이동 후 90도 시계 방향 회전을 지정합니다. Rotate270FlipY 동일 */
	Rotate90FlipX(Orientation.LeftTop);

	public Orientation orientation() {
		return orientation;
	}

	private Orientation orientation;

	private RotateFlipType(Orientation orientation)
	{
		this.orientation = orientation;
	}
}