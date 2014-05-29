#!/bin/bash
#
# ref link <http://wiki.babel.baidu.com/twiki/bin/view/Com/Pmo/Scm/UserManual>

set -eu
Prompt(){
	echo "[$(date '+%D %H:%M:%S')] $@"
}

WORKSPACE=$(cd $(dirname $0); pwd)
Prompt "Work dir: $WORKSPACE"

cd $WORKSPACE


OUTPUT=$WORKSPACE/target/jmx2statsd-1.0-SNAPSHOT-all/output

Prompt "Clean and Package"
mvn clean package -Dmaven.test.skip=true || exit $?
#mvn clean package || exit $?
#mvn clean cobertura:cobertura || exit $?

Prompt "Package for scm"
rm -fr $WORKSPACE/output
mv $OUTPUT ./

Prompt "Generate verion and timestamp..."
echo $(date -d  today +%Y%m%d%H%M%S) > $WORKSPACE/output/version

exit $?
