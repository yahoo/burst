ARG JAVA_VERSION=8
FROM amazoncorretto:${JAVA_VERSION}
RUN amazon-linux-extras enable mariadb10.5 &&\
    yum install -y openssh-clients openssl mariadb &&\
    yum clean all && rm -rf /var/cache/yum
