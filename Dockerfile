# Pull OpenJDK, it uses Oracle Linux
FROM openjdk:11

# Update the environment and perform apt installations
RUN apt-get update -y \
    && apt-get install -y maven \
    && apt-get clean

# create a directory for app
WORKDIR /swim-broker

# Copy project directory into the container
COPY  . /swim-broker

# Generate fat jar
RUN mvn package

# Metric and healthchecks
EXPOSE 9350 
# Application
EXPOSE 9351

# Production FAT jar
CMD java -jar target/SWIM-Broker-2.0-SNAPSHOT.jar server settings_prod.yml