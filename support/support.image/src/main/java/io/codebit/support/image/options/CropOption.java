package io.codebit.support.image.options;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public enum CropOption implements ICropOption
{
	
	//horizontal). Vertical
	
	/**
	 * 이미지의 가로 좌측을 남기고 우측을 잘라냄
	 */
	HORIZONTAL_LEFT
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(0, 0);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			double ratio = (double) destHeight / (double) srcHeight;
			destWidth = (int) Math.round(srcWidth * ratio);
    		destHeight = (int) Math.round(srcHeight * ratio);
    		Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle((int)point.getX(), (int)point.getY(), destWidth, destHeight);
		}
	},
	HORIZONTAL_RIGHT
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			int x = srcWidth -  destWidth;
			int y = 0;
			return new Point(x, y);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			double ratio = (double) destHeight / (double) srcHeight;
			destWidth = (int) Math.round(srcWidth * ratio);
			destHeight = (int) Math.round(srcHeight * ratio);
    		Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle((int)point.getX(), (int)point.getY(), destWidth, destHeight);
		}
	},
	HORIZONTAL_CENTER
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point((srcWidth / 2) - (destWidth / 2), 0);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			double ratio = (double) destHeight / (double) srcHeight;
			destWidth = (int) Math.round(srcWidth * ratio);
			destHeight = (int) Math.round(srcHeight * ratio);
    		Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle((int)point.getX(), (int)point.getY(), destWidth, destHeight);
		}
	},
	VERTICAL_TOP
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(0, 0);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			double ratio = (double) destWidth / (double) srcWidth;
			destWidth = (int) Math.round(srcWidth * ratio);
			destHeight = (int) Math.round(srcHeight * ratio);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle((int)point.getX(), (int)point.getY(), destWidth, destHeight);
		}
	},
	VERTICAL_BOTTOM
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(0, srcHeight - destHeight);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			double ratio = (double) destWidth / (double) srcWidth;
			destWidth = (int) Math.round(srcWidth * ratio);
			destHeight = (int) Math.round(srcHeight * ratio);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle((int)point.getX(), (int)point.getY(), destWidth, destHeight);
		}
	},
	VERTICAL_CENTER
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(0, (srcHeight / 2) - (destHeight / 2));
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			double ratio = (double) destWidth / (double) srcWidth;
			destWidth = (int) Math.round(srcWidth * ratio);
			destHeight = (int) Math.round(srcHeight * ratio);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle((int)point.getX(), (int)point.getY(), destWidth, destHeight);
		}
	},
	
	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * if it is to be placed at the top left-hand corner of the enclosing
	 * image.
	 */
	TOP_LEFT()
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(0, 0);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}
	},
	
	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * if it is to be horizontally centered at the top of the enclosing image.
	 */
	TOP_CENTER()
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(Math.round((srcWidth / 2) - (destWidth / 2)), 0);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}
	},
	
	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * if it is to be placed at the top right-hand corner of the enclosing
	 * image.
	 */
	TOP_RIGHT()
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(srcWidth - destWidth, 0);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}
	},
	
	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * if it is to be placed vertically centered at the left-hand corner of
	 * the enclosing image.
	 */
	CENTER_LEFT()
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(0, (srcHeight /2) - (destHeight / 2));
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}

	},
	
	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * horizontally and vertically centered in the enclosing image.
	 */
	CENTER()
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point((srcWidth /2) - (destWidth / 2) , (srcHeight /2) - (destHeight / 2));
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}
	},

	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * if it is to be placed vertically centered at the right-hand corner of
	 * the enclosing image.
	 */
	CENTER_RIGHT()
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(srcWidth - destWidth , (srcHeight /2) - (destHeight / 2));
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}
	},
	
	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * if it is to be placed at the bottom left-hand corner of the enclosing
	 * image.
	 */
	BOTTOM_LEFT()
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(0 , srcHeight - destHeight);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}
	},
	
	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * if it is to be horizontally centered at the bottom of the enclosing
	 * image.
	 */
	BOTTOM_CENTER()
	{
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point((srcWidth /2) - (destWidth / 2) , srcHeight - destHeight);
		}

		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}
	},
	
	/**
	 * Calculates the {@link Point} at which an enclosed image should be placed
	 * if it is to be placed at the bottom right-hand corner of the enclosing
	 * image.
	 */	
	BOTTOM_RIGHT()
	{
		public Point point(Dimension src, Dimension dest)
		{
			return new Point((int)(src.getWidth() - dest.getWidth()) , (int)(src.getHeight() - dest.getHeight()));
		}
		
		public Point point(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			return new Point(srcWidth - destWidth , srcHeight - destHeight);
		}
		
		public Rectangle rectangle(int srcWidth, int srcHeight, int destWidth, int destHeight)
		{
			Dimension dimension = _dimension(srcWidth, srcHeight, destWidth, destHeight);
			Point point = this.point(srcWidth, srcHeight, destWidth, destHeight);
			return new Rectangle(point, dimension);
		}
	};

	private static Dimension _dimension(Dimension src, Dimension dest)
	{
		double ratio = ratio(src, dest);
		return new Dimension((int) Math.round(src.getWidth() * ratio), (int) Math.round(src.getHeight() * ratio));
	}
	
	private static Dimension _dimension(int srcWidth, int srcHeight, int destWidth, int destHeight)
	{
		double ratio = ratio(srcWidth, srcHeight, destWidth, destHeight);
		destWidth = (int) Math.round(srcWidth * ratio);
		destHeight = (int) Math.round(srcHeight * ratio);
		return new Dimension(destWidth, destHeight);
	}

	public static double ratio(int srcWidth, int srcHeight, int destWidth, int destHeight)
	{
		double ratio = 0;
		if (srcWidth >= srcHeight)
		{
			ratio = (double) destHeight / (double) srcHeight;
		} else
		{
			ratio = (double) destWidth / (double) srcWidth;
		}
		return ratio;
	}
	
	public static double ratio(Dimension src, Dimension dest)
	{
		double ratio = 0;
		if (src.getWidth() > src.getHeight())
		{
			ratio = dest.getHeight() / src.getHeight();
		} else
		{
			ratio = dest.getWidth() / src.getWidth();
		}
		return ratio;
	}
}
