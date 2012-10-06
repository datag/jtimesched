#!/bin/sh

if [ -n "$JAVA_HOME" ]; then
	JAVA=$JAVA_HOME/bin/java	# use $JAVA_HOME if set
else
	JAVA=java		# java binary within the current PATH
fi

if ! $JAVA -jar jTimeSched.jar "$@"; then
	echo "Error executing Java binary." >&2
	exit 1
fi

