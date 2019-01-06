package io.codebit.support.image;

import org.w3c.dom.*;

import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

public class ImageMetadataUtil
{
/*	public static void main(String[] args) throws IOException
	{
		int length = args.length;
		for (int i = 0; i < length; i++)
			Metadata.displayMetadata(args[i]);
	}
*/
	public static String toXML(String fileName) throws IOException
	{
		File file = new File(fileName);
		ImageInputStream iis = ImageIO.createImageInputStream(file);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

		if (readers.hasNext())
		{
			// pick the first available ImageReader
			ImageReader reader = readers.next();

			// attach source to the reader
			reader.setInput(iis, true);

			// read metadata of first image
			IIOMetadata metadata = reader.getImageMetadata(0);

			String[] names = metadata.getMetadataFormatNames();
			int length = names.length;
			String rtn = "";
			for (int i = 0; i < length; i++)
			{
				rtn += toXML(metadata.getAsTree(names[i]));
			}
			return rtn;
		}
		return null;
	}

	public static String toXML(Node root)
	{
		return displayMetadata(root, 0).toString();
	}

	private static void indent(int level, StringBuilder builder)
	{
		for (int i = 0; i < level; i++)
			builder.append("    ");
	}

	private static StringBuilder displayMetadata(Node node, int level)
	{
		StringBuilder builder = new StringBuilder();

		indent(level, builder);
		builder.append("<" + node.getNodeName());
		NamedNodeMap map = node.getAttributes();
		if (map != null)
		{
			// print attribute values
			int length = map.getLength();
			for (int i = 0; i < length; i++)
			{
				Node attr = map.item(i);
				builder.append(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
			}
		}
		
		Node child = node.getFirstChild();
		if (child == null)
		{
			// no children, so close element and return
			builder.append("/>\n");
			return builder;
		}
		
		// children, so close current tag
		builder.append(">");
		while (child != null)
		{
			// print children recursively
			builder.append("\n");
			builder.append(displayMetadata(child, level + 1));
			child = child.getNextSibling();
		}
		
		// String textContent = node.getTextContent();
		// System.out.println(textContent);
		// print close tag of element
		indent(level, builder);
		builder.append("</" + node.getNodeName() + ">\n");
		return builder;
	}
}
