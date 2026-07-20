FROM gcr.io/distroless/java25-debian13@sha256:73f2263db8defa233004a7c700fd81e25c8747a530c413bddf74367b68663468
WORKDIR /app
COPY build/libs/app.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
