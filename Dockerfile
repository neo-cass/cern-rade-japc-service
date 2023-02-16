FROM gradle:7.5.1-jdk17-alpine AS build
WORKDIR /buildDir
COPY . .
RUN apk update && apk add openssl
RUN true | openssl s_client -connect artifactory.cern.ch:443 | openssl x509 > artifactory.cert
USER root
RUN keytool -importcert -cacerts -alias artifactory-cert -file artifactory.cert -storepass changeit -noprompt
RUN gradle --no-daemon clean war

FROM tomcat:11.0.0-jdk17
ENV webappDir=/buildDir/webapp-build
EXPOSE 9090
WORKDIR $CATALINA_HOME

COPY --from=build $webappDir/japc-new-1.0.0.war webapps
COPY --from=build $webappDir/server.xml conf

RUN groupadd -r serverUser -g 501
RUN useradd -d /home/serverUser -u 501 -m -s /bin/bash -g serverUser serverUser
RUN chown -R serverUser:serverUser /usr/local/tomcat
RUN chmod -R 777 /usr/local/tomcat
USER serverUser

CMD ["catalina.sh", "run"]
