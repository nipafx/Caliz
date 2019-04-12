# Caliz - A Wrapper For Graal AOT

Wouldn't it be great to write scripts in Java?
(Shush, yes it would be!)

Three problems:

 1. Java is verbose
 2. compile ~> execute
 3. slow JVM launch

Three fixes:

 1. Java 12
 2. single-source file
    execution (since 11)
 3. Graal AOT compilation

I want to combine these into a wrapper around Graal AOT-compiled scripts.
