# DJ Knox Flowable Work

## DISCLAIMER

This project was only intended to show how to use x509 certificate Spring security configuration. It is only a PoC.

## Project structure

- docker
  - docker-compose.yml file to run the infrasctructure dependencies:
    - The database server (Postgresql)
    - Elasticsearch
- keystore
  - Makefile (from [baeldung.com](https://www.baeldung.com/x-509-authentication-in-spring-security#Keystores)) to generate and configure the server and client certificates.
  - Already generated certificates for localhost and admin.
- Parent POM
  - [dj-knox-work](https://localhost:8443)
    - Flowable Work configured to ask for a x509 client certificate.
  - [dj-knox-design](https://localhost:18091)
    - Flowable Design configured to ask for a x509 client certificate.
  - [dj-knox-control](https://localhost:18092)
    - Flowable Control configured to ask for a x509 client certificate.
  - [dj-knox-work-no-x509](http://localhost:8090)
    - Flowable Work configured with the standard form login security. This module is used to test a "normal" Flowable Work installation along with the x509 versions and compare functionality, access to features, etc.

## Architechture

```text
                                 +--------------------+
                                 |                    |
                      x509       |  Flowable Control  |
                   +------------->                    |
            XXX    |             +------------------+-+
           XXXXX   |                                |
             X     +                                |       +-------------------+
          XXXXXXX                                   |       |                   |
        XXX  X  XXX      x509                       |       |  Flowable Design  |
             X     +---------------------------+    |       |                   |
             X                                 |    |       +--+----------------+
           XXXX                                |    |          |
          XX  XX                               |    |          |
         XX    XX                              |    | x509 (admin certificate)
                                               |    |          |
                                            +--v----v----------v--+
                                            |                     |
                  +--------------+   JDBC   |  Flowable Work      |
                  |              <----------+                     |
                  |   Database   |          +-+-----------------^-+
                  |              |            |
                  +--------------+            |REST
                                              |
                              +---------------v-+
                              |                 |
                              |  ElasticSearch  |
                              |                 |
                              +-----------------+
```

## Starting the project

This project uses public key certificates to authenticate the users.

First you must trust the self-signed certificates. A quick'n'dirty way to do this is to put the ca and the localhost certificates in your JVM cacerts file.

```bash
Java 8:
keytool -import -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -alias DesatranquesCA -file ca.crt

Java 11:
keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -alias DesatranquesCA -file ca.crt

```

Also, you need to import the certificate authority certificate file (ca.crt) and the client certificate file (admin.p12) in your browser. You must set that you trust the CA to indentify websites.

All modules are Maven Spring Boot projects that can be started with:

```bash
mvn spring-boot:run
```

This is the order to start the applications:

1. start the database and elasticsearch with docker-compose
2. start dj-knox-work
3. start dj-knox-design and dj-knox-control
4. Optionally, start dj-knox-work-no-x509

### Data

Data created by the database and Elasticsearch are stored in docker volumes `data_db` and `data_es.
This allows you to purge and recreate the containers without loosing any data.

If you need a clean state for the database and elasticsearch just execute `docker-compose down -v`inside the `/docker` directory.
The volumes will be recreated as soon as you restart the containers.  
BE CAREFUL AS WITH THIS YOU WILL LOOSE ALL YOUR DATA STORED IN THE DATABASE AND ELASTICSEARCH!

## Sample users

By default three users are created **BUT ONLY THE `admin` user is configured to accept x509 authentication**.

| User | User Definition Key | Login | Password |
| -------------| ------------- | ------------- | ------------- |
| Flowable Administrator | admin-flowable | admin | test |
| Knox Administrator | admin-knox | knox.admin | test |
| Knox User | user-knox | knox.user | test |

To enable more users, the UserDetailsService of each module must be extended.

## GET `/users/{username}` controller

You can use this GET controller to see the UserDetails object that is returned for any user (once you are authenticated). This endpoint is useful to get the authorities that are configured.
