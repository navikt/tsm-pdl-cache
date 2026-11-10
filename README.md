# infotoast
A minimal PDL cache for Team Symfoni

## Technologies used
* Kotlin
* Ktor
* Gradle
* Kafka


## Requirements for developing the application
#### Java
Make sure you have the Java JDK 25 installed
You can check which version you have installed using this command:
``` bash
java -version
```
or install it: https://sdkman.io/

#### Docker
Make sure you have docker installed using this command:
You can check which version you have installed using this command:
``` bash
docker --version
```
or install it: https://docs.docker.com/engine/install/

#### Docker Compose
Make sure you have docker compose installed using this command:
You can check which version you have installed using this command:
``` bash
docker compose version
```
or install it: https://docs.docker.com/compose/install/


### Building the application
To build the application locally and run tests you can simply run
``` bash
./gradlew clean build
 ```

### Upgrading the Gradle wrapper version to latest
``` bash
./gradlew :wrapper --gradle-version latest
```

### Contact

This project is maintained by [navikt/tsm](CODEOWNERS)

Questions and/or feature requests? Please create an [issue](https://github.com/navikt/tsm-pdl-cache/issues)

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack
channel [#team-symfoni](https://nav-it.slack.com/archives/C07MY3KCDS5)