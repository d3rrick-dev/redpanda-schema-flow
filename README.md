# 🚀 redpanda-schema-flow

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-brightgreen?style=for-the-badge&logo=spring)
![Kafka](https://img.shields.io/badge/Redpanda-Kafka-red?style=for-the-badge&logo=redpanda)
![Avro](https://img.shields.io/badge/Avro-Schema_Registry-blue?style=for-the-badge&logo=apache)

> **Event-driven architecture at Guru level.** > A bulletproof implementation of Kafka + Avro + Schema Registry with an automated DLQ safety net. No more poison pills. No more broken downstream consumers.

## 🛠 Tech Stack
* **Engine:** [Redpanda](https://redpanda.com/) (Kafka-compatible, blazing fast)
* **Serialization:** [Apache Avro](https://avro.apache.org/) (Binary, schema-enforced)
* **Framework:** Spring Boot 3.3.x / Spring Kafka
* **Testing:** Testcontainers (Real Redpanda infra in Docker)
* **Automation:** GitHub Actions (CI/CD Pipeline)

---

## 🏗 Architecture
This project implements a **Schema-First** approach. The data contract is defined in `.avsc` files, ensuring that Producers and Consumers speak the exact same language.



### 💎 Key Features
* **Schema Evolution:** Backward-compatible field additions (e.g., `age`, `phoneNumber`).
* **Resilience:** `DeadLetterPublishingRecoverer` with 3-step retry backoff logic.
* **Type Safety:** Generated Java classes from Avro definitions.
* **Dynamic Testing:** Integration tests run against actual Redpanda binaries via Testcontainers.

---

## ⚖️ Coding Standards
* **Null Safety:** This project uses `@NonNullApi` at the package level.
* **Explicit Intent:** Fields in `.avsc` are **Required** by default. Use Avro Unions `["null", "type"]` ONLY when a business case for optionality exists.

## 🚀 Quick Start

### 1. Prerequisites
* Docker (Desktop / Colima / OrbStack)
* JDK 21+
* Maven

### 2. Generate Avro Classes
```bash
mvn clean compile