FROM debian:buster

RUN dpkg --add-architecture arm64
RUN apt-get update -q -y && apt-get install -q -y --no-install-recommends \
  crossbuild-essential-arm64 \
  pkg-config \
  ca-certificates \
  libxkbfile-dev:arm64 \
  libsecret-1-dev:arm64 \
  curl \
  cmake \
  software-properties-common && \
  apt-add-repository 'deb http://security.debian.org/debian-security stretch/updates main' && \
  apt-get update -q -y && \
  apt-get install -q -y openjdk-8-jdk && \
  ln -s /usr/include/libsecret-1/libsecret /usr/include/libsecret


ENV AS=/usr/bin/aarch64-linux-gnu-as \
  STRIP=/usr/bin/aarch64-linux-gnu-strip \
  AR=/usr/bin/aarch64-linux-gnu-ar \
  CC=/usr/bin/aarch64-linux-gnu-gcc \
  CPP=/usr/bin/aarch64-linux-gnu-cpp \
  CXX=/usr/bin/aarch64-linux-gnu-g++ \
  LD=/usr/bin/aarch64-linux-gnu-ld \
  FC=/usr/bin/aarch64-linux-gnu-gfortran \
  PKG_CONFIG_PATH=/usr/lib/aarch64-linux-gnu/pkgconfig \
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
