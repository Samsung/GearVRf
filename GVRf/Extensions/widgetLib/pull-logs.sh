#! /usr/bin/env bash

# Our "includes"
source ./ask.sh

function usage() {
    echo "`basename ${0}` [options]"
    echo
    echo "  Pulls VRTop's persistent logs into a local directory (\"logs\" by default);"
    echo "  packages the logs into a zipfile (\"log.zip\" by default); and concatenates"
    echo "  the logs into a single file for grepping convenience (\"logs.log\" by default)."
    echo
    echo "  If the pull directory already exists, prompts to overwrite the directory unless"
    echo "  the force option (\`-f\`) is specified."
    echo
    echo "  Options:"
    echo "    -c           Clean up the pulled logs after creating the zip and concatenated"
    echo "                 log file."
    echo "    -d <DIR>     Directory to pull logs to (\"logs\" by default). See -n."
    echo "    -f           Force overwrite of pull directory if it already exists."
    echo "    -l <FILE>    Name for the concatenated log file (\"logs.log\" by default)"
    echo "                 See -n."
    echo "    -n <NAME>    Name to use for the pull directory, zip file, and concatenated"
    echo "                 log file.  Equivalent to \`-d NAME -l NAME -z NAME\`. Can be"
    echo "                 overridden with later uses of -d, -l, and -z."
    echo "    -z <FILE>    Name for the zip file (\"logs.zip\") by default). See -n."
    echo "    -h           Print this helpful message"

    exit
}

checkdir() {
    local DIR=${1}
    if [[ -e ${DIR} ]] ; then
        if [[ ${FORCE} -eq 1 ]] ; then
            rm -rf ${DIR}
        else
            if ask "'${DIR}' already exists; remove it?" N ; then
                rm -rf ${DIR}
            else
                exit
            fi
        fi
    fi

    mkdir -p ${DIR}
}

CLEANUP=0
FORCE=0
DEF_NAME="logs"
DIR=$DEF_NAME
LOG=$DEF_NAME
ZIP=$DEF_NAME


while getopts ":cd:fl:n:z:h" OPTION
do
	case ${OPTION} in
	    c ) CLEANUP=1 ;;
		d ) DIR=${OPTARG} ;;
		f ) FORCE=1 ;;
		l ) LOG=${OPTARG} ;;
		n ) DIR=${OPTARG} ; LOG=${OPTARG} ; ZIP=${OPTARG} ;;
		z ) ZIP=${OPTARG} ;;
		h ) usage ;;
		* ) echo "Bad option: ${OPTARG}" ; usage;;
	esac
done

shift $(($OPTIND - 1))

checkdir ${DIR}

pushd . > /dev/null

cd ${DIR}
echo "In `pwd`"
#adb pull /sdcard/vrtop .
adb pull /storage/emulated/0/Android/data/org.gearvrf.gvrf_launcher/files/Documents/vrtop ${DEF_NAME}

cd ${DEF_NAME}
zip ${ZIP} logFile*
cat logFile* > ${LOG}.log

for f in fullLogFile* ; do
    if [ -e "$f" ] ; then
        zip ${ZIP}-full fullLogFile*
        cat fullLogFile* > ${LOG}.full.log
    fi
    break
done

if [[ ${CLEANUP} -eq 1 ]] ; then
    rm logFile.*
    rm fullLogFile*
fi

popd > /dev/null

