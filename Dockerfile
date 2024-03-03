FROM registry.access.redhat.com/ubi8/openjdk-17:1.18

ENV LANGUAGE='en_US:en'

COPY --chown=185 target/crebito-api-1.0.0-SNAPSHOT-runner.jar /deployments/

EXPOSE 8080
USER 185

ENTRYPOINT [ "java", "-jar", "/deployments/crebito-api-1.0.0-SNAPSHOT-runner.jar" ]

