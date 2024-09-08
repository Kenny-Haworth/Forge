#!/bin/bash
set -eu # exit on error or undefined variable

# compile the project
javac -d classes $(find forge -name "*.java")

# package the jar
jar -cf bin/Forge.jar -C classes .

# cleanup
rm -rf classes
