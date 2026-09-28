
@echo off

call mvn install:install-file -Dfile=.\dcm4che-core-5.29.2.jar -DgroupId=org.dcm4che  -DartifactId=dcm4che-core  -Dversion=5.29.2 -Dpackaging=jar

call mvn install:install-file -Dfile=.\dcm4che-deident-5.29.2.jar -DgroupId=org.dcm4che  -DartifactId=dcm4che-deident  -Dversion=5.29.2 -Dpackaging=jar

call mvn install:install-file -Dfile=.\dcm4che-dcmr-5.29.2.jar -DgroupId=org.dcm4che  -DartifactId=dcm4che-dcmr  -Dversion=5.29.2 -Dpackaging=jar