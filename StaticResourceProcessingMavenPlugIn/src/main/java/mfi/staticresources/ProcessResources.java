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

		content = minifyJS(content);

		return content;
	}

	private String minifyCSS(String content) {

		content = deleteBetween(content, "/*", "*/", "license-attribution");

		content = replace(content, "\t", "");
		content = replace(content, "\r", "");
		content = replace(content, "\n\n", "\n");
		content = trimAround(content, "\n");
		content = trimAround(content, "{");
		content = trimAround(content, ":");
		content = replace(content, ",\n", ",");
		content = trimAround(content, ",");
		content = replace(content, "{\n", "{");
		content = replace(content, ";\n", ";");
		content = replace(content, "\n}", "}");
		content = replace(content, ";}", "}");
		// hacks
		content = StringUtils.replace(content, "@charset \"UTF-8\";", "@charset \"UTF-8\";\n");

		return content;
	}

	private String minifyJS(String content) {

		content = deleteBetween(content, "//", "\n", null);
		content = deleteBetween(content, "/*", "*/", null);

		content = replace(content, "\t", "");
		content = replace(content, "\r", "");
		content = replace(content, "\n\n", "\n");
		content = trimAround(content, "\n");
		content = replace(content, "{\n", "{");
		content = replace(content, "\n}", "}");
		content = replace(content, ";\n", ";");
		content = replace(content, "(\n", "(");
		content = replace(content, "\n(", "(");
		content = replace(content, ")\n", ")");
		content = replace(content, "\n)", ")");
		content = replace(content, ",\n", ",");
		content = replace(content, "\n.", ".");
		content = trimAround(content, "=");
		content = trimAround(content, ">");
		content = trimAround(content, "<");
		content = trimAround(content, "+");
		content = trimAround(content, "-");
		content = trimAround(content, "}");
		content = trimAround(content, "{");
		content = trimAround(content, "(");
		content = trimAround(content, ")");
		content = trimAround(content, ",");

		return content;
	}

	private String trimAround(String content, String search) {

		content = replace(content, " " + search, search);
		content = replace(content, search + " ", search);

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

	private String deleteBetween(String content, String start, String end, String exclusionContent) {

		try {
			String[] subs = StringUtils.substringsBetween(content, start, end);
			if (subs == null || subs.length == 0) {
				// noop
			} else {
				for (String sub : subs) {
					if (StringUtils.isNotBlank(exclusionContent) && StringUtils.containsIgnoreCase(sub, exclusionContent)) {
						// noop
					} else {
						sub = start + sub + end;
						content = StringUtils.replace(content, sub, "");
					}
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
