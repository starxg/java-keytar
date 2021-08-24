FROM i386/debian:stretch

RUN apt-get update
RUN apt-get install  -q -y \
    libsecret-1-dev \
    pkg-config \
    gcc \
    g++ \
    cmake \
    openjdk-8-jdk && \
    ln -s /usr/include/libsecret-1/libsecret /usr/include/libsecret


ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-i386
ENV CC gcc
ENV CXX g++
