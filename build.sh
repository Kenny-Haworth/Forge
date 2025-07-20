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

# build the jar file list
classpath="."
for jar_file in $(find lib -type f -name "*.jar"); do
    classpath+=";$jar_file"
done

# compile all files
javac -Xlint:all,-serial \
      -d classes \
      -cp $classpath \
      $(find forge -type f -name "*.java")

# extract the content of all jar files
(
    cd classes
    for jar_file in $(find . -type f -name "*.jar"); do
        jar xf $jar_file
    done
)

# form a list of the class directories to place in the jar
class_dirs=""
for dir in $(cd classes && ls -d */ | cut -f1 -d'/'); do
    class_dirs+="-C classes $dir "
done

# create the jar
jar -cf bin/Forge.jar $class_dirs

# cleanup
rm -rf classes
echo "Forge compiled successfully 🛠️"
