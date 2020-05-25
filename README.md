# Open Data Management Platform

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


## Hypothetical Architecture

![Architecture](/doc/architecture.jpg)

- Assume this all runs in Kubernetes or Docker Swarm.

## Possible monetization (if it catches):

The usual open-source model:

- Custom dev/integration (processors, adapters).

- Support

- Training

- Consulting

- Non-free plugins (see: elastic.co).
