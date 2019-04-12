// #! /usr/bin/java11 --source 11
package org.codefx.caliz;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Caliz {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO check args and maybe handle options beyond file to compile
		String scriptPath = createScriptPath(args[0]);
		Process script = startProcess(scriptPath);
		script.waitFor();
		System.out.println("Wohoo! " + script.exitValue());
	}

	private static String createScriptPath(String arg) {
		String userDir = System.getProperty("user.dir");
		Path script = Paths.get(userDir).resolve(arg);
		return script.toAbsolutePath().toString();
	}

	private static Process startProcess(String scriptPath) throws IOException {
		// TODO make path to JVM executable configurable
		// TODO make source level configurable
		return new ProcessBuilder("/usr/bin/java11", "--source 11", scriptPath)
				.inheritIO()
				.start();
	}

}
