FROM openjdk:8
WORKDIR /home
COPY agent/ /home/agent
COPY spring-cloud-client.jar /home/spring-cloud-client.jar
COPY start.sh /home
RUN chmod -R 777 /home
ENTRYPOINT ["sh", "/home/start.sh"]
