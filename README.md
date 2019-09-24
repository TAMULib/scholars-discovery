[![Build Status](https://travis-ci.org/vivo-community/scholars-discovery.svg?branch=master)](https://travis-ci.org/vivo-community/scholars-discovery)
[![Coverage Status](https://coveralls.io/repos/github/vivo-community/scholars-discovery/badge.svg?branch=master)](https://coveralls.io/github/vivo-community/scholars-discovery?branch=master)

# scholars-discovery

VIVO Scholars Discovery is a middleware project that pulls [VIVO](https://duraspace.org/vivo/) content into its own search index (Solr) and then exposes that content via a RESTful service endpoint.

Various frontend applications are available (or can be built) to display the content as read-only websites.
Existing frontend applications include:
1. [VIVO Scholars Angular](https://github.com/vivo-community/scholars-angular)

# Background

Scholars-Discovery project was initiated by [Scholars@TAMU](https://scholars.library.tamu.edu/) project team at Texas A&M University (TAMU) Libraries. In support of the Libraries’ goal of enabling and contextualizing the discovery of scholars and their expertise across disciplines, the Scholars’ team at TAMU Office of Scholarly Communications (OSC) proposed the Scholars version 2 project, which focuses on deploying (1) new public facing layer (Read-only), (2) faceted search engine, (3) Data reuse options, and (4) search engine optimization. Digital Initiative (DI) at TAMU Libraries collaborated with the OSC to design and implement the current system architecture including Scholars-Discovery and VIVO Scholars Angular. In a later stage, Scholars-Discovery project was adopted by VIVO Community’s [VIVO Scholar Task Force](https://wiki.duraspace.org/display/VIVO/VIVO+Scholar+Task+Force).

# System Architecture

![System Architecture](https://webassets.library.tamu.edu/public/files/scholarsdiscovery.png)

## Installation instructions

1. [Install](https://maven.apache.org/install.html) Maven
2. [Install](https://docs.docker.com/install/) Docker
3. Clone this project
4. Build and Run the provided Solr application
```bash
   cd scholars-discovery/solr
   docker build --tag=scholars/solr .
   docker run -p 8983:8983 scholars/solr
```
5. Build and Run the application
```bash
   mvn clean install
   mvn spring-boot:run
```
   - Note: Custom application configuration can be achieved by providing a location and an optional profile, such as:
```bash
      mvn spring-boot:run -Dspring-boot.run.config.location=/some/directory/ -Dspring-boot.run.profiles=dev
```
   - ..where an `application-dev.yml` exists in the `/some/location/` directory

## Docker Deployment

```bash
docker build -t scholars/discovery .
```

```bash
docker run -d -p 9000:9000 -e SPRING_APPLICATION_JSON="{\"spring\":{\"data\":{\"solr\":{\"host\":\"http://localhost:8983/solr\"}}},\"ui\":{\"url\":\"http://localhost:3000\"},\"vivo\":{\"base-url\":\"https://scholars.library.tamu.edu/vivo\"},\"graphql\":{\"spqr\":{\"gui\":{\"enabled\":true}}},\"middleware\":{\"allowed-origins\":[\"http://localhost:3000\"],\"index\":{\"onStartup\":false},\"export\":{\"individualBaseUri\":\"http://localhost:3000/display\"}}}" scholars/discovery
```

> The environment variable `SPRING_APPLICATION_JSON` will override properties in application.yml.
