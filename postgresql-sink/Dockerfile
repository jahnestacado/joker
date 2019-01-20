FROM jahnestacado/scala-sbt-arm64v8 as sbt-build
COPY . /repo
WORKDIR /repo
RUN sbt update
RUN sbt universal:packageZipTarball
#-----------------------------------------------------
FROM openjdk:8-jdk-alpine3.8
RUN apk add --no-cache bash
COPY --from=sbt-build  /repo/target/universal/postgresql-sink-*.tgz /service/
WORKDIR service
RUN tar xzf postgresql-sink-*.tgz && rm postgresql-sink-*.tgz

ENTRYPOINT ["./bin/postgresql-sink"]
