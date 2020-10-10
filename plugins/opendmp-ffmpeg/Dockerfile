FROM centos:8

RUN dnf -y install https://download.fedoraproject.org/pub/epel/epel-release-latest-8.noarch.rpm \
    && dnf -y localinstall --nogpgcheck https://download1.rpmfusion.org/free/el/rpmfusion-free-release-8.noarch.rpm \
    && dnf -y install --nogpgcheck https://download1.rpmfusion.org/nonfree/el/rpmfusion-nonfree-release-8.noarch.rpm \
    && dnf -y install http://rpmfind.net/linux/epel/7/x86_64/Packages/s/SDL2-2.0.10-1.el7.x86_64.rpm \
    && dnf update -y \
    && dnf install -y java-11-openjdk-devel ffmpeg \
    && dnf clean all \
    && rm -rf /var/cache/dnf

ENV JAVA_HOME /etc/alternatives/java_sdk

ARG JAR_FILE
ADD target/${JAR_FILE} /opt/odmp/opendmp-plugin-ffmpeg.jar

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/odmp/opendmp-plugin-ffmpeg.jar