FROM registry.access.redhat.com/ubi8/openjdk-17:1.18

ENV LANGUAGE='en_US:en'

COPY --chown=185 target/*.jar /deployments/

EXPOSE 8080
USER 185

ENV JAVA_APP_JAR="/deployments/*.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]

