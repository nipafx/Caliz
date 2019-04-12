rm -rf build
mkdir build

sed '0,/\/\//{s/\/\/ //}' src/org/codefx/caliz/Caliz.java > build/caliz
chmod +x build/caliz

sed '0,/\/\//{s/\/\/ //}' demo/HelloScripts.java > build/hello-scripts
chmod +x build/hello-scripts
build/hello-scripts
