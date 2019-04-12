// #! /home/nipa/code/Caliz/build/caliz
import java.io.IOException;

public class HelloScripts {

	public static void main(String[] args) throws IOException {
		System.out.println("Hello, scripts!");
		new ProcessBuilder("touch", "/home/nipa/code/Caliz/build/script-ran.txt").start();
		System.exit(42);
	}

}
