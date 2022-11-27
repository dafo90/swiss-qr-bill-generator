FROM eclipse-temurin:17-jre

ENV TZ=Europe/Zurich

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone

WORKDIR /home/spring

# creating unprivileged user
RUN groupadd --gid 1000 spring \
    && useradd --uid 1000 --gid spring --shell /bin/bash --home-dir /home/spring spring \
    && chown spring:spring .

# creating directory to read and write files
RUN mkdir /data \
    && chown -R spring:spring /data

COPY --chown=spring:spring /target/app.jar ./
COPY --chown=spring:spring entrypoint.sh ./

RUN chmod +x ./entrypoint.sh

ENTRYPOINT [ "./entrypoint.sh" ]
