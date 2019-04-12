// #! /usr/bin/java11 --source 11
package org.codefx.caliz;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Caliz {

	private static final Path BUILD_DIR = Paths.get("build").toAbsolutePath();

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO check args and maybe handle options beyond file to compile
		// TODO handle exceptions and turn them into informative error messages
		// TODO figure out how to handle output streams of various processes
		Process script = executeAfterAot(args[0]);
		script.waitFor();
		System.out.println("Wohoo! " + script.exitValue());
	}

	private static Process executeAfterAot(String scriptFile) throws IOException, InterruptedException {
		Path scriptImage = createNativeImage(scriptFile);
		return executeImage(scriptImage);
	}

	private static Path createNativeImage(String scriptFile) throws IOException, InterruptedException {
		Path scriptPath = createCompilableScript(scriptFile);
		Path bytecode = compileToBytecode(scriptPath);
		return compileToNativeImage(bytecode);
	}

	private static Path createCompilableScript(String scriptFile) throws IOException {
		List<String> sourceLines = Files.readAllLines(createScriptPath(scriptFile));
		sourceLines.set(0, "// " + sourceLines.get(0));
		// TODO extract class name from source file to allow arbitrary names
		Path sourceFile = BUILD_DIR.resolve("Script.java");
		Files.write(sourceFile, sourceLines, StandardOpenOption.CREATE);
		return sourceFile;
	}

	private static Path compileToBytecode(Path scriptPath) {
		// TODO check whether Substrate VM contains a compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		// TODO evaluate return value (errors!)
		compiler.run(
				null,
				System.out,
				System.err,
				"-d", BUILD_DIR.toString(),
				// TODO set bootstrap class path (ugh)
				"-source", "8",
				"-target", "8",
				scriptPath.toString());
		// TODO find a better way to determine class file name (this breaks if script is in a package)
		return BUILD_DIR.resolve(scriptPath.getFileName().toString().replace(".java", ".class"));
	}

	private static Path compileToNativeImage(Path bytecode) throws IOException, InterruptedException {
		Path image = BUILD_DIR.resolve("script-image");
		// TODO make path to `native image` configurable
		new ProcessBuilder(
				"/usr/bin/native-image",
				"-cp", bytecode.getParent().toString(),
				"Script",
				image.toString())
				.inheritIO()
				.start()
				.waitFor();
		return image;
	}

	private static Process executeImage(Path scriptImage) throws IOException {
		return new ProcessBuilder(scriptImage.toString())
				.inheritIO()
				.start();
	}

	/*
	 * USE JVM
	 */

	private static Process executeWithJvm(String scriptFile) throws IOException {
		Path scriptPath = createScriptPath(scriptFile);
		return startProcess(scriptPath);
	}

	private static Process startProcess(Path scriptPath) throws IOException {
		// TODO make path to JVM executable configurable
		// TODO make source level configurable
		return new ProcessBuilder("/usr/bin/java11", "--source 11", scriptPath.toString())
				.inheritIO()
				.start();
	}

	/*
	 * HELPER
	 */

	private static Path createScriptPath(String arg) {
		String workingDir = System.getProperty("user.dir");
		Path script = Paths.get(workingDir).resolve(arg);
		return script.toAbsolutePath();
	}

}
