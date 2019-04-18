#! /bin/bash
set -e

rm -rf demo/tmp
mkdir demo/tmp
rm -f work/caliz

javac -d demo/tmp src/org/codefx/caliz/Caliz.java
native-image -cp demo/tmp org.codefx.caliz.Caliz work/caliz

rm -rf demo/tmp/org
