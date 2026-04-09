FROM tomcat:11.0.15-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/CurrencyExchangeJB.war /usr/local/tomcat/webapps/ROOT.war

COPY src/main/resources/db/currency_exchange.db /usr/local/tomcat/currency_exchange.db

EXPOSE 8080