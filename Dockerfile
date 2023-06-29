FROM gradle:7.5.1-jdk17-alpine AS build
WORKDIR /buildDir
EXPOSE 8080

USER root
RUN apk update && apk add wget && mkdir certs
RUN wget -O certs/root.cert "https://cafiles.cern.ch/cafiles/certificates/CERN%20Root%20Certification%20Authority%202.crt"
RUN wget -O certs/auth.cert "https://cafiles.cern.ch/cafiles/certificates/CERN%20Certification%20Authority(2).crt"

RUN keytool -cacerts -keypass changeit -alias root.cert -storepass changeit -noprompt -importcert -file certs/root.cert
RUN keytool -cacerts -keypass changeit -alias auth.cert -storepass changeit -noprompt -importcert -file certs/auth.cert

RUN addgroup -S japc -g 501
RUN adduser -S -h /home/japc -u 501 -s /bin/bash -G japc japc
RUN chown -R japc:japc ../buildDir
RUN chmod -R 777 ../buildDir
USER japc

COPY . .
CMD ["gradle", "bootRun"]

#FROM tomcat:11.0.0-jdk17
#RUN apt-get update -y && apt-get install -y netcat
#RUN groupadd -r catalina -g 501
#RUN useradd -d /home/catalina -u 501 -m -s /bin/bash -g catalina catalina
#RUN chown -R catalina:catalina /usr/local/tomcat
#RUN chmod -R 777 /usr/local/tomcat
#USER catalina
#ENV webappDir=/buildDir/webapp-build
#EXPOSE 8080
#WORKDIR $CATALINA_HOME
#
#COPY --from=build $webappDir/japc-1.0.0.war webapps
#COPY --from=build $webappDir/server.xml conf