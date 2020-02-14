FROM adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine-slim as builder

# Build song-server jar
COPY . /srv
WORKDIR /srv
RUN ./mvnw clean package -DskipTests


FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine as server
# Paths
ENV APP_HOME /srv/rollcall
ENV APP_LOGS $APP_HOME/logs
ENV JAR_FILE            /srv/rollcall/rollcall.jar

COPY --from=builder /srv/target/rollcall-*.jar $JAR_FILE

ENV APP_USER rollcall
ENV APP_UID 9999
ENV APP_GID 9999

RUN addgroup -S -g $APP_GID $APP_USER  \
    && adduser -S -u $APP_UID -g $APP_GID $APP_USER  \
    && mkdir -p $APP_HOME $APP_LOGS \
    && chown -R $APP_UID:$APP_GID $APP_HOME

USER $APP_USER

WORKDIR $APP_HOME

CMD java -Dlog.path=$APP_LOGS \
        -jar $JAR_FILE \
        --spring.config.location=classpath:/application.yml