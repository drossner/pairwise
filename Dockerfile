FROM adoptopenjdk:11-jre-hotspot
COPY ./build/libs/pairwise-1.0-SNAPSHOT-all.jar /
#COPY ./build/db-create-all.sql /docker-entrypoint-initdb.d/
WORKDIR /
ENTRYPOINT ["java", "-cp", "pairwise-1.0-SNAPSHOT-all.jar", "de.iisys.va.pairwise.StartupKt"]
