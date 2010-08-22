#!/bin/bash

RELEASE_EXTRA=
if [[ -n $1 ]]; then
	RELEASE_EXTRA="-$1"
fi

CURDIR=$(pwd)
TMPDIR=$CURDIR/tmp

AUX_FILES="data ChangeLog.txt LICENSE.txt jTimeSched.png"
AUX_FILES="$AUX_FILES launcher/jTimeSched.sh launcher/jTimeSched.exe"
AUX_FILES="$AUX_FILES src"    	# add soures as well, as we are GPLv3


function dist_prepare()
{
	echo "Preparing..."
	
	# remove old working files
	rm -rf $TMPDIR 2>/dev/null
	mkdir -p $TMPDIR
	
	# copy all needed files
	for i in $AUX_FILES ; do
		cp -a -t $TMPDIR $i
	done
	
	# remove all subversion directories
	find $TMPDIR -name ".svn" -type d -exec rm -rf {} \;  2>/dev/null
	
	return 0
}

function dist_compile()
{
	echo "Compiling..."
	
	JAVAC_CLASSPATH=
	PROGRAM_MAIN=de/dominik_geyer/jtimesched/JTimeSchedApp.java

	# find external libraries and add them to classpath
	cd $CURDIR/src
	#for i in $(find ../lib -type f -iname '*.jar') ; do
	#  JAVAC_CLASSPATH=${JAVAC_CLASSPATH}:${i}
	#done
	

	mkdir $TMPDIR/bin
	javac -d $TMPDIR/bin -classpath ".:${JAVAC_CLASSPATH}" $PROGRAM_MAIN

	[[ $? -eq 0 ]] || return 1

	cd $CURDIR
}

function dist_jar()
{
	echo "Jar..."
	
	cd $TMPDIR
	
	# create the jar-package
	jar cfm jTimeSched.jar $CURDIR/Manifest.txt -C bin/ .
	
	# set the executable bits for the jar
	chmod a+x jTimeSched.jar
	
	# set the executable bits for the launcher script
	chmod a+x jTimeSched.sh
	
	cd $CURDIR
}

function dist_javadoc()
{
	echo "Java-Doc..."	
	
	cd $TMPDIR
	
	mkdir doc/api
	
	# generate api
	javadoc -quiet -d doc/api -sourcepath src -private -subpackages jTimeSched
	
	cd $CURDIR
}

function dist_zip()
{
	echo "Zip..."
	
	# create a zip-package
	cd $TMPDIR
	
	rm $CURDIR/jTimeSched${RELEASE_EXTRA}.zip 2>/dev/null
	zip -r -9 -q $CURDIR/jTimeSched${RELEASE_EXTRA}.zip . -x bin/\*
	
	cd $CURDIR
}

###############################################################################


# copy files and clean working directory
dist_prepare || { echo "Prepare error" ; exit 1; }

# compile the sources
dist_compile || { echo "Compile error" ; exit 1; }

# create a jar package
dist_jar || { echo "Jar error" ; exit 1; }

# generate the API documentation
#dist_javadoc || { echo "Java-Doc error" ; exit 1; }

# create a zip package
dist_zip || { echo "Zip error" ; exit 1; }

echo "Success."

