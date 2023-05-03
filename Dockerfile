FROM gradle:7.5.1-jdk17-alpine AS build
WORKDIR /buildDir
COPY . .
RUN apk update && apk add openssl && apk add wget
RUN true | openssl s_client -connect artifactory.cern.ch:443 | openssl x509 > artifactory.cert
RUN wget -O root.cert https://ca.cern.ch/cafiles/certificates/CERN%20Root%20Certification%20Authority%202.crt
RUN wget -O grid.cert https://ca.cern.ch/cafiles/certificates/CERN%20Grid%20Certification%20Authority(1).crt

RUN wget -O auth.cert https://ca.cern.ch/cafiles/certificates/CERN%20Certification%20Authority.crt
RUN wget -O auth1.cert https://ca.cern.ch/cafiles/certificates/CERN%20Certification%20Authority(1).crt
RUN wget -O auth2.cert https://ca.cern.ch/cafiles/certificates/CERN%20Certification%20Authority(2).crt

USER root
RUN keytool -importcert -cacerts -alias root-cert -file root.cert -storepass changeit -noprompt
RUN keytool -importcert -cacerts -alias grid-cert -file grid.cert -storepass changeit -noprompt
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
