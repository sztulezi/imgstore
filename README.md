# Image Store

Image storage service where images can be uploaded and downloaded via REST API. Additionally, all of the images can be downloaded in a zip file.
* The sevice accepts png and jpg formats only
* Large (> 5000x5000) images are scaled down during upload
* Images are stored encrypted (AES) in the DB


### Prerequisites

* java 17+
* maven
* docker-compose
* imagemagick


### Database setup

Before starting the postgres container for the first time we need to create the referenced volume:

```
$ docker volume create imgstore-pg
```

(Optional) If you want to remove the volume completely (in order to recreate it as an empty volume):

```
$ docker-compose down
$ docker volume rm imgstore-pg
```


### External tool setup

In order to use ImageMagick within the application the `MAGICK` environment variable must exist pointing to the directory of the tool.

Windows example:

```
$ setx MAGICK "C:\Program Files\ImageMagick-7.1.1-Q16-HDRI"
```

Linux example:

```
$ export MAGICK=/usr/local/lib/imagemagick
```


### Build the application

```
$ mvn clean install
```


### Run the application

Launch the postgres container first (the `docker-compose.yml` is located in the `<project root>/infra` directory):

```
$ docker-compose up -d
```

Launch the application:

```
$ java -jar target/imgstore-0.0.1-SNAPSHOT.jar
```


### API access

[Swagger UI](http://localhost:8080/swagger-ui.html)

