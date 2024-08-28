[![Java CI with Maven](https://github.com/TAMULib/scholars-discovery/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/TAMULib/scholars-discovery/actions?query=workflow%3A%22Java+CI+with+Maven%22)
[![Coverage Status](https://coveralls.io/repos/github/TAMULib/scholars-discovery/badge.svg)](https://coveralls.io/github/TAMULib/scholars-discovery)

# scholars-discovery

VIVO Scholars Discovery is a middleware project that pulls [VIVO](https://duraspace.org/vivo/) content into its own search index (Solr) and then exposes that content via a RESTful service endpoint.

Various frontend applications are available (or can be built) to display the content as read-only websites.
Existing frontend applications include:

1. [VIVO Scholars Angular](https://github.com/vivo-community/scholars-angular)

# API

[Scholars Middleware REST Service API Documentation](https://tamulib.github.io/scholars-discovery/)

# Background

Scholars Discovery project was initiated by [Scholars@TAMU](https://scholars.library.tamu.edu/) project team at Texas A&M University (TAMU) Libraries. In support of the Libraries’ goal of enabling and contextualizing the discovery of scholars and their expertise across disciplines, the Scholars’ team at TAMU Office of Scholarly Communications (OSC) proposed the Scholars version 2 project, which focuses on deploying (1) new public facing layer (Read-only), (2) faceted search engine, (3) Data reuse options, and (4) search engine optimization. Digital Initiative (DI) at TAMU Libraries collaborated with the OSC to design and implement the current system architecture including Scholars Discovery and VIVO Scholars Angular. In a later stage, Scholars Discovery project was adopted by VIVO Community’s [VIVO Scholar Task Force](https://wiki.duraspace.org/display/VIVO/VIVO+Scholar+Task+Force).

# Technology

Scholars discovery system is first and foremost an ETL system in which **e**xtracts data from VIVO's triplestore, **t**ransforms triples into flattened documents, and **l**oads the documents into Solr. The Solr index is then exposed via REST API and GraphQL API as a nested JSON. A secondary feature is that of providing a persistent, configurable discovery layout for rendering a UI. 

Extraction from VIVO is done view configurable harvesters in which make SPARQL requests to the triplestore for a collection of objects and subsequent SPARQL requests for each property value of the target document. The SPARQL requests can be found in [src/main/resources/templates/sparql](https://github.com/vivo-community/scholars-discovery/tree/master/src/main/resources/templates/sparql). The transformation is done granularly converting resulting triples of a SPARQL request into a property of a flattened document. This document is then saved into a heterogeneous Solr collection. The configuration of the Solr collection can be found in [solr/config](https://github.com/vivo-community/scholars-discovery/tree/master/solr/config). In order to represent a flatten document as a nested JSON response, the field values are indexed with a relationship identifier convention. ```[value]::[id]```, ```[value]::[id]::[id]```, etc. During serialization the document model is traversed parsing the Solr field value and constructing a nested JSON.

Here is a list of some dependencies used:

1. [Spring Boot](https://spring.io/projects/spring-boot)
   - [Spring Data REST](https://spring.io/projects/spring-data-rest)
   - [Spring Data for Apache Solr](https://spring.io/projects/spring-data-solr)
   - [Spring HATEOAS](https://spring.io/projects/spring-hateoas)
   - [Spring REST Docs](https://spring.io/projects/spring-restdocs)
2. [Apache Jena](https://jena.apache.org/)
3. [Apache Solr](https://lucene.apache.org/solr/)

## Configuration

The basic Spring Boot application configuration can be found at [src/main/resources/application.yml](https://github.com/vivo-community/scholars-discovery/blob/master/src/main/resources/application.yml). Here you be able to configure basic server and spring configuration as well as custom configuration for Scholars Discovery. There are several configuration POJOs to represent configurations. They can be found in [src/main/java/edu/tamu/scholars/middleware/config/model](https://github.com/vivo-community/scholars-discovery/tree/master/src/main/java/edu/tamu/scholars/middleware/config/model), and [src/main/java/edu/tamu/scholars/middleware/auth/config](https://github.com/vivo-community/scholars-discovery/tree/master/src/main/java/edu/tamu/scholars/middleware/auth/config).

## Assets

Assets are hosted at `/file/:id/:filename` and configured location `middleware.assets-location`.

Tested options are

Assets stored in src/main/resources/assets
```
middleware.assets-location: classpath:/assets
```

Assets stored in externally
```
middleware.assets-location: file:/scholars/assets
```

### Harvesting

Harvesting can be configured via ```middleware.harvesters``` and represented with [HarvesterConfig](https://github.com/vivo-community/scholars-discovery/blob/master/src/main/java/edu/tamu/scholars/middleware/config/model/HarvesterConfig.java). For each harvester, a bean will be created in which specifies the type of harvester and which document types it maps to. The reference implementation is the local triplestore harvester.

### Indexing

Indexing can be configured via ```middleware.indexers``` and represented with [IndexerConfig](https://github.com/vivo-community/scholars-discovery/blob/master/src/main/java/edu/tamu/scholars/middleware/config/model/IndexerConfig.java). For each indexer, a bean will be created in which specifies the type of indexer and which document types it indexes. The reference implementation is the solr indexer.

The application can be configured to harvest and index on startup, ```middleware.index.onStartup```, and via a cron schedule via ```middleware.index.cron```. The indexing is done in batch for performance. It can be tuned via ```middleware.index.batchSize```.

### Solr

Solr is configured via ```spring.data.solr```.

## Development Instructions

1. Install [Maven](https://maven.apache.org/install.html)
2. Install [Docker](https://docs.docker.com/install/)
3. Start Solr

```bash
   cd solr && docker build --tag=scholars/solr . && docker run -d -p 8983:8983 scholars/solr && cd ..
```

5. Build and Run the application

```bash
   mvn clean install
   mvn spring-boot:run
```

   - Note: Custom application configuration can be achieved by providing a location and an optional profile, such as:

```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.config.location=/some/directory/
```

   - ..where an `application-dev.yml` exists in the `/some/location/` directory

## Docker Deployment

```bash
docker build -t scholars/discovery .
```

```bash
docker run -d -p 9000:9000 -e SPRING_APPLICATION_JSON="{\"spring\":{\"data\":{\"solr\":{\"host\":\"http://localhost:8983/solr\"}}},\"ui\":{\"url\":\"http://localhost:3000\"},\"vivo\":{\"base-url\":\"http://localhost:8080/vivo\"},\"middleware\":{\"allowed-origins\":[\"http://localhost:3000\"],\"index\":{\"onStartup\":false},\"export\":{\"individualBaseUri\":\"http://localhost:3000/display\"}}}" scholars/discovery
```

> The environment variable `SPRING_APPLICATION_JSON` will override properties in application.yml.

## Docker Compose for Development

```bash
docker-compose up
```

This will provide Postgres database at localhost:5432 and Solr at localhost:8983. There should be two volume mounts at relative path `pgdata` and `solr/data`.

To run the `mvn spring-boot:run` command with `SPRING_APPLICATION_JSON` defined, you can use the following approach:

```
SPRING_APPLICATION_JSON='{"spring.datasource.driver-class-name":"org.postgresql.Driver","spring.datasource.url":"jdbc:postgresql://localhost:5432/scholars","spring.jpa.database-platform":"org.hibernate.dialect.PostgreSQLDialect","spring.sql.init.platform":"postgres"}' mvn spring-boot:run
```

Save the following as `config.json`.

```json
{
  "spring.datasource.driver-class-name": "org.postgresql.Driver",
  "spring.datasource.url": "jdbc:postgresql://localhost:5432/scholars",
  "spring.jpa.database-platform": "org.hibernate.dialect.PostgreSQLDialect",
  "spring.sql.init.platform": "postgres"
}
```

```
SPRING_APPLICATION_JSON=$(cat config.json) mvn spring-boot:run
```


For Windows Command Prompt, the syntax is slightly different:

```
set SPRING_APPLICATION_JSON={"spring.datasource.driver-class-name":"org.postgresql.Driver","spring.datasource.url":"jdbc:postgresql://localhost:5432/scholars","spring.jpa.database-platform":"org.hibernate.dialect.PostgreSQLDialect","spring.sql.init.platform":"postgres"} && mvn spring-boot:run
```

Run with schematize and indexing turned on (default):
```
set SPRING_APPLICATION_JSON={"spring.datasource.driver-class-name":"org.postgresql.Driver","spring.datasource.url":"jdbc:postgresql://localhost:5432/scholars","spring.jpa.database-platform":"org.hibernate.dialect.PostgreSQLDialect","spring.sql.init.platform":"postgres","middleware.index.schematize":true,"middleware.index.onStartup":true} && mvn spring-boot:run
```

Run with schematize and indexing turned off:
```
set SPRING_APPLICATION_JSON={"spring.datasource.driver-class-name":"org.postgresql.Driver","spring.datasource.url":"jdbc:postgresql://localhost:5432/scholars","spring.jpa.database-platform":"org.hibernate.dialect.PostgreSQLDialect","spring.sql.init.platform":"postgres","middleware.index.schematize":false,"middleware.index.onStartup":false} && mvn spring-boot:run
```

Run with schematize and indexing turned off and cache enabled:
```
set SPRING_APPLICATION_JSON={"spring.datasource.driver-class-name":"org.postgresql.Driver","spring.datasource.url":"jdbc:postgresql://localhost:5432/scholars","spring.jpa.database-platform":"org.hibernate.dialect.PostgreSQLDialect","spring.sql.init.platform":"postgres","middleware.index.schematize":false,"middleware.index.onStartup":false,"middleware.index.cacheEnabled":true} && mvn spring-boot:run
```

Run with schematize and indexing turned off and cache enabled and not clear cache on startup (default):
```
set SPRING_APPLICATION_JSON={"spring.datasource.driver-class-name":"org.postgresql.Driver","spring.datasource.url":"jdbc:postgresql://localhost:5432/scholars","spring.jpa.database-platform":"org.hibernate.dialect.PostgreSQLDialect","spring.sql.init.platform":"postgres","middleware.index.schematize":false,"middleware.index.onStartup":false,"middleware.index.cacheEnabled":true,"middleware.index.clearCache":false} && mvn spring-boot:run
```

Run with schematize and indexing turned off and cache enabled read-only:
```
set SPRING_APPLICATION_JSON={"spring.datasource.driver-class-name":"org.postgresql.Driver","spring.datasource.url":"jdbc:postgresql://localhost:5432/scholars","spring.jpa.database-platform":"org.hibernate.dialect.PostgreSQLDialect","spring.sql.init.platform":"postgres","middleware.index.schematize":false,"middleware.index.onStartup":false,"middleware.index.cacheEnabled":true,"middleware.index.cacheReadOnly":true} && mvn spring-boot:run
```

For Windows PowerShell:

```
$env:SPRING_APPLICATION_JSON='{"spring.datasource.driver-class-name":"org.postgresql.Driver","spring.datasource.url":"jdbc:postgresql://localhost:5432/scholars","spring.jpa.database-platform":"org.hibernate.dialect.PostgreSQLDialect","spring.sql.init.platform":"postgres"}'; mvn spring-boot:run
```

## Verify Installation

With the above installation instructions, the following service endpoints can be verified:

1. [HAL Explorer (9000/explorer)](http://localhost:9000)
2. [REST API (9000/individual)](http://localhost:9000/individual)
3. [REST API Docs (9000/api)](http://localhost:9000/api)

The [HAL(Hypertext Application Language)](https://docs.spring.io/spring-data/rest/docs/current/reference/html/#tools.hal-explorer) explorer can be used to browse scholars-discovery resources.
