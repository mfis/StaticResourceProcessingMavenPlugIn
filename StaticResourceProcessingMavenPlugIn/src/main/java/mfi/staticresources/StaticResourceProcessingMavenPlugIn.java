package mfi.staticresources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import net.sf.image4j.codec.ico.ICOEncoder;

@Mojo(name = "StaticResourceProcessingMavenPlugIn")
public class StaticResourceProcessingMavenPlugIn extends AbstractMojo {

	@Parameter(property = "StaticResourceProcessingMavenPlugIn.webContentSourceDir")
	private File webContentSourceDir;

	@Parameter(property = "StaticResourceProcessingMavenPlugIn.webContentDestDir")
	private File webContentDestDir;

	@Parameter(property = "StaticResourceProcessingMavenPlugIn.webContentTempDir")
	private File webContentTempDir;

	@Parameter(property = "StaticResourceProcessingMavenPlugIn.versionsMapFile")
	private File versionsMapFile;

	public void execute() throws MojoExecutionException {

		checkParameters();
		clearDestFiles();
		generateIcons();
		prepareResources();
		minifyCssAndJs();
		versioning();
		clearTempDir();

	}

	private void generateIcons() {

		// generate touch-icons directly in sourceDir, becouse some
		// browsers will look there
		File[] resources = webContentSourceDir.listFiles();
		for (File resource : resources) {
			if (resource.getName().equalsIgnoreCase("icon.png")) {
				generateFavIcons(resource);
				generateTouchIcons(resource);
				break;
			}
		}
	}

	private void generateFavIcons(File resource) {

		// generate favicon directly in sourceDir, becouse some
		// browsers will look there
		try {
			BufferedImage resizedImage = (new ImageResize()).resize(resource, 32);
			File dest = new File(resource.getParentFile().getAbsolutePath() + "/favicon.ico");
			// doc: https://github.com/imcdonagh/image4j 0.7.1
			ICOEncoder.write(resizedImage, dest);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void generateTouchIcons(File resource) {

		int[] touchIconSizes = new int[] { 57, 72, 76, 114, 120, 144, 152 };
		List<ImageResizeThread> threads = new LinkedList<ImageResizeThread>();

		for (int size : touchIconSizes) {
			File dest = new File(resource.getParentFile().getAbsolutePath() + "/apple-touch-icon-" + size + "x" + size + "-precomposed.png");
			ImageResizeThread t = new ImageResizeThread(resource, dest, size);
			threads.add(t);
			t.start();
		}
		for (ImageResizeThread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void prepareResources() {

		// copy resources from sourceDir to tempDir
		File[] resources = webContentSourceDir.listFiles();
		for (File resource : resources) {
			if (resource.isFile() && !resource.isHidden()) {
				String name = resource.getName();
				String hash = hash(resource);
				getLog().info("Found " + name + " - " + hash);
				File sourcefile = new File(webContentSourceDir.getAbsolutePath() + "/" + name);
				File destfile = new File(webContentTempDir.getAbsolutePath() + "/" + name);
				copyFile(sourcefile, destfile);
			}
		}
	}

	private void minifyCssAndJs() {

		// minify css and js
		File[] resources = webContentTempDir.listFiles();
		for (File resource : resources) {
			if (resource.isFile() && !resource.isHidden()) {
				String name = resource.getName();
				try {
					if (name.toLowerCase().endsWith(".js")) {
						String opt = (new ProcessResources()).processJS(FileUtils.fileRead(resource, "UTF-8"));
						FileUtils.fileWrite(resource.getAbsolutePath(), "UTF-8", opt);
					}
					if (name.toLowerCase().endsWith(".css")) {
						String opt = (new ProcessResources()).processCSS(FileUtils.fileRead(resource, "UTF-8"));
						FileUtils.fileWrite(resource.getAbsolutePath(), "UTF-8", opt);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void versioning() {

		boolean doHashVersions = versionsMapFile != null;

		StringBuilder propstring = new StringBuilder();

		File[] resources = webContentTempDir.listFiles();
		for (File resource : resources) {
			if (resource.isFile() && !resource.isHidden()) {
				String name = resource.getName();
				String hash = "";
				if (doHashVersions) {
					hash = hash(resource);
					propstring.append(resource.getName() + " = " + hash + "\n");
				}
				File sourcefile = new File(webContentTempDir.getAbsolutePath() + "/" + name);
				File destfile = new File(webContentDestDir.getAbsolutePath() + "/" + hash + (doHashVersions ? "_" : "") + name);
				copyFile(sourcefile, destfile);
			}
		}

		try {
			if (doHashVersions) {
				PrintWriter propout = new PrintWriter(versionsMapFile.getAbsolutePath());
				propout.print(propstring.toString().trim());
				propout.flush();
				propout.close();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private String hash(File file) {

		Path path = Paths.get(file.getAbsolutePath());
		byte[] data;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		md.reset();
		md.update(data);
		byte[] digest = md.digest();

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < digest.length; ++i) {
			sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1, 3));
		}

		return sb.toString();
	}

	private static void copyFile(File sourceFile, File destFile) {

		try {
			if (!destFile.exists()) {
				destFile.createNewFile();
			}

			FileChannel source = null;
			FileChannel destination = null;

			try {
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());
			} finally {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void clearTempDir() {

		// clear the temp dir
		File[] resources = webContentTempDir.listFiles();
		for (File resource : resources) {
			if (resource.isFile() && !resource.isHidden()) {
				resource.delete();
			}
		}
	}

	private void clearDestFiles() {

		// clear the temp dir
		File[] resources = webContentDestDir.listFiles();
		for (File resource : resources) {
			if (resource.isFile() && !resource.isHidden()) {
				resource.delete();
			}
		}
	}

	private void checkParameters() {

		if (webContentSourceDir.exists()) {
			getLog().info("webContentSourceDir = " + webContentSourceDir.getAbsolutePath());
		} else {
			getLog().error("webContentSourceDir = " + webContentSourceDir.getAbsolutePath() + " does not exist!");
			throw new IllegalArgumentException("webContentSourceDir = " + webContentSourceDir.getAbsolutePath() + " does not exist!");
		}

		if (webContentDestDir.exists()) {
			getLog().info("webContentDestDir = " + webContentDestDir.getAbsolutePath());
		} else {
			if (webContentDestDir.getParentFile() != null && webContentDestDir.getParentFile().exists() && webContentDestDir.getParentFile().isDirectory()) {
				FileUtils.mkdir(webContentDestDir.getAbsolutePath());
			}
			if (!webContentDestDir.exists()) {
				getLog().error("webContentDestDir = " + webContentDestDir.getAbsolutePath() + " does not exist!");
				throw new IllegalArgumentException("webContentDestDir = " + webContentDestDir.getAbsolutePath() + " does not exist!");
			}
		}

		if (webContentTempDir.exists()) {
			getLog().info("webContentTempDir = " + webContentTempDir.getAbsolutePath());
		} else {
			if (webContentTempDir.getParentFile() != null && webContentTempDir.getParentFile().exists() && webContentTempDir.getParentFile().isDirectory()) {
				FileUtils.mkdir(webContentTempDir.getAbsolutePath());
			}
			if (!webContentTempDir.exists()) {
				getLog().error("webContentTempDir = " + webContentTempDir.getAbsolutePath() + " does not exist!");
				throw new IllegalArgumentException("webContentTempDir = " + webContentTempDir.getAbsolutePath() + " does not exist!");
			}
		}

		if (versionsMapFile != null) {
			if (versionsMapFile.getParentFile() != null && versionsMapFile.getParentFile().exists()) {
				getLog().info("versionsMapFile = " + versionsMapFile.getAbsolutePath());
			} else {
				getLog().error("versionsMapFile parent = " + versionsMapFile.getParentFile() + " does not exist!");
				throw new IllegalArgumentException("versionsMapFile parent = " + versionsMapFile.getParentFile() + " does not exist!");
			}
		}
	}
}
