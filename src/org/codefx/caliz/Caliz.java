// #! /usr/bin/java11 --source 11
package org.codefx.caliz;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Caliz {

	private static final Path IMAGES_DIR = Paths.get("images").toAbsolutePath();
	private static final Path BUILD_DIR = Paths.get("build").toAbsolutePath();

	public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
		// TODO check args and maybe handle options beyond file to compile
		// TODO handle exceptions and turn them into informative error messages
		Path script = createPathToScript(args[0]);
		String checksum = createChecksum(script);
		if (existsNativeImage(checksum))
			executeImageFromCache(checksum);
		else
			executeScriptAndCreateImage(script, checksum);
	}

	/*
	 * IMAGE CACHE
	 */
	private static boolean existsNativeImage(String checksum) throws IOException, NoSuchAlgorithmException {
		Files.createDirectories(IMAGES_DIR);
		return Files.exists(IMAGES_DIR.resolve(checksum));
	}

	private static String createChecksum(Path file) throws IOException, NoSuchAlgorithmException {
		// it may seem desirable to never parse the file twice (once for checksum, once to create
		// a compilable source file), but:
		//  * creating the checksum is performance critical - I don't want to do unnecessary work
		//    (caveat: I never measured whether creating the compilable source on the same run
		//     negatively impacts performance)
		//  * creating a compilable script is on the very-slow path of creating a native image anyway,
		//    so saving a few milliseconds doesn't make a difference there
		byte[] bytes = createDigest(file);
		// this bytes[] has bytes in decimal format; convert it to hexadecimal format
		return convertDecimalToHexadecimalBytes(bytes);
	}

	private static byte[] createDigest(Path file) throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		byte[] byteArray = new byte[8192];
		int bytesCount;
		try (FileInputStream fis = new FileInputStream(file.toFile())) {
			while ((bytesCount = fis.read(byteArray)) != -1)
				digest.update(byteArray, 0, bytesCount);
		}
		return digest.digest();
	}

	private static String convertDecimalToHexadecimalBytes(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte aByte : bytes)
			sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
		return sb.toString();
	}

	private static void executeImageFromCache(String checksum) throws IOException, InterruptedException {
		Path imagePath = IMAGES_DIR.resolve(checksum);
		prepareNativeExecutionOfImage(imagePath)
				.start()
				.waitFor();
	}

	private static void executeScriptAndCreateImage(Path script, String checksum)
			throws IOException, InterruptedException {
		Process runningScript = prepareScriptExecution(script).start();
		createNativeImageFromScript(script, checksum);
		runningScript.waitFor();
	}

	/*
	 * NATIVE IMAGES
	 */

	private static void createNativeImageFromScript(Path script, String checksum) throws IOException, InterruptedException {
		Path compilableScript = createCompilableScript(script);
		Path imageInBuild = createNativeImage(compilableScript);
		Path imageInCache = IMAGES_DIR.resolve(checksum);
		// TODO use REPLACE_EXISTING?
		Files.move(imageInBuild, imageInCache, StandardCopyOption.ATOMIC_MOVE);
	}

	private static Path createCompilableScript(Path script) throws IOException {
		List<String> sourceLines = Files.readAllLines(script);
		sourceLines.set(0, "// " + sourceLines.get(0));
		// TODO extract class name from source file to allow arbitrary names
		Path sourceFile = BUILD_DIR.resolve("Script.java");
		Files.write(sourceFile, sourceLines, StandardOpenOption.CREATE);
		return sourceFile;
	}

	private static Path createNativeImage(Path compilableScript) throws IOException, InterruptedException {
		Path bytecode = compileToBytecode(compilableScript);
		return compileToNativeImage(bytecode);
	}

	private static Path compileToBytecode(Path scriptPath) throws IOException, InterruptedException {
		Process javac = new ProcessBuilder(
				"javac",
				"-d", BUILD_DIR.toString(),
				scriptPath.toString())
				.start();
		javac.waitFor();
		// TODO: print output (standard or error?) if compilation failed (~> error handling)
		// TODO find a better way to determine class file name (this breaks if script is in a package)
		return BUILD_DIR.resolve(scriptPath.getFileName().toString().replace(".java", ".class"));
	}

	private static Path compileToNativeImage(Path bytecode) throws IOException, InterruptedException {
		Path image = BUILD_DIR.resolve("script-image");
		// TODO make path to `native image` configurable
		// TODO consider creating the image in a sibling process that does not get killed (?)
		Process graalAot = new ProcessBuilder(
				"/usr/bin/native-image",
				"-cp", bytecode.getParent().toString(),
				"Script",
				image.toString())
				.start();
		graalAot.waitFor();
		// TODO: print output (standard or error?) if compilation failed (~> error handling)
		return image;
	}

	private static String captureStandardOutput(Process process) throws IOException {
		return captureOutput(process.getInputStream());
	}

	private static String captureErrorOutput(Process process) throws IOException {
		return captureOutput(process.getErrorStream());
	}

	private static String captureOutput(InputStream output) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(output));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		return builder.toString();
	}

	private static ProcessBuilder prepareNativeExecutionOfImage(Path scriptImage) {
		return new ProcessBuilder(scriptImage.toString())
				.inheritIO();
	}

	/*
	 * USE JVM
	 */

	private static ProcessBuilder prepareScriptExecution(Path scriptPath) throws IOException {
		// TODO make path to JVM executable configurable
		// TODO make source level configurable
		return new ProcessBuilder("/usr/bin/java11", "--source 11", scriptPath.toString())
				.inheritIO();
	}

	/*
	 * HELPER
	 */

	private static Path createPathToScript(String arg) {
		String workingDir = System.getProperty("user.dir");
		Path script = Paths.get(workingDir).resolve(arg);
		return script.toAbsolutePath();
	}

}
