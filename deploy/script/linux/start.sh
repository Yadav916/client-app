#!/bin/bash

cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd
process_id=$(jps | grep eclarity-client.jar | awk '{print $1}')
printf "The process is %s\n" "$process_id"

if [[ -n "$process_id" ]]; then
   #kill -9 $process_id
   nohup java AlreadyRunning > /dev/null 2>&1 &
   exit 1
fi

java -jar -Dspring.config.location=./ \
   -Dlog.folder=./logs/ \
   -Dspring.config.name=application \
   -Dloader.path=./ \
   -Dwork.dir=./workdir/ \
   eclarity-client.jar
