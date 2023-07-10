FROM gradle:7.5.1-jdk17-alpine AS build
USER root

RUN addgroup -S japc -g 501
RUN adduser -S -h /japc -u 501 -s /bin/bash -G japc japc
RUN apk update && apk add wget && apk add openssl

RUN wget -O root.crt "https://ca.cern.ch/cafiles/certificates/CERN%20Certification%20Authority.crt"
RUN wget -O auth.crt "https://ca.cern.ch/cafiles/certificates/CERN%20Certification%20Authority(1).crt"

RUN keytool -cacerts -keypass changeit -alias root.crt -storepass changeit -noprompt -importcert -file root.crt
RUN keytool -cacerts -keypass changeit -alias auth.crt -storepass changeit -noprompt -importcert -file auth.crt
RUN rm -rf *.crt

USER japc
WORKDIR /japc/build
COPY --chown=japc:japc . .
RUN gradle -Pversion=prod bootWar && cp japc-prod.war /japc
WORKDIR /japc
RUN rm -rf build
EXPOSE 8443
USER root
RUN chown -R japc:japc /japc
RUN chmod -R 777 /japc
USER japc
CMD ["java", "-jar", "japc-prod.war"]

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

#USER japc
#RUN keytool -genkeypair -alias keystore.p12 -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore ${KEYSTORE} -validity 3650 -storepass changeit -dname O=CERN -dname OU=CERN -dname CN=cern.ch
#RUN keytool -import -alias auth.crt -keystore ${KEYSTORE} -file auth.crt -storepass changeit
#RUN keytool -import -alias root.crt -keystore ${KEYSTORE} -file root.crt -storepass changeit
#RUN wget -O root.crt "https://cafiles.cern.ch/cafiles/certificates/CERN%20Root%20Certification%20Authority%202.crt"
#RUN wget -O auth.crt "https://cafiles.cern.ch/cafiles/certificates/CERN%20Certification%20Authority(2).crt"
