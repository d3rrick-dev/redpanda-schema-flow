# 🚀 redpanda-schema-flow

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-brightgreen?style=for-the-badge&logo=spring)
![Kafka](https://img.shields.io/badge/Redpanda-Kafka-red?style=for-the-badge&logo=redpanda)
![Avro](https://img.shields.io/badge/Avro-Schema_Registry-blue?style=for-the-badge&logo=apache)

> **Real-time Event Streaming at Guru level.** > A bulletproof implementation of Kafka + Avro + Schema Registry with Stateful Streams, Interactive Queries, and an automated DLQ safety net.

## 🏗 Architecture
This project implements a **Stateful, Schema-First** architecture. It doesn't just transport data; it processes it in real-time, maintaining a "Source of Truth" directly inside the streaming engine using RocksDB.

### The Pipeline
1. **Producer:** Publishes Avro-encoded `User` events to `users-topic`.
2. **Kafka Streams Processor:** - **Stateless:** Filters data and transformation.
    - **Stateful:** Groups events by email domain and maintains a running count in a **KTable**.
3. **Interactive Queries:** Exposes internal Stream state (RocksDB) via a **REST API** for sub-millisecond analytics.
4. **Consumer:** Subscribes to `processed-users-topic` with a non-blocking retry strategy.
5. **Resilience:** `Main Topic` -> `Retry Topic (5s backoff)` -> `DLT`.



---

## Key Features
* **Stateful Processing:** Real-time aggregation of user signups by domain using `KTable` and `Materialized` state stores.
* **Interactive Queries:** Query the streaming state directly via HTTP without needing an external database like PostgreSQL.
* **Schema Enforcement:** Avro & Redpanda Schema Registry ensure 100% data contract compatibility.
* **Resilience & Retries:** Automated 4-attempt retry logic with a Dead Letter Topic (DLT) for "Poison Pill" handling.
* **Zero-Lag Tuning:** Configured `commit.interval.ms` and `cache.max.bytes` for near-instant analytics updates.
* **Testcontainers Integration:** Full end-to-end testing of the entire flow (Redpanda + Registry + Streams + API) in isolated Docker environments.

---

## Tech Stack
* **Engine:** [Redpanda](https://redpanda.com/) (Kafka-compatible, C++ powered performance)
* **Processing:** Kafka Streams (Stateless & Stateful DSL)
* **Storage:** RocksDB (Local state storage for Streams)
* **Serialization:** Apache Avro (Binary, schema-enforced)
* **Framework:** Spring Boot 3.3.x / Spring Kafka
* **Testing:** Testcontainers & Awaitility

---

## API Endpoints
The application exposes the internal state of the Kafka Stream through a REST interface:

`GET` `/analytics/domains/{domain}`  Returns the real-time signup count for a specific email domain.

---

##  Coding Standards
* **Avro First:** All data models are defined in `.avsc`. Java classes are generated at compile-time to ensure type safety.
* **Explicit Serdes:** Kafka Streams uses explicit `SpecificAvroSerde` to handle complex Avro types during repartitioning.
* **Wait-and-Verify:** Integration tests use `Awaitility` to handle the eventually consistent nature of distributed streaming state.

## Quick Start

### 1. Prerequisites
* Docker (Desktop / Colima / OrbStack)
* JDK 21+
* Maven

### 2. Run the full Pipeline
```bash
# Generate Avro classes and build
mvn clean compile

# Run the application
mvn spring-boot:run
```