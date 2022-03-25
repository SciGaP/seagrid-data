#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

. `dirname $0`/setenv.sh
cd $SEAGRID_WORKER_HOME

IS_DAEMON_MODE=false
SEAGRID_WORKER_COMMAND=""
STOP=false
FORCE=false

for var in "$@"
do
    case $var in
	start)
	    IS_DAEMON_MODE=true
            shift
        ;;
	stop)
	    STOP=true
	    SEAGRID_WORKER_COMMAND="$
	    SEAGRID_WORKER_COMMAND $var"
            shift
        ;;
        -h)
            echo "Usage: datacat-worker.sh [command-options]"
            echo "command options:"
	    echo "  start              Start server in daemon mode"
	    echo "  stop               Stop server."
	    echo "  -h                 Display this help and exit"
	        shift
            exit 0
        ;;
	*)
	    SEAGRID_WORKER_COMMAND="$SEAGRID_WORKER_COMMAND $var"
            shift
    esac
done

if $STOP;
then
	for f in `find . -name "*-start_*"`; do
		IFS='_' read -a f_split <<< "$f"
		echo "Found process file : $f"
		echo -n "    Sending kill signals to process ${f_split[1]}..."
		out=`kill -9 ${f_split[1]} 2>&1`
		if [ -z "$out" ]; then
		    echo "done"
		else
		    echo "failed (REASON: $out)"
		fi
		echo -n "    Removing process file..."
		out=`rm $f 2>&1`
		if [ -z "$out" ]; then
		    echo "done"
		else
		    echo "failed (REASON: $out)"
		fi
	done
else
	if $IS_DAEMON_MODE ; then
		echo "Starting SEAGRID listener in daemon mode..."
		cd "$SEAGRID_WORKER_HOME"/lib
		nohup $JAVA_HOME/bin/java -jar "$SEAGRID_WORKER_HOME"/lib/seagrid-scanner-0.1-SNAPSHOT.jar > ../seagrid-worker.out & echo $! > "../seagrid-worker-start_$!"
		cd ..
	else
        cd "$SEAGRID_WORKER_HOME"/lib
		$JAVA_HOME/bin/java -jar "$SEAGRID_WORKER_HOME"/lib/seagrid-scanner-0.1-SNAPSHOT.jar
		cd ..
	fi
fi