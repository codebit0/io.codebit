package io.codebit.support.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import io.codebit.support.image.options.IWriteOption;

public class Test
{

	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub
		try
		{
			URI uri = URI.create("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Felis_silvestris_silvestris_small_gradual_decrease_of_quality.png/240px-Felis_silvestris_silvestris_small_gradual_decrease_of_quality.png");
//			File file = new File("c:/Temp/5.gif");
//			File out = new File("c:/Temp/5-5.gif");
//			
//			File file = new File("c:/Temp/2.jpg");
//			File out = new File("c:/Temp/2-5.jpg");
//			File file = new File("c:/Temp/q.jpg");
			File file = new File("c:/Temp/3.gif");
			File out = new File("c:/Temp/3-out.gif");
//			GifImage gif = GifDecoder.read(new FileInputStream(file));
//			final int frameCount = gif.getFrameCount();
//		    for (int i = 0; i < frameCount; i++) {
//		        final BufferedImage img = gif.getFrame(i);
//		        final int delay = gif.getDelay(i);
//		        ImageIO.write(img, "png", new File("c:/Temp/" + "frame_" + i + ".png"));
//		    }
			BitmapImage bitmap = BitmapImage.fromStream(new FileInputStream(file));
//			BitmapImage bitmap = BitmapImage.fromURI(uri);
//			bitmap.addText("qqqqq", 10, 10);
//			bitmap.setBackground(new Dimension(2000,2000), new Color(255,255,255,0));
			//bitmap.setBackgroundColor(new Color(255,10,30, 200));
			//bitmap.addPoint(10, 10, new Color(10, 10 , 10));
			//bitmap.trim(255, 255, 255);
			//bitmap.rotate(RotateFlipType.Rotate270FlipNone);
//			bitmap.resize(new Dimension(300, 300), RatioOption.AspectRatio);
//			bitmap.resize(new Dimension(300, 300), CropOption.CENTER);
//			bitmap.resize(new Dimension(300, 300));
			//bitmap.crop(0, 0, 300, 300);
			//bitmap.resize(new Dimension(300, 300), CropOption.CENTER);
			IWriteOption writeOption = null;
			if(bitmap instanceof Jpeg)
			{
				writeOption = new Jpeg.WriteOption();
				((Jpeg.WriteOption)writeOption).setQuality(0.90f);
			}else
			{
				writeOption = new Gif.WriteOption();
//				writeOption.ignoreMetadata(true);
			}
			bitmap.resize(200, 100);
//			bitmap.rotate(90);
			bitmap.saveAs(out, writeOption);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
