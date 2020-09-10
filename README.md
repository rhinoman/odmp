# Open Data Management Platform

See this [blog post](https://jamesadam.me/2020/09/08/my-lock-down-project-a-data-management-thing/) introducing the project

## Rationale:

- Eliminate bespoke data management systems being implemented for every organization/program/project and concomitant duplication of effort.

- It would be cool.

## Goals:

- Create a system that makes as few assumptions as possible about how users work with, store, process, and analyze data.

- Support integration with external tools (e.g., labeling) – it’s not possible to do everything, so don’t try.

- Key is a UI/UX that is intuitive and pleasant to use.  Intended audience is Data Scientists and ML practitioners -- NOT Software Engineers.

- The core system should be open source.

- Mostly the core services define interfaces and serve to manage the execution of user defined algorithms, processes, and tools.

- There are some commercial, closed-source solutions out there, most of which are very expensive.  But I’m unaware of any similar open-source solution.


## Architecture

![Architecture](/doc/architecture.jpg)

- Assume this all runs in Kubernetes or Docker Swarm.

## Early Screenshots

### Dataflow Index Screen
![Dataflow Index](/doc/screenshots/dataflow_index.jpg)

### Dataflow Screen
![Dataflow](/doc/screenshots/single_dataflow.jpg)

### Processor Editor Screen
![Processor Editor](/doc/screenshots/processor_editor.jpg)

## Developer Setup

The backend services are mostly written in Kotlin.  There may be cause to mix in other languages (most likely Python, R, and/or Clojure) in the future for individual processors, but the core services should remain Kotlin.

The frontend (opendmp-ui) is a re-frame application written in Clojurescript.  it uses Material-UI for the UI elements.

### Prerequisites

At a minimum, you'll need Docker CE installed as well as docker-compose to spin up the required dev resources.  The included docker-compose.yml in the root directory is intended for development use and starts dev instances of all the required support services.

For backend development:

- JDK 11
- Apache Maven

For frontend development:
- JDK 11
- lein
- npm
- shadow-cljs

### Keycloak setup

There is an `odmp-realm.json` file located under kc-data/ you can use to import the ODMP realm into Keycloak once it's running.  If you make changes intended to be permanent in the ODMP realm settings in Keycloak, please remember to re-export (you can use the `kc-data/export_realm.sh` script) before committing :)

### Mongo setup

There is a mongo-init.js script in mongo-data/ which should automatically run the first time mongo is started.  You may have to dump your docker volume if changes to the mongo schema are made.
