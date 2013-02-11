#!/bin/bash

if [[ -n "$JAVA_HOME" ]]; then
	JAVA="$JAVA_HOME/bin/java"	# use $JAVA_HOME if set
else
	JAVA=java	# java binary within the current PATH
fi

# change into launcher directory
cd "$(dirname "$(readlink -f "$BASH_SOURCE")")"

if ! $JAVA -jar jTimeSched.jar "$@"; then
	echo "Error executing Java binary." >&2
	exit 1
fi
