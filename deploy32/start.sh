#!/bin/sh

nohup java -jar -Dspring.config.location=./ \
   -Dlog.folder=./logs/ \
   -Dspring.config.name=application \
   -Dloader.path=./ \
   -Dwork.dir=./workdir/ \
   eclarity-client.jar > /dev/null 2>&1 &