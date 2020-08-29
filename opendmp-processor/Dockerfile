FROM centos:8 AS compile-image

RUN dnf update -y \
    && dnf install -y gcc java-11-openjdk-devel python38 python38-devel \
    && dnf clean all \
    && rm -rf /var/cache/dnf

ENV JAVA_HOME /etc/alternatives/java_sdk
RUN pip3 install --user jep

FROM centos:8 AS build-image
ARG JAR_FILE
RUN dnf update -y \
    && dnf install -y java-11-openjdk-headless python38 \
    && dnf clean all \
    && rm -rf /var/cache/dnf

ENV JAVA_HOME /etc/alternatives/java_sdk

COPY --from=compile-image /root/.local /root/.local
ADD target/${JAR_FILE} /opt/odmp/opendmp-processor.jar

ENV PATH=/root/.local/bin$PATH

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/odmp/opendmp-processor.jar