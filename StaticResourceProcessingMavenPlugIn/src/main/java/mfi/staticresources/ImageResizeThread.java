package mfi.staticresources;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImageResizeThread extends Thread {

	int size;
	File source;
	File dest;

	public ImageResizeThread() {
		super();
	}

	public ImageResizeThread(File source, File dest, int size) {
		super();
		this.source = source;
		this.dest = dest;
		this.size = size;
	}

	public void run() {

		try {
			resizeImage();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void resizeImage() throws Exception {

		ImageResize imageResize = new ImageResize();
		BufferedImage resizedImage = imageResize.resize(source, size);
		ImageIO.write(resizedImage, "png", dest);
	}

}
