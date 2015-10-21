#!/bin/sh
symlink=`find "$0" -printf "%l"`
cd "`dirname "${symlink:-$0}"`"
"${JAVA_HOME:-/usr}"/bin/java -Djava.library.path=lib -jar mudmap2.jar
