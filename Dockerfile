FROM openjdk:11 

RUN apt-get update -y && apt-get install -y software-properties-common

RUN apt-add-repository ppa:octave/stable \ 
	&& apt-get install -y build-essential \
	&& apt-get install -y glibc-source \
        && apt-get install -y libglib2.0-dev \
	&& apt-get install -y octave \
	&& apt-get install -y vim \
        && rm -rf /var/lib/apt/lists/*

ENV VERSION=3.0.0

RUN mkdir -p /home/huxley/data

COPY ./target/evaluator-${VERSION}.jar /home/huxley/
COPY ./scripts /home/huxley/data/scripts
COPY ./config.properties /home/huxley/data/scripts/safe_scripts/

RUN cd /home/huxley/data/scripts/safe_scripts/ && ls -lah && ./deploy_scripts.sh

WORKDIR /home/huxley

ENTRYPOINT java -jar evaluator-${VERSION}.jar
