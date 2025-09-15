FROM gcr.io/distroless/java21-debian12@sha256:418b2e2a9e452aa9299511427f2ae404dfc910ecfa78feb53b1c60c22c3b640c
WORKDIR /app
COPY build/libs/app.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
