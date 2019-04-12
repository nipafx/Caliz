// #! /usr/bin/java11 --source 11
package org.codefx.caliz;

import java.util.Arrays;

public class Caliz {

	public static void main(String[] args) {
		Arrays.stream(args).forEach(System.out::println);
	}

}
