package io.codebit.support.image.options;

import java.awt.Dimension;
import java.awt.Rectangle;

public interface IRatioOption
{
	
	/**
	 * Ratio.
	 *
	 * @param src the src
	 * @param desc the desc
	 * @return the dimension
	 */
	Dimension dimension(Dimension src, Dimension desc);
	
	/**
	 * 캔버스 사이즈가  canvas 인 이미지에서  
	 * 상대 좌표가 src 좌표 이고 src 사이즈를 가진 이미지를
	 * desc사이즈로 resize하는 Rectangle 객체를 구함  
	 *
	 * @param canvas the canvas
	 * @param src the src
	 * @param desc the desc
	 * @return the rectangle
	 */
	public Rectangle rectangle(Dimension canvas, Rectangle src, Dimension desc);

	
}
