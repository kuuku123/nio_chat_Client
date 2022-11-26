FROM adoptopenjdk/openjdk11:latest
VOLUME /tmp
COPY build/libs/Client-1.0-SNAPSHOT.jar Client.jar
COPY 1.jpg 1.jpg
COPY 2.txt 2.txt
ENTRYPOINT ["java","-jar","Client.jar"]
