mvn clean package
sudo docker build . -t cs505-final
sudo docker run -d --rm -p 9999:9999 cs505-final
