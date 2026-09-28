if [ "$1" == "c" ]; then
  mvn clean package -DskipTests=true -P dev
  cp target/eclarity-client.jar deploy/
fi

java -jar -Dspring.config.location=/home/cirakas/Demo/eclarity-client/deploy/ \
   -Dlog.folder=/tmp/ \
   -Dspring.config.name=application \
   -Dloader.path=/home/cirakas/Demo/eclarity-client/deploy/ \
   -Dwork.dir=/home/cirakas/.dicomuploader \
   deploy/eclarity-client.jar
