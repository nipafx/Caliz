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

## Good to know

### Script class names

While the script file can have any name you want, the class it contains must be called `Script`.

### Java 8

Unfortunately, Graal currently only supports Java 8 (i.e. bytecode level 52), so neither the scripts nor `Caliz.java` can use newer language features or APIs.
Sad.
😭
