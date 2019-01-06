package io.codebit.support.image.options;

import java.awt.Dimension;
import java.awt.Rectangle;

public enum RatioOption implements IRatioOption
{
	/** 
	 * 비율을 적용하여 resize합니다. 
	 * 요청한 가로세로 사이즈 보다는 작거나 같은 이미지를 반환함
	 * desc 의 width 가 0보다 같거나 작은 경우 height값을 기준으로 비율을 적용합니다.
	 * desc 의 height 가 0보다 같거나 작은 경우 width값을 기준으로 비율을 적용합니다.
	 */
	AspectRatio
	{
		@Override
		public Rectangle rectangle(Dimension canvas, Rectangle src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = Integer.MAX_VALUE;
			}else if(desc.height <= 0)
			{
				desc.height = Integer.MAX_VALUE;
			}
			double wratio = (double) desc.width / canvas.width;
			double hratio = (double) desc.height / canvas.height;
			double ratio = Math.min(wratio, hratio);
			
			int dx = (int) Math.ceil(src.x * ratio);
			int dy = (int) Math.ceil(src.y * ratio);
			int dw = (int) Math.ceil(src.width * ratio);
			int dh = (int) Math.ceil(src.height * ratio);
			
			return new Rectangle(dx, dy, dw, dh);
		}
	
		@Override
		public Dimension dimension(Dimension src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = Integer.MAX_VALUE;
			}else if(desc.height <= 0)
			{
				desc.height = Integer.MAX_VALUE;
			}
			double wratio = (double) desc.width / src.width;
			double hratio = (double) desc.height / src.height;
			//작은쪽 ratio를 선택함, 이미지를 비율에 맞춰 최대한 작게 만듬
			double ratio = Math.min(wratio, hratio);
			
			int dw = (int) Math.ceil(src.width * ratio);
			int dh = (int) Math.ceil(src.height * ratio);
			
			return new Dimension(dw, dh);
		}
	},
	
	/** (!) Ratio 적용 없이 주어진 사이즈를 강제로 맞춤 
	 * desc 의 width 가 0보다 같거나 작은 경우 src 의 width값을 적용합니다. (height만 줄어듬)
	 * desc 의 height 가 0보다 같거나 작은 경우 src의 height값을 적용합니다. (width만 줄어듬)
	 */
	IgnoreAspectRatio
	{
		@Override
		public Rectangle rectangle(Dimension canvas, Rectangle src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = src.width;
			}else if(desc.height <= 0)
			{
				desc.height = src.height;
			}
			double wratio = (double) desc.width / canvas.width;
			double hratio = (double) desc.height / canvas.height;
			
			int dx = (int) Math.ceil(src.x * wratio);
			int dy = (int) Math.ceil(src.y * hratio);
			int dw = (int) Math.ceil(src.width * wratio);
			int dh = (int) Math.ceil(src.height * hratio);
			
			return new Rectangle(dx, dy, dw, dh);
		}
		
		@Override
		public Dimension dimension(Dimension src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = src.width;
			}else if(desc.height <= 0)
			{
				desc.height = src.height;
			}
			return desc;
		}
	},
	
	/** 
	 * 이미지를 축소하는 경우에만 비율을 적용하여 resize합니다. 
	 * 축소할려는 사이즈가 원본보다 작은 경우만 리사이즈함
	 * desc 의 width 가 0보다 같거나 작은 경우 height값을 기준으로 비율을 적용합니다.
	 * desc 의 height 가 0보다 같거나 작은 경우 width값을 기준으로 비율을 적용합니다.
	 */
	OnlyShrinkLarger
	{
		@Override
		public Rectangle rectangle(Dimension canvas, Rectangle src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = Integer.MAX_VALUE;
			}else if(desc.height <= 0)
			{
				desc.height = Integer.MAX_VALUE;
			}
			double wratio = (double) desc.width / canvas.width;
			double hratio = (double) desc.height / canvas.height;
			double ratio = Math.min(wratio, hratio);
			if(ratio < 1)
			{
				int dx = (int) Math.ceil(src.x * ratio);
				int dy = (int) Math.ceil(src.y * ratio);
				int dw = (int) Math.ceil(src.width * ratio);
				int dh = (int) Math.ceil(src.height * ratio);
				
				return new Rectangle(dx, dy, dw, dh);
			}
			return src;
		}
		
		@Override
		public Dimension dimension(Dimension src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = Integer.MAX_VALUE;
			}else if(desc.height <= 0)
			{
				desc.height = Integer.MAX_VALUE;
			}
			double wratio = (double) desc.width / src.width;
			double hratio = (double) desc.height / src.height;
			double ratio = Math.min(wratio, hratio);
			//작은쪽 ratio를 선택함
			if (ratio < 1)
			{
				int dw = (int) Math.ceil(src.width * ratio);
				int dh = (int) Math.ceil(src.height * ratio);
				
				return new Dimension(dw, dh);
			}
			
			return src;
		}
	},
	
	/** 
	 * OnlyShrinkLarger 반대
	 * 이미지를 확장해야 하는 경우만 리사이즈함
	 * desc 의 width 가 0보다 같거나 작은 경우 height값을 기준으로 비율을 적용합니다.
	 * desc 의 height 가 0보다 같거나 작은 경우 width값을 기준으로 비율을 적용합니다.
	 */
	OnlyEnlargeSmaller
	{
		@Override
		public Rectangle rectangle(Dimension canvas, Rectangle src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = Integer.MAX_VALUE;
			}else if(desc.height <= 0)
			{
				desc.height = Integer.MAX_VALUE;
			}
			double wratio = (double) desc.width / canvas.width;
			double hratio = (double) desc.height / canvas.height;
			double ratio = Math.min(wratio, hratio);
			if(ratio > 1)
			{
				int dx = (int) Math.ceil(src.x * ratio);
				int dy = (int) Math.ceil(src.y * ratio);
				int dw = (int) Math.ceil(src.width * ratio);
				int dh = (int) Math.ceil(src.height * ratio);
				
				return new Rectangle(dx, dy, dw, dh);
			}
			return src;
		}
		
		@Override
		public Dimension dimension(Dimension src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = Integer.MAX_VALUE;
			}else if(desc.height <= 0)
			{
				desc.height = Integer.MAX_VALUE;
			}
			double wratio = (double) desc.width / src.width;
			double hratio = (double) desc.height / src.height;
			double ratio = Math.min(wratio, hratio);
			//작은쪽 ratio를 선택함
			if (ratio > 1)
			{
				int dw = (int) Math.ceil(src.width * ratio);
				int dh = (int) Math.ceil(src.height * ratio);
				
				return new Dimension(dw, dh);
			}
			
			return src;
		}
	},
	
	/** 
	 * 비율을 적용하여 resize합니다.
	 * 요청한 가로세로 사이즈보다 크거나 같은 이미지를 반환합니다.
	 * 예를 들어 500*500 이미지를 600*1000 으로 resize를 요청하면 하면 
	 * AspectRatio 옵션은 600*600으로 리사이즈하나 
	 * FillGivenArea 옵션은 1000*1000으로 리사이즈함 
	 * 
	 * desc 의 width 가 0보다 같거나 작은 경우 height값을 기준으로 비율을 적용합니다.
	 * desc 의 height 가 0보다 같거나 작은 경우 width값을 기준으로 비율을 적용합니다.
	 */
	OuterAspectRatio
	{
		@Override
		public Rectangle rectangle(Dimension canvas, Rectangle src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = Integer.MIN_VALUE;
			}else if(desc.height <= 0)
			{
				desc.height = Integer.MIN_VALUE;
			}
			double wratio = (double) desc.width / canvas.width;
			double hratio = (double) desc.height / canvas.height;
			double ratio = Math.max(wratio, hratio);
			
			int dx = (int) Math.ceil(src.x * ratio);
			int dy = (int) Math.ceil(src.y * ratio);
			int dw = (int) Math.ceil(src.width * ratio);
			int dh = (int) Math.ceil(src.height * ratio);
			
			return new Rectangle(dx, dy, dw, dh);
		}
		
		@Override
		public Dimension dimension(Dimension src, Dimension desc)
		{
			if(desc.width <= 0)
			{
				desc.width = Integer.MIN_VALUE;
			}else if(desc.height <= 0)
			{
				desc.height = Integer.MIN_VALUE;
			}
			double wratio = (double) desc.width / src.width;
			double hratio = (double) desc.height / src.height;
			//큰쪽을 선택함, 이미지를 비율에 맞춰 최대한 크게 만듬
			double ratio = Math.max(wratio, hratio);

			int dw = (int) Math.ceil(src.width * ratio);
			int dh = (int) Math.ceil(src.height * ratio);
			
			return new Dimension(dw, dh);
		}
	};
	
	//** 주어진 픽셀수를 넘지 않도록 이미지를 리사이즈.  */
	//PixelCountLimit ("@")
}
