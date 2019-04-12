rm -rf build
mkdir build

sed '0,/\/\//{s/\/\/ //}' demo/HelloScripts.java > build/hello-scripts
chmod +x build/hello-scripts
build/hello-scripts
