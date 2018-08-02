FROM maven:3.5.4-jdk-8
MAINTAINER ICGC <dcc-support@icgc.org>
ARG TAG_NAME

RUN apt update && apt install wget unzip 
RUN wget https://github.com/overture-stack/rollcall/archive/${TAG_NAME}.tar.gz  && \
    tar zxvf *.tar.gz && \
    mv rollcall-* rollcall

RUN cd rollcall && \
    mvn clean package -DskipTests

COPY scripts /scripts
CMD /scripts/run.sh rollcall/target/rollcall-0.0.1-SNAPSHOT.jar 

    

