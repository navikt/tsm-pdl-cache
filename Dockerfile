FROM gcr.io/distroless/java21-debian12@sha256:320d27b74347b6baaf35bcbe21bae51f738b07ed2c0741ead5cf050a3b5c3487
WORKDIR /app
COPY build/libs/app.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
