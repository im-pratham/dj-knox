# DJ Knox Flowable Work


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
  - [dj-knox-platform](https://localhost:18443)
    - Flowable Platform (headless) configured to accept HTTP-Basic authentication requests.
  - [dj-knox-design](https://localhost:8090)
    - Flowable Design configured to ask for a x509 client certificate.
  - [dj-knox-control](https://localhost:8090)
    - Flowable Control configured to ask for a x509 client certificate.
  - [dj-knox-work-no-x509](https://localhost:8090)
    - Flowable Work configured with the standard form login security. This module is used to test a "normal" Flowable Work installation along with the x509 versions and compare functionality, access to features, etc.

## Architechture

```text
                                 +--------------------+
                                 |                    |
                      x509       |  Flowable Control  |
                   +------------->                    |
            XXX    |             +-------------+------+
           XXXXX   |                           |
             X     +                           |            +-------------------+
          XXXXXXX                              |            |                   |
        XXX  X  XXX      x509                  |            |  Flowable Design  |
             X        +------------------------------------->                   |
x509         X                                 |            +--+----------------+
     +-+   XXXX                                |               |
     |    XX  XX                             HTTP-Basic auth (SSL)
     |   XX    XX                              |               |
     |                                         |               |
     |                                      +--v---------------v--+
     |                                      |                     |
     |            +--------------+   JDBC   |  Flowable Platform  |
     |    JDBC    |              <----------+                     |
     |      +----->   Database   |          +-+-----------------^-+
     |      |     |              |            |
     |      |     +--------------+            |REST
     |      |                                 |
     |      |                 +---------------v-+
  +--v------+-------+         |                 |
  |                 |  REST   |  ElasticSearch  |
  |  Flowable Work  +--------->                 |
  |                 |         +-----------------+
  +-----------------+

```

## Starting the project

This project uses public key certificates to authenticate the users.

First you must put the ca certificate in your JVM cacerts file to trust the certificate authority.

```bash
keytool -import -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -alias DesatranquesCA -file ca.crt
```

Also, you need to import the certificate authority certificate file (ca.crt) and the client certificate file (admin.p12) in your browser. You must set that you trust the CA to indentify websites.

All modules are Maven Spring Boot projects that can be started with:

```bash
mvn spring-boot:run
```

This is the order to start the applications:

1. start the database and elasticsearch with docker-compose
2. start dj-knox-work
3. start dj-knox-platform
4. start dj-knox-design and dj-knox-control
5. Optionally, start dj-knox-work-no-x509

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
