package mfi.staticresources;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageResize {

	public BufferedImage resize(File source, int size) {

		BufferedImage resizedImage;
		try {
			BufferedImage originalImage = ImageIO.read(source);
			Image toolkitImage = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
			int width = toolkitImage.getWidth(null);
			int height = toolkitImage.getHeight(null);
			resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
			Graphics g = resizedImage.getGraphics();
			g.drawImage(toolkitImage, 0, 0, null);
			g.dispose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return resizedImage;
	}
}