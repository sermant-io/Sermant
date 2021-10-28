#!/bin/bash

if [[ ! -f ~/.m2/repository/sigar/sigar-native/1.0/sigar-native-1.0.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/sigar-native-1.0.jar -DgroupId=sigar -DartifactId=sigar-native -Dversion=1.0	-Dpackaging=jar -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/grinder/grinder-patch/3.9.1/grinder-patch-3.9.1.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/grinder-3.9.1-patch.jar -DgroupId=grinder -DartifactId=grinder-patch -Dversion=3.9.1-patch -Dpackaging=jar -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/org/ngrinder/universal-analytics-java/1.0/universal-analytics-java-1.0.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/universal-analytics-java-1.0.jar -DgroupId=org.ngrinder -DartifactId=universal-analytics-java -Dversion=1.0 -Dpackaging=jar -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/org/ngrinder/ngrinder-core/3.4.2/ngrinder-core-3.4.2.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/ngrinder-core-3.4.2.jar -DgroupId=org.ngrinder -DartifactId=ngrinder-core -Dversion=3.4.2 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/org/ngrinder/ngrinder-groovy/3.4.2/ngrinder-groovy-3.4.2.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/ngrinder-groovy-3.4.2.jar -DgroupId=org.ngrinder -DartifactId=ngrinder-groovy -Dversion=3.4.2 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/org/ngrinder/ngrinder-runtime/3.4.2/ngrinder-runtime-3.4.2.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/ngrinder-runtime-3.4.2.jar -DgroupId=org.ngrinder -DartifactId=ngrinder-runtime -Dversion=3.4.2 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/sonia/svnkit/svnkit/1.8.4-scm1/svnkit-1.8.4-scm1.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/svnkit-1.8.4-scm1.jar -DgroupId=sonia.svnkit -DartifactId=svnkit -Dversion=1.8.4-scm1 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/sonia/svnkit/svnkit-dav/1.8.4-scm1/svnkit-dav-1.8.4-scm1.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/svnkit-dav-1.8.4-scm1.jar -DgroupId=sonia.svnkit -DartifactId=svnkit-dav -Dversion=1.8.4-scm1 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
fi
