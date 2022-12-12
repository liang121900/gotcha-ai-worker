FROM python:3.10
WORKDIR /home/app
COPY layers/libs /home/app/libs
COPY layers/classes /home/app/classes
COPY layers/resources /home/app/resources
COPY layers/application.jar /home/app/application.jar


RUN pip install --no-cache -r /home/app/resources/python/deep-vision/requirements-3_10.txt

RUN apt-get install bash
RUN apt-get update && \
apt-get install -y --no-install-recommends openjdk-17-jre
RUN echo $(java -version)
RUN echo $(python --version)

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
