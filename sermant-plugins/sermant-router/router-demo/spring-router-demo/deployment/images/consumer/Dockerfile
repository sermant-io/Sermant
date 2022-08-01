FROM java:8

WORKDIR /home

COPY consumer-demo.jar /home

COPY start.sh /home

RUN chmod -R 777 /home

ENTRYPOINT ["sh", "/home/start.sh"]