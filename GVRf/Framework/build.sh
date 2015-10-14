#! /usr/bin/env bash

BUILDFILE=build.xml
TARGET=

function usage() {
	echo "build.sh [target]"
	echo
	echo "Target may be one of clean, debug, or release.  Defaults to debug."
	exit
}

function build_src() {
	android update lib-project -p .
	sed -i 's:${sdk.dir}/tools/ant/build.xml:./android_patched_'$BUILDFILE':' $BUILDFILE
	ndk-build -j8
	ant $TARGET
}

function clean() {
	echo "Workspace Cleaned"
	ndk-build clean
	ant clean
	rm $BUILDFILE 2>/dev/null
	rm local.properties 2>/dev/null
}

case $1 in
	"" ) TARGET="debug" ;;
	"clean" | "debug" | "release") TARGET=$1 ;;
	* ) usage ;;
esac

case $TARGET in
    "clean" ) clean ;;
    * )
        build_src
    ;;
esac
