package mfi.staticresources;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;

public class ProcessResources {

	public String processCSS(String content) {

		content = base64svg(content);
		content = minifyCSS(content);

		return content;
	}

	public String processJS(String content) {

		return content;
	}

	private String minifyCSS(String content) {

		content = deleteBetween(content, "/*", "*/");
		content = replace(content, "\t", "");
		content = replace(content, "\r", "");
		content = replace(content, "\n\n", "\n");
		content = replace(content, " \n", "\n");
		content = replace(content, "\n ", "\n");
		content = replace(content, " {", "{");
		content = replace(content, ": ", ":");
		content = replace(content, " :", ":");
		content = replace(content, ",\n", ",");
		content = replace(content, ", ", ",");
		content = replace(content, "{\n", "{");
		content = replace(content, ";\n", ";");
		content = replace(content, "\n}", "}");
		content = replace(content, ";}", "}");
		// hacks  
		content = StringUtils.replace(content, "@charset \"UTF-8\";", "@charset \"UTF-8\";\n");
		
		return content;
	}

	private String replace(String content, String search, String replacement) {

		String compare;
		do {
			compare = content;
			content = StringUtils.replace(content, search, replacement);
		} while (!compare.equals(content));

		return content;
	}

	private String base64svg(String content) {

		content = replace(content, "image/svg+xml;charset=utf8,", "image/svg+xml;charset=utf8;base64,");
		try {
			boolean again = true;
			while (again) {
				String svg = StringUtils.substringBetween(content, "<svg", "</svg>");
				if (StringUtils.isBlank(svg)) {
					again = false;
				} else {
					svg = "<svg" + svg + "</svg>";
					byte[] svgBytes = svg.getBytes("UTF-8");
					String substringase64 = new String(Base64.encodeBase64(svgBytes), "UTF-8");
					content = StringUtils.replace(content, svg, substringase64);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return content;
	}

	private String deleteBetween(String content, String start, String end) {

		try {
			boolean again = true;
			while (again) {
				String sub = StringUtils.substringBetween(content, start, end);
				if (StringUtils.isBlank(sub)) {
					again = false;
				} else {
					sub = start + sub + end;
					content = StringUtils.replace(content, sub, "");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return content;
	}

	public static void main(String[] args) throws Exception {
		String string = FileUtils.fileRead("/Users/mfi/Documents/Programmierung/git/FileJuggler/FileJuggler/WebContent/style.css");
		string = (new ProcessResources()).processCSS(string);
		System.out.println(string);
	}

}
