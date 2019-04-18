#! /bin/bash
set -e

rm -rf demo/tmp
mkdir demo/tmp
rm -f work/caliz

mode=$1

if [ "$mode" != "--native" ]
then
	sed '0,/\/\//{s/\/\/ //}' src/org/codefx/caliz/Caliz.java > work/caliz
	chmod +x work/caliz
fi

sed '0,/\/\//{s/\/\/ //}' demo/src/HelloScripts.java > demo/tmp/hello-scripts
chmod +x demo/tmp/hello-scripts
demo/tmp/hello-scripts
