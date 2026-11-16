FROM registry.cern.ch/docker.io/library/eclipse-temurin:21.0.13_9-jdk-alpine
ARG WAR_NAME="japc-1.0.0.war"
ENV WAR_NAME=${WAR_NAME}
WORKDIR /japc
# monit-otlp.cern.ch is signed by the CERN Grid CA, which the JDK does not trust
RUN wget -q -O /tmp/grid.crt "https://cafiles.cern.ch/cafiles/certificates/CERN%20Grid%20Certification%20Authority(1).crt"     && keytool -importcert -cacerts -storepass changeit -noprompt -alias cern-grid-ca -file /tmp/grid.crt && rm /tmp/grid.crt
COPY build/libs/${WAR_NAME} .
EXPOSE 8443
RUN chgrp -R 0 /japc && chmod -R g=u /japc
ENTRYPOINT ["/bin/sh", "-c"]
CMD ["java -jar ${WAR_NAME}"]
