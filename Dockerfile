FROM eclipse-temurin:20

ENV SBT_VERSION 1.9.2

RUN apt-get update && apt-get -y install clang libstdc++-12-dev

RUN curl -L -o sbt-$SBT_VERSION.tgz https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz && \
    mkdir sbt && \
    tar -xvzf sbt-$SBT_VERSION.tgz -C /sbt --strip-components=1

ENV PATH="$PATH:/sbt/bin"

WORKDIR /app

COPY . /app

ENTRYPOINT ["tail"]
CMD ["-f","/dev/null"]