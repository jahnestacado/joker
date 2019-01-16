FROM jahnestacado/scala-sbt-arm64v8 as sbt-build
COPY . /repo
WORKDIR /repo
RUN sbt update
RUN sbt universal:packageZipTarball
#-----------------------------------------------------
FROM openjdk:8-jdk-alpine3.8
COPY --from=sbt-build  /repo/target/universal/twitter-producer-*.tgz /service/
WORKDIR service
RUN tar xzf twitter-producer-*.tgz && rm twitter-producer-*.tgz

ENTRYPOINT ["./bin/twitter-producer"]
