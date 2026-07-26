FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y bash openjdk-11-jdk maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mvn clean install

RUN rm -f target/original*.jar

WORKDIR /app/target

RUN sh -c "ls *.jar | head -n 1 | xargs -I {} mv {} app.jar"

WORKDIR /app

CMD ["java", "-jar", "target/app.jar"]
