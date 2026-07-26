FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y bash openjdk-11-jdk maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mvn clean install

RUN rm -f target/original*.jar

RUN mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

CMD ["java", "-jar", "target/app.jar"]
