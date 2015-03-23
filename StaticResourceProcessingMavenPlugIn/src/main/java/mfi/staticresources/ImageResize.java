package mfi.staticresources;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageResize {

	public BufferedImage resize(File source, int size) {

		BufferedImage resizedImage;
		try {
			BufferedImage originalImage = ImageIO.read(source);
			int type = BufferedImage.SCALE_SMOOTH;
			resizedImage = new BufferedImage(size, size, type);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(originalImage, 0, 0, size, size, null);
			g.dispose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return resizedImage;
	}
}