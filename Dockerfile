FROM gcr.io/distroless/java21-debian12@sha256:995893ef4d670f7497394d8bc680ca61ae79bd6d4fedf23b4589028e91645aa4
WORKDIR /app
COPY build/libs/app.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
