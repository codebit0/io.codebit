package io.codebit.support.image.options;

import java.awt.Point;

public interface ICropOption
{
	public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight);
	
//	public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight);
	
}
