#Definizione della BaseImage con FROM.
#Si tratta di un modello minimale, preconfigurato, che contiene un sistema operativo,
#strumenti di runtime, librerie e dipendenze.

#Le BaseImage, posso essere create "from-scratch",
#altrimenti su Docker Hub (https://hub.docker.com/search/?q=java)
#sono disponibili una serie di immagini già configurate.
#In questo caso sto usando la distribuzione openJdk 17 di amazoncorretto,
#che si basa su Alpine Linux.
FROM amazoncorretto:17-apline-jdk

#Aggiunta di Metadata (info descrittive) con LABEL
#In questo caso definisco solo il maintainer
LABEL maintainer="Alessandro mymail@live.it"

#Con COPY dico al costruttore di copiare delle risorse dall'host e
#inserirle nella Container Image.
#In questo caso copio il JAR in Target e lo rinomino in "app.jar"
COPY target/springDemo-0.0.1-SNAPSHOT.jar app.jar

#In ENTRYPOINT si configura il comportamento di default del container.
#In questo caso, specifico il comando Java standard per eseguire un JAR,
#così la mia applicazione partirà con il container.
ENTRYPOINT["java", "-jar", "/app.jar"]




#Per la build, dal terminale si lancia il comando:
#docker build --tag=dockeralex00/springSecurity:latest .
#N.B. Docker Desktop deve essere installato