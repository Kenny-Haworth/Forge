#!/bin/bash
#
# Compiles the Forge into a single jar file.
#
# Usage: ./build.sh
#
# Output: Forge.jar (in bin)

set -eu # add x for debug

# go to where this script lives
cd $(dirname $0)

# compile all files
javac -d classes $(find forge -name "*.java")

# create the jar
jar -cf bin/Forge.jar -C classes .

# cleanup
rm -rf classes
echo "Forge compiled successfully"
