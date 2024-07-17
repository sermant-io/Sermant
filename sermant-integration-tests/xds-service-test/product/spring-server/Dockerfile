FROM openjdk:8
WORKDIR /home
COPY agent/ /home/agent
COPY spring-server.jar /home/spring-server.jar
COPY start.sh /home
RUN chmod -R 777 /home
ENTRYPOINT ["sh", "/home/start.sh"]
