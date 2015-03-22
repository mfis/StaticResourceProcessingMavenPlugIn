package mfi.staticresources;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "StaticResourceProcessingMavenPlugIn")
public class StaticResourceProcessingMavenPlugIn extends AbstractMojo {

	@Parameter(property = "StaticResourceProcessingMavenPlugIn.webContentSourceDir")
	private File webContentSourceDir;

	@Parameter(property = "StaticResourceProcessingMavenPlugIn.webContentDestDir")
	private File webContentDestDir;

	@Parameter(property = "StaticResourceProcessingMavenPlugIn.versionsMapFile")
	private File versionsMapFile;

	public void execute() throws MojoExecutionException {

		checkParameters();
		
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
			getLog().error("webContentDestDir = " + webContentDestDir.getAbsolutePath() + " does not exist!");
			throw new IllegalArgumentException("webContentDestDir = " + webContentDestDir.getAbsolutePath() + " does not exist!");
		}

		if (versionsMapFile.getParentFile() != null && versionsMapFile.getParentFile().exists()) {
			getLog().info("versionsMapFile = " + versionsMapFile.getAbsolutePath());
		} else {
			getLog().error("versionsMapFile parent = " + versionsMapFile.getParentFile() + " does not exist!");
			throw new IllegalArgumentException("versionsMapFile parent = " + versionsMapFile.getParentFile() + " does not exist!");
		}
	}
}
