FROM eclipse-temurin:21-alpine
ARG WAR_NAME="japc-1.0.0.war"
ENV WAR_NAME=${WAR_NAME}
WORKDIR /japc
COPY build/libs/${WAR_NAME} .
EXPOSE 8443
RUN chgrp -R 0 /japc && chmod -R g=u /japc
ENTRYPOINT ["/bin/sh", "-c"]
CMD ["java -jar ${WAR_NAME}"]





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
