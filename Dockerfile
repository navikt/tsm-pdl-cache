FROM gcr.io/distroless/java21-debian12@sha256:04f730658c79b99d42fadf2f8dd11c9d441cee10ce5eaa7cad4a75eaca2cb52a
WORKDIR /app
COPY build/libs/app.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
