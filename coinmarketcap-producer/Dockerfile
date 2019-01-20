FROM jahnestacado/scala-sbt-arm64v8 as sbt-build
COPY . /repo
WORKDIR /repo
RUN sbt update
RUN sbt universal:packageZipTarball
#-----------------------------------------------------
FROM openjdk:8-jdk-alpine3.8
RUN apk add --no-cache bash
COPY --from=sbt-build  /repo/target/universal/coinmarketcap-producer-*.tgz /service/
WORKDIR service
RUN tar xzf coinmarketcap-producer-*.tgz && rm coinmarketcap-producer-*.tgz

ENTRYPOINT ["./bin/coinmarketcap-producer"]
