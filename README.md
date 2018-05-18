# Used book store



### Purpose
  
The goal is to serve the local community of book lovers who wants buy/sell the used books.

It is developed with the Play! Framework 2.6 and make use of akka to send images to S3, where the images are persisted.

The application is running in two containers, the first is the main play app and the second is the postgres

Demo of production version is running at http://52.15.156.35


## Main tools

* [Play framework 2.6.12](https://www.playframework.com)
* [Scrimage](https://github.com/sksamuel/scrimage) 
* Akka
* Postgres
* Docker
* AWS S3
* AWS Lightsail

#### Note : This project is in development phase and exposes many bugs and features that are not implemented yet.
