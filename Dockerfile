FROM gradle:7.5.1-jdk17-alpine AS build
WORKDIR /buildDir
COPY . .
RUN apk update && apk add openssl
RUN true | openssl s_client -connect artifactory.cern.ch:443 | openssl x509 > artifactory.cert
USER root
RUN keytool -importcert -cacerts -alias artifactory-cert -file artifactory.cert -storepass changeit -noprompt
RUN gradle --no-daemon clean war

FROM tomcat:11.0.0-jdk17
RUN apt-get update -y && apt-get install -y netcat
RUN groupadd -r catalina -g 501
RUN useradd -d /home/catalina -u 501 -m -s /bin/bash -g catalina catalina
RUN chown -R catalina:catalina /usr/local/tomcat
RUN chmod -R 777 /usr/local/tomcat
USER catalina
ENV webappDir=/buildDir/webapp-build
EXPOSE 8080
WORKDIR $CATALINA_HOME

COPY --from=build $webappDir/japc-1.0.0.war webapps
COPY --from=build $webappDir/server.xml conf

CMD ["catalina.sh", "run"]
