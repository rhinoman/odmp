FROM centos:8

RUN dnf update -y \
    && dnf install -y gcc java-11-openjdk-devel python38 python38-devel \
    && dnf clean all \
    && rm -rf /var/cache/dnf

RUN alternatives --set python /usr/bin/python3.8

ENV JAVA_HOME /etc/alternatives/java_sdk
RUN pip3 install --user jep \
    && pip3 install --user numpy

ARG JAR_FILE
ADD target/${JAR_FILE} /opt/odmp/opendmp-processor.jar

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/odmp/opendmp-processor.jar