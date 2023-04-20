FROM tomcat:9.0.62-jre11-openjdk-slim
COPY conf/logging.properties /usr/local/tomcat/conf/
COPY conf/tomcat-users.xml /usr/local/tomcat/conf/
COPY conf/server.xml /usr/local/tomcat/conf/
COPY conf/context.xml /usr/local/tomcat/conf/
COPY target/example-sso.war /usr/local/tomcat/webapps/
