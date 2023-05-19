FROM tomcat:9.0.73-jdk11-temurin-focal
COPY conf/tomcat-users.xml /usr/local/tomcat/conf/
COPY conf/server.xml /usr/local/tomcat/conf/
COPY conf/context.xml /usr/local/tomcat/conf/
COPY target/example-sso.war /usr/local/tomcat/webapps/
