FROM openjdk:8u302-jdk

WORKDIR /home

COPY sermant-injector.jar /home
COPY start.sh /home

RUN chmod -R 777 /home

ENTRYPOINT ["sh", "/home/start.sh"]