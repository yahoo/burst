ARG JAVA_VERSION=8
FROM amazoncorretto:${JAVA_VERSION}

RUN yum install -y openssh-clients openssl &&\
    yum clean all && rm -rf /var/cache/yum
