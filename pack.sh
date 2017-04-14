#!/bin/sh
# This is a helper script to pack all necessary files for release

TMPDIR=/tmp/mudmaprelease/
DOCSDIR=docs/
JARPREFIX=mudmap2-
JARSUFFIX=.jar
ZIPPREFIX=mudmap-
ZIPSUFFIX=.zip

if [ "$#" -eq 2 ]
then
	
	VERSION=$1
	JARFILE=$2

	if [ -e $2 ]
	then

		if [ -d $TMPDIR ]
		then
			rm -rf $TMPDIR
		fi

		mkdir $TMPDIR

		cp $JARFILE $TMPDIR$JARPREFIX$VERSION$JARSUFFIX

		#cp doc/README $TMPDIRREADME.txt
		#cp doc/CHANGELOG $TMPDIRCHANGELOG.txt
		
		sed "s/$/\r/" < ${DOCSDIR}README > ${TMPDIR}README.txt
		sed "s/$/\r/" < ${DOCSDIR}CHANGELOG > ${TMPDIR}CHANGELOG.txt

		zip -j $ZIPPREFIX$VERSION$ZIPSUFFIX ${TMPDIR}*

		rm -rf $TMPDIR

	else

		echo "File $2 does not exist"

	fi

else
	echo "This script requires two parameters"
	echo "./pack.sh <version> <jar file>"
	echo "eg. ./pack 2.4.0 target/mudmap.jar"
fi
