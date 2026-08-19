# Literature Review Paper

**Title:** End-to-End Techniques for Improving Sustainable Throughput in High-Concurrency Java Microservices: A Literature Review

> **Contents**
>
> - [Abstract](#abstract)
> - [1. Introduction](#1-introduction)
>   - [1.1. Background and Context](#11-background-and-context)
>   - [1.2. Problem](#12-problem)
>   - [1.3. Aim and Scope](#13-aim-and-scope)
> - [2. Network and Traffic Management](#2-network-and-traffic-management)
>   - [2.1. Network Optimization](#21-network-optimization)
>   - [2.2. Load Balancing](#22-load-balancing)
> - [3. Java Application and Runtime](#3-java-application-and-runtime)
>   - [3.1. Java Request Processing](#31-java-request-processing)
>   - [3.2. JVM and Memory Management](#32-jvm-and-memory-management)
> - [4. Data and Messaging](#4-data-and-messaging)
>   - [4.1. Database Optimization](#41-database-optimization)
>   - [4.2. Local Caching and Redis](#42-local-caching-and-redis)
>   - [4.3. Kafka and Asynchronous Processing](#43-kafka-and-asynchronous-processing)
> - [5. Scaling, Protection, and Monitoring](#5-scaling-protection-and-monitoring)
>   - [5.1. Horizontal Scaling and Autoscaling](#51-horizontal-scaling-and-autoscaling)
>   - [5.2. Rate Limiting and Backpressure](#52-rate-limiting-and-backpressure)
>   - [5.3. Monitoring and Profiling](#53-monitoring-and-profiling)
> - [6. Conclusion and Future Research](#6-conclusion-and-future-research)
> - [References](#references)

> **Drafting rules:**
>
> - All blockquoted summaries are working notes for planning and quick reading. They will not appear in the final paper or table of contents.
> - Use a short introduction, bullet points for multiple characteristics, a comparison table where useful, and a brief critical synthesis.

## Abstract

High-concurrency Java microservices must sustain high transactions per second (TPS) while keeping latency, error rate, stability, and resource use within acceptable limits. This literature review examines the complete request path rather than treating Java threading as the only performance concern. It covers network communication and load balancing; Java request processing and JVM memory management; database access, local caching, Redis, and Kafka; and horizontal scaling, overload protection, monitoring, and profiling. No single technique guarantees high sustainable TPS. Each removes or controls a particular form of work, but its benefit depends on the workload and the remaining capacity of shared resources such as CPU, database connections, memory, Kafka partitions, and downstream services. Improving one layer may therefore move the bottleneck to another. End-to-end evidence remains limited because studies evaluate individual components using different applications, environments, workloads, and measurements. Future research should test combined techniques in one reproducible Java microservice system under steady traffic, bursts, sustained overload, and dependency failures. It should distinguish accepted TPS from completed TPS and measure tail latency, error rate, backlog, recovery, resource use, and operational cost.

**Keywords:** Java microservices; sustainable throughput; high concurrency; JVM optimization; distributed caching; Apache Kafka; autoscaling.

## 1. Introduction

> - **Background:** High TPS depends on the whole system—network, load balancer, Java/JVM, Redis, Kafka, and database—not only threads.
> - **Problem:** Improving one layer may move the bottleneck to another. There is no single solution for high TPS.
> - **Aim and Scope:** Review and compare techniques across all layers based on TPS, latency, reliability, and resource usage.

### 1.1. Background and Context

High-concurrency Java microservices aim to sustain high TPS while keeping latency, error rate, stability, and resource use within acceptable limits. Sustainable TPS means completed throughput maintained over time without uncontrolled queue growth or resource saturation. It depends on the complete request path—including the network, load balancers, Java application and JVM, Redis, Kafka, databases, and downstream services—not on thread management alone (Meijer et al., 2024).

### 1.2. Problem

No single optimization guarantees high TPS. Improving request processing can expose a database or network bottleneck; caching, messaging, or service replicas may move the limit elsewhere. Because studies often examine these workload-dependent techniques separately, the problem is determining how the layers work together and what performance, reliability, and complexity trade-offs they introduce (Henning & Hasselbring, 2024; Nunes et al., 2024).

### 1.3. Aim and Scope

This paper compares techniques across the network, load-balancing, Java/JVM, caching, messaging, database, scaling, protection, and monitoring layers. It evaluates sustainable TPS, latency, reliability, resource use, complexity, and cost, then identifies research gaps. It does not implement a solution.

## 2. Network and Traffic Management

> **Purpose:** Explain how network communication and load balancing affect end-to-end TPS.

### 2.1. Network Optimization

> - Reduce communication overhead between services.
> - Compare protocols, gateways, and service meshes under different workloads.
> - Identify when network optimization shifts the bottleneck elsewhere.

Network optimization reduces communication cost along the request path. Each inter-service call adds delay and processing through serialization, protocols, connections, gateways, or proxies. Four areas are central:

1. **Service calls and hops:** Remove unnecessary calls and avoid excessively long synchronous request chains.
2. **Payloads and serialization:** Reduce transferred data and choose a suitable serialization format.
3. **Protocols and connections:** Select communication protocols by workload and reuse connections where appropriate.
4. **Gateways and service meshes:** Balance their traffic-management benefits against additional processing and proxy overhead.

**Evidence comparison**

**Table 1.** *Comparison of Network Optimization Evidence*

| Study | Focus | Main finding | Limitation or implication |
|---|---|---|---|
| Kazanavičius and Mažeika (2023) | REST, RabbitMQ, Kafka, gRPC, and GraphQL | Relative performance changed with message size and structure. | No communication technology was best for every workload. |
| Meijer et al. (2024) | Gateway aggregation and offloading | Reducing downstream communication could improve performance. | Additional gateway work could move the CPU bottleneck to the gateway. |
| Zhu et al. (2023) | Service-mesh sidecar overhead | Some configurations substantially increased latency and CPU use. | The dominant overhead and value of optimizations depended on proxy mode and workload. |

**Critical synthesis**

Network techniques must be tested across the complete Java request path. A protocol that performs well alone may add little when a gateway, proxy, or downstream service becomes the bottleneck. Evaluation should combine TPS with tail latency, CPU and network use, message size, and reliability.

### 2.2. Load Balancing

> - Compare static and adaptive load-balancing approaches.
> - Explain how capacity and network latency guide request routing.
> - Evaluate their effects on TPS, tail latency, and errors.

Load balancing distributes work across instances and network paths. Equal distribution is insufficient when replicas differ in capacity, current load, or network latency. Four approaches are common:

1. **Static balancing:** Uses fixed rules such as round-robin and requires little monitoring.
2. **Resource-aware balancing:** Routes requests using CPU, memory, queue, or connection information.
3. **Latency-aware balancing:** Prefers instances or paths with better response times and network locality.
4. **Multi-cluster balancing:** Considers geographical latency, capacity, and data-transfer cost across clusters.

**Evidence comparison**

**Table 2.** *Comparison of Load-Balancing Evidence*

| Study | Approach | Main finding | Limitation |
|---|---|---|---|
| Nguyen et al. (2022) | Locality-, resource-, and delay-aware Kubernetes proxy | Maintained better throughput and latency than the tested default proxy modes as network delay increased. | Tested with four workers and a manually selected overload threshold. |
| Bachar et al. (2023) | Capacity-, latency-, and cost-aware multi-cluster selection | Improved response time and reduced outbound traffic cost relative to the evaluated mechanisms. | Assumed fixed placement and sufficiently accurate demand information. |
| Michaelis et al. (2024) | P99-latency and telemetry-based service-mesh balancing | Reduced P99 latency compared with round-robin in the tested workloads. | Periodic telemetry could miss short spikes and become stale. |

**Critical synthesis**

Adaptive balancing helps when capacity and network conditions are unequal, but it needs timely monitoring and adds control overhead. Tests should cover uneven requests, bursts, and failures while measuring throughput, P95/P99 latency, error rate, and resource distribution.

## 3. Java Application and Runtime

> **Purpose:** Explain how Java execution models and JVM memory management affect sustainable TPS.

### 3.1. Java Request Processing

> - Explain how Java handles CPU-bound and I/O-bound requests.
> - Introduce platform threads, virtual threads, and reactive processing.
> - Show when each approach is suitable and what still limits it.

A Java service performs two broad types of work. **CPU-bound work** uses the processor for calculation, validation, encryption, or serialization. **I/O-bound work** waits for databases, Redis, Kafka, files, networks, or other services. Most requests mix both, and the concurrency model determines how they are managed while running or waiting.

1. **Platform threads:** Platform threads are backed by operating-system threads and are normally managed through a bounded pool. For CPU-bound work, bounding the pool around the available processor capacity avoids excessive scheduling. However, a platform thread remains occupied during blocking I/O, so the pool can be exhausted when many requests wait simultaneously (Pressler & Bateman, 2023).
2. **Virtual threads:** Virtual threads are lightweight threads managed by the JVM. They allow blocking code to retain the familiar thread-per-request style while supporting many more waiting requests than a platform-thread pool. They improve concurrency for I/O-bound work, but they do not increase CPU speed or expand database connections, locks, and downstream capacity (Pressler & Bateman, 2023).
3. **Reactive processing:** Reactive applications use non-blocking operations, event loops, and asynchronous pipelines. A small number of threads can coordinate many waiting requests, making this approach suitable for I/O-heavy services and streaming. Its benefit is reduced when the call path contains blocking operations, and its asynchronous programming model can make code and debugging more complex (Zbarcea & Tudose, 2024).

The choice depends on the workload. CPU-bound work is limited by processor capacity, whereas I/O-bound services may benefit from virtual threads or reactive processing. Platform threads remain useful for bounded concurrency and simple execution. Every model can still be limited by database pools, memory, locks, downstream services, and other shared resources (Lašić et al., 2024; Navarro et al., 2023).

### 3.2. JVM and Memory Management

> - Explain the main JVM and memory components that affect application performance.
> - Connect each component to TPS, latency, and total memory usage.
> - Keep the focus on what a Java programmer should understand and examine.

The JVM executes Java code and manages the memory used by the application. Its runtime work consumes the same CPU and memory needed to process requests. Five components are particularly relevant to high-TPS services:

1. **Object allocation and retention:** High allocation creates garbage-collection work. Referenced objects cannot be collected, so leaks, oversized collections, and unbounded caches can fill the heap. Programmers should reduce unnecessary temporary objects and investigate retained objects.
2. **Heap sizing:** Too little heap causes frequent collection or an out-of-memory error; too much reduces the memory available for other service instances. The process must also fit its non-heap memory inside the container or machine limit.
3. **Garbage collection:** Parallel GC emphasizes throughput but may pause longer. G1 balances throughput and pause time, while ZGC and Shenandoah work more concurrently to shorten pauses. Concurrent collection still consumes CPU and memory, so shorter pauses do not guarantee higher TPS (Cai et al., 2022; Tavakolisomeh et al., 2023; Zhao et al., 2022).
4. **Non-heap memory:** Thread stacks, direct buffers, class metadata, the JIT code cache, native libraries, and other JVM structures consume memory outside the heap. Heap monitoring alone can therefore miss why a container exceeds its limit.
5. **JIT compilation and warm-up:** The just-in-time compiler optimizes frequently executed code while the application runs. Performance can be unstable before compilation. Load tests need a warm-up period, while startup and restart tests should also measure the unwarmed service (Khrabrov et al., 2022).

These components interact. A larger heap may reduce collection frequency but use more container memory; a low-pause collector may require more CPU; and lower allocation reduces collection work. JVM changes should therefore be tested with TPS and P99 latency alongside CPU, allocation rate, garbage collection, heap use, and total process memory.

## 4. Data and Messaging

> **Purpose:** Explain how databases, caching, and asynchronous messaging manage data and affect sustainable TPS.

The database, cache, and message broker serve different purposes. The database stores persistent data, a cache keeps temporary copies of reusable data, and Kafka carries work or events between services. Combining them can shorten the immediate request path and reduce repeated work, but each component also introduces capacity limits, consistency decisions, and failure cases (Nunes Laigner & Zhou, 2024; Raptis et al., 2024; Zhang et al., 2024).

### 4.1. Database Optimization

> - Explain how queries, connections, and transactions consume database capacity.
> - Introduce batching, pagination, replication, and partitioning.
> - Show why adding application instances does not automatically increase database TPS.

The database is a shared resource used by many concurrent requests. TPS becomes limited when requests spend too long executing queries, waiting for connections, or competing for the same data. Five areas are important:

1. **Queries and indexes:** A query should retrieve only the required rows and columns. Indexes help the database locate data without scanning an entire table, but every additional index also adds work to inserts and updates. Query execution plans can reveal full scans, expensive joins, unnecessary sorting, and indexes that are missing or unused.
2. **Connection pools:** Opening a new database connection for every operation is expensive, so Java services normally reuse connections from a pool. A small pool makes requests wait, while an oversized pool can send more concurrent work than the database can handle. The total number of connections from every service instance must remain within the database's capacity (Sobri et al., 2022).
3. **Transactions and locking:** Transactions keep related changes consistent. However, a transaction may hold locks that delay other requests accessing the same rows or tables. Transactions should contain only the necessary database work and should not remain open while the application performs unrelated processing or network calls.
4. **Batching and pagination:** Batching combines multiple inserts or updates into fewer database calls. Pagination limits how much data is returned at once. Both reduce repeated communication and resource usage, although very large batches or pages can still consume substantial memory and hold database resources for longer.
5. **Replication, partitioning, and sharding:** Read replicas can distribute read traffic, but replicated data may briefly lag behind the primary database. Partitioning divides a dataset into smaller parts; sharding distributes those parts across database nodes. Both can spread work, but cross-partition queries and transactions become more complicated.

Database optimization should first reduce the work and waiting time of each operation. Adding more Java service instances cannot improve sustainable TPS when they all compete for the same limited connections, locks, CPU, memory, or storage in the database (Lašić et al., 2024; Nunes Laigner & Zhou, 2024).

### 4.2. Local Caching and Redis

> - Explain how caching avoids repeated database queries, service calls, or calculations.
> - Compare a Java application's local cache with a shared Redis cache.
> - Cover expiration, invalidation, cache stampedes, and cache failure.

Caching stores reusable data closer to the application. A **cache hit** returns a stored value without repeating the original work. A **cache miss** still requires the database query, downstream call, or calculation. Caching is therefore most useful when the same data is requested frequently and does not change on every request (Zhang et al., 2024).

The main caching arrangements are:

**Table 3.** *Comparison of Local, Redis, and Two-Level Caching*

| Approach | How it works | Main benefit | Main trade-off |
|---|---|---|---|
| **Local cache** | Stores data inside each Java service instance. | Avoids a network call and usually provides the fastest lookup. | Every instance holds a separate copy, uses JVM memory, and loses the cache when it restarts. |
| **Redis cache** | Stores data in a shared in-memory service accessed over the network. | All service instances can reuse the same cached values. | Adds network and serialization work and makes Redis another service that must be scaled and protected. |
| **Two-level cache** | Checks a local cache first and Redis second. | Keeps the hottest values inside the application while sharing other values through Redis. | Uses more memory and makes expiration and invalidation harder to coordinate. |

A common **cache-aside** request works as follows:

1. The application checks the cache using a key.
2. A cache hit returns the stored value.
3. A cache miss reads the original data and places a copy in the cache.
4. When the original data changes, the application removes or refreshes the affected cache entry.

In this cache-aside design, the database remains the source of truth. Four issues determine whether the cached copy remains useful:

1. **Expiration and eviction:** A time to live removes an entry after a set period, while an eviction policy removes entries when cache memory is full. Short lifetimes create more misses; long lifetimes increase the chance of stale data.
2. **Invalidation:** Cached values must be removed or refreshed when their original data changes. This becomes difficult when several service responses depend on the same data or when multiple cache layers hold separate copies; stronger consistency requires additional coordination (Repin & Sidorov, 2025).
3. **Cache stampede:** When a popular entry expires, many requests may miss together and repeat the same database query. Single-flight loading or a per-key lock can let one request reload the value while others wait. Slightly varying expiration times also prevents many popular keys from expiring together.
4. **Cache failure:** The application needs a short timeout and a defined fallback when Redis is unavailable. Directly sending every failed cache request to the database can overload it, so fallback traffic may need limits or temporarily stale values where the data permits them.

Caching improves TPS only when the work avoided by cache hits is greater than the additional lookup, memory, consistency, and failure-handling costs. A high hit rate is useful only if the returned data is sufficiently current and the original database remains protected during cache failures.

### 4.3. Kafka and Asynchronous Processing

> - Explain how asynchronous processing moves suitable work outside the immediate request path.
> - Introduce Kafka's producers, brokers, topics, partitions, and consumers.
> - Connect parallelism, delivery, and consumer lag to sustainable TPS.

In synchronous processing, a request waits for the required downstream work to finish. This is necessary when the caller needs the completed result immediately. In asynchronous processing, the service publishes a message and another component processes it later. This can shorten the immediate request path and absorb temporary traffic bursts, but it does not remove the work; every queued message must still be processed (Kreps et al., 2011).

Kafka uses six main components:

**Table 4.** *Main Apache Kafka Components and Roles*

| Component | Role |
|---|---|
| **Producer** | Creates and publishes records to Kafka. |
| **Broker** | Receives, stores, and serves records. A Kafka cluster contains multiple brokers. |
| **Topic** | Groups records belonging to the same type of event or task. |
| **Partition** | Stores an ordered portion of a topic and allows the topic's work to be divided. |
| **Consumer** | Reads records and performs the required processing. |
| **Consumer group** | Shares partitions among consumers so that they can process the workload together. |

These components form Kafka's partitioned, replicated log model (Kreps et al., 2011).

Five details affect Kafka's throughput and reliability:

1. **Partitions and consumers:** Multiple partitions allow records to be produced and consumed in parallel. Within one consumer group, one partition is assigned to only one active consumer at a time. Adding more consumers than partitions therefore does not increase parallel processing (Raptis et al., 2024).
2. **Batching, compression, and message size:** Sending and reading records in batches reduces the cost per record, while compression reduces network and storage usage. Larger batches may add waiting time, and compression consumes CPU. Very large records also use more network, broker memory, storage, and processing time.
3. **Acknowledgments and replication:** The producer can wait for different levels of acknowledgment before treating a send as successful. Waiting for the required replicas gives better protection against message loss but requires more coordination than accepting the record without the same confirmation.
4. **Ordering and repeated processing:** Kafka preserves order within a partition, not across an entire multi-partition topic. Related records can use the same key to reach the same partition. Failures and retries may also cause a record to be processed more than once, so consumers should make repeated processing safe.
5. **Consumer lag and failure:** Consumer lag is the published work that consumers have not yet processed. Temporary lag allows Kafka to absorb a burst. Continuously increasing lag means that producers are creating work faster than consumers and their downstream dependencies can complete it.

Kafka can increase the number of requests an API accepts when suitable work is moved out of the immediate request. However, **accepted TPS** at the API is not the same as **completed TPS** at the consumers. Sustainable throughput requires the consumers to keep up over time and recover after bursts. Kafka can buffer a slow database or service, but it cannot increase that component's actual processing capacity (Henning & Hasselbring, 2024; Raptis et al., 2024).

## 5. Scaling, Protection, and Monitoring

> **Purpose:** Explain how a system adds capacity, protects itself from overload, and identifies the component that limits TPS.

Scaling, protection, and monitoring form a continuous loop. Scaling adds service capacity, protection keeps incoming work within system capacity, and monitoring shows whether completed TPS rises without an unacceptable latency or error rate (Nunes et al., 2024; Panahandeh et al., 2024; Xing et al., 2025).

### 5.1. Horizontal Scaling and Autoscaling

> - Explain how multiple service instances share traffic.
> - Compare the signals used to add or remove instances.
> - Cover startup delay and the limits imposed by shared dependencies.

Horizontal scaling runs multiple instances of the same service and distributes requests among them. It increases capacity when the work can be divided and the shared dependencies still have spare capacity. Autoscaling changes the number of instances as demand changes (Nunes et al., 2024).

A stateless service is easier to scale because any healthy instance can handle the next request. Required session or business state should not exist only inside one instance; it must be available through a database, Redis, or another suitable shared service. A local cache may still be used, but it should not be the only copy of required data.

An autoscaler needs a signal that represents the service's actual workload:

**Table 5.** *Autoscaling Signals and Their Limitations*

| Signal | Useful when | Main limitation |
|---|---|---|
| **CPU utilization** | The service performs CPU-bound work. | An I/O-bound service can be saturated while CPU usage remains low. |
| **Request rate or concurrency** | The cost of requests is reasonably consistent. | Different request types may consume very different amounts of work. |
| **Latency or error rate** | User-visible performance indicates overload. | These are often late signals because users are already affected. |
| **Queue depth or Kafka lag** | Workers process asynchronous or queued work. | More workers help only while partitions and downstream systems have capacity. |

New instances do not provide capacity immediately. The platform must schedule the container, start the JVM, pass readiness checks, open connections, and warm important code and caches. Minimum ready capacity can absorb ordinary bursts, while a cooldown period prevents the autoscaler from repeatedly adding and removing instances. Scale-down should also allow in-flight work to finish.

Horizontal scaling must be evaluated across the complete request path. Additional Java instances also create more database connections, Redis and Kafka traffic, and downstream calls. If one of those components is already saturated, adding replicas can worsen that bottleneck instead of increasing completed TPS (Raptis et al., 2024; Sobri et al., 2022).

### 5.2. Rate Limiting and Backpressure

> - Explain how the system controls incoming work before resources become saturated.
> - Distinguish rate limiting, bounded concurrency, backpressure, and load shedding.
> - Show how timeouts, circuit breakers, and controlled retries contain failures.

Every service has a maximum amount of work it can complete at one time. When incoming work exceeds that capacity, additional requests mainly create longer queues, consume memory, and hold threads or connections. Protection mechanisms limit unfinished work so that the system can continue completing useful requests during overload (Xing et al., 2025).

1. **Rate limiting:** A rate limiter controls how many requests are accepted during a period. Limits can apply to the whole API, one customer, or one operation. A token bucket, for example, accepts work at a controlled average rate while allowing a limited burst.
2. **Bounded queues and concurrency:** A short queue can absorb a temporary burst, but it needs a maximum size. A concurrency limit caps the number of requests that can simultaneously use an executor, database, or downstream service. At the limit, new work waits briefly or is rejected before consuming more resources.
3. **Backpressure:** Backpressure allows a slower component to tell its producer to slow down or pause. In a request chain this signal can travel toward the caller; in Kafka, growing consumer lag shows that processing is falling behind. The imbalance remains until production slows or consumer and downstream capacity increases.
4. **Timeouts, circuit breakers, and retries:** A timeout limits how long a caller waits for a dependency. A circuit breaker temporarily stops calls when repeated failures or timeouts show that the dependency is unhealthy. Retries may recover from temporary failures, but they need a small limit, delay, and backoff because uncontrolled retries add more work during an existing failure (Aderaldo et al., 2025).
5. **Load shedding:** Load shedding rejects excess work before expensive processing begins. Important operations can be protected by rejecting lower-priority requests or temporarily disabling optional work. The objective is to preserve useful completed work and acceptable latency, not to accept every request.

These mechanisms do not create processing capacity. They prevent the system from wasting CPU, memory, threads, connections, and queue space on work that is unlikely to finish successfully.

### 5.3. Monitoring and Profiling

> - Measure completed TPS, latency, errors, and resource saturation across the system.
> - Use traces to locate the slow component and profiling to inspect its internal work.
> - Confirm each optimization with a controlled load test.

Monitoring records the system's behavior over time, while profiling examines where a program spends CPU time, allocates memory, or waits. Neither increases TPS directly; they show whether the system is healthy and which component limits throughput. Different forms of evidence answer different questions:

**Table 6.** *Monitoring Evidence and Diagnostic Questions*

| Evidence | Examples | Question answered |
|---|---|---|
| **Metrics** | Completed TPS, P95/P99 latency, error rate, CPU, memory, queue length, GC, database waits, cache hits, and Kafka lag | Is a problem occurring, and which resource is becoming saturated? |
| **Logs** | Errors, retries, rejected requests, dependency failures, and structured request identifiers | What event or failure occurred? |
| **Distributed traces** | Timed spans across gateways, services, databases, Redis, Kafka, and other dependencies | Which part of an end-to-end request is slow? |
| **Java profiles** | CPU samples, object allocation, garbage collection, locks, blocked threads, and I/O waits | Which code or runtime activity consumes the service's resources? |
| **Load tests** | Realistic request mixtures, gradual load, bursts, sustained traffic, warm-up, and restart scenarios | At what load does the system stop sustaining its target performance? |

Start with user outcomes: completed TPS, tail latency, timeouts, and error rate. Correlated resource metrics then expose saturation, traces locate the responsible service or dependency, and a Java profiler identifies costly code or runtime activity (Panahandeh et al., 2024). Because profilers can produce different measurements, their results should be checked under a representative workload and repeated when necessary (Burchell et al., 2023).

Architecture can provide another clue. Across five microservice benchmarks, endpoints with higher coupling tended to respond more slowly, while systems with stronger dependency cycles supported fewer users (Avritzer et al., 2026). Because this evidence is correlational, coupling should guide investigation rather than be treated as proof of causation.

A practical bottleneck investigation follows five steps:

1. Create a realistic workload and allow the JVM to warm up.
2. Increase demand while measuring completed TPS, P99 latency, and error rate.
3. Find the resource, queue, or dependency that saturates when TPS stops increasing.
4. Use traces and Java profiling to locate the responsible request path and code.
5. Change one factor and repeat the same test to verify the end-to-end result.

The load generator must also have enough capacity; otherwise, the test measures the generator rather than the application. Optimization should begin only after an observed TPS or latency problem has been connected to a specific saturated component (Meijer et al., 2024).

## 6. Conclusion and Future Research

> - **Conclusion:** Sustainable TPS depends on the complete request path rather than one technology or optimization.
> - **Research gap:** Existing studies usually test individual components under different conditions, making end-to-end comparison difficult.
> - **Future research:** Evaluate combined techniques in one realistic and reproducible Java microservice system.

High sustainable TPS is produced by the complete system working together. Network and load-balancing decisions control how traffic reaches the services. Java concurrency and the JVM determine how requests use CPU and memory. Databases, caches, and Kafka manage persistent data, repeated work, and asynchronous processing. Scaling adds parallel capacity, while protection mechanisms and monitoring prevent overload and expose the next bottleneck. Improving one component may therefore move the limit somewhere else. The objective should be completed TPS with acceptable tail latency, errors, reliability, resource usage, and cost—not merely the largest number of accepted requests.

The reviewed literature provides useful evidence for individual techniques, but the evidence is fragmented. Studies use different applications, traffic patterns, hardware, JVM versions, frameworks, databases, test durations, and performance measurements. Many examine only one layer or report peak throughput without showing tail latency, errors, queue growth, recovery, or cost. It is therefore difficult to determine how the techniques interact in a complete Java microservice system. In particular, there is limited comparable evidence showing how Java request processing, JVM memory, database pools, Redis, Kafka, autoscaling, and overload protection behave together during sustained traffic, bursts, and partial failures.

Future research should build a reproducible Java microservice testbed that keeps the business workload and deployment environment consistent while varying the system configuration. It should:

1. Test steady traffic, sudden bursts, sustained overload, and dependency failures.
2. Compare individual and combined changes across the network, Java/JVM, database, caching, messaging, scaling, and protection layers.
3. Measure accepted and completed TPS, P95/P99 latency, error rate, queue and consumer lag, CPU, memory, garbage collection, database and cache activity, recovery time, and operational cost.

The aim should not be to declare one universal best architecture. It should identify which component becomes the bottleneck under each workload, how an optimization shifts that bottleneck, and which combination maintains the highest sustainable TPS within clearly defined reliability and resource limits.

## References

Aderaldo, C. M., Costa, T. M., Vasconcelos, D. M., Mendonça, N. C., Cámara, J., & Garlan, D. (2025). A declarative approach and benchmark tool for controlled evaluation of microservice resiliency patterns. *Software: Practice and Experience, 55*(1), 170–192. https://doi.org/10.1002/spe.3368

Avritzer, A., Janes, A., Rodrigues, H., Cai, Y., Schoyen, T., Pisch, E., Trubiani, C., Bondi, A. B., Menasché, D. S., & Zhang, C. (2026). Automated assessment of the relationship between microservice architectures and performance. *Journal of Systems and Software, 237*, Article 112857. https://doi.org/10.1016/j.jss.2026.112857

Bachar, D., Bremler-Barr, A., & Hay, D. (2023). Optimizing service selection and load balancing in multi-cluster microservice systems with MCOSS. In *2023 IFIP networking conference (IFIP Networking)* (pp. 1–9). IEEE. https://doi.org/10.23919/IFIPNetworking57963.2023.10186445

Burchell, H., Larose, O., Kaleba, S., & Marr, S. (2023). Don't trust your profiler: An empirical study on the precision and accuracy of Java profilers. In *Proceedings of the 20th ACM SIGPLAN international conference on managed programming languages and runtimes* (pp. 100–113). ACM. https://doi.org/10.1145/3617651.3622985

Cai, Z., Blackburn, S. M., Bond, M. D., & Maas, M. (2022). Distilling the real cost of production garbage collectors. In *2022 IEEE international symposium on performance analysis of systems and software (ISPASS)* (pp. 46–57). IEEE. https://doi.org/10.1109/ISPASS55109.2022.00005

Henning, S., & Hasselbring, W. (2024). Benchmarking scalability of stream processing frameworks deployed as microservices in the cloud. *Journal of Systems and Software, 208*, Article 111879. https://doi.org/10.1016/j.jss.2023.111879

Kazanavičius, J., & Mažeika, D. (2023). The evaluation of microservice communication while decomposing monoliths. *Computing and Informatics, 42*(1), 1–36. https://doi.org/10.31577/cai_2023_1_1

Khrabrov, A., Pirvu, M., Sundaresan, V., & de Lara, E. (2022). JITServer: Disaggregated caching JIT compiler for the JVM in the cloud. In *2022 USENIX annual technical conference (USENIX ATC 22)* (pp. 869–884). USENIX Association. https://www.usenix.org/conference/atc22/presentation/khrabrov

Kreps, J., Narkhede, N., & Rao, J. (2011). Kafka: A distributed messaging system for log processing. In *Proceedings of the 6th international workshop on networking meets databases (NetDB 2011)* (pp. 1–7). ACM. https://www.microsoft.com/en-us/research/wp-content/uploads/2017/09/Kafka.pdf

Lašić, L., Beronić, D., Mihaljević, B., & Radovan, A. (2024). Assessing the efficiency of Java virtual threads in database-driven server applications. In *2024 47th MIPRO ICT and electronics convention (MIPRO)* (pp. 2045–2050). IEEE. https://doi.org/10.1109/MIPRO60963.2024.10569754

Meijer, W., Trubiani, C., & Aleti, A. (2024). Experimental evaluation of architectural software performance design patterns in microservices. *Journal of Systems and Software, 218*, Article 112183. https://doi.org/10.1016/j.jss.2024.112183

Michaelis, O., Schmid, S., & Mostafaei, H. (2024). L3: Latency-aware load balancing in multi-cluster service mesh. In *Proceedings of the 25th international middleware conference* (pp. 49–61). ACM. https://doi.org/10.1145/3652892.3654793

Navarro, A., Ponge, J., Le Mouël, F., & Escoffier, C. (2023). Considerations for integrating virtual threads in a Java framework: A Quarkus example in a resource-constrained environment. In *Proceedings of the 17th ACM international conference on distributed and event-based systems* (pp. 103–114). ACM. https://doi.org/10.1145/3583678.3596895

Nguyen, Q.-M., Phan, L.-A., & Kim, T. (2022). Load-balancing of Kubernetes-based edge computing infrastructure using resource adaptive proxy. *Sensors, 22*(8), Article 2869. https://doi.org/10.3390/s22082869

Nunes, J. P. K. S., Nejati, S., Sabetzadeh, M., & Nakagawa, E. Y. (2024). Self-adaptive, requirements-driven autoscaling of microservices. In *2024 IEEE/ACM 19th symposium on software engineering for adaptive and self-managing systems (SEAMS)* (pp. 168–174). ACM. https://doi.org/10.1145/3643915.3644094

Nunes Laigner, R., & Zhou, Y. (2024). Benchmarking data management systems for microservices. In *2024 IEEE 40th international conference on data engineering (ICDE)* (pp. 5671–5672). IEEE. https://doi.org/10.1109/ICDE60146.2024.00467

Panahandeh, M., Hamou-Lhadj, A., Hamdaqa, M., & Miller, J. (2024). ServiceAnomaly: An anomaly detection approach in microservices using distributed traces and profiling metrics. *Journal of Systems and Software, 209*, Article 111917. https://doi.org/10.1016/j.jss.2023.111917

Pressler, R., & Bateman, A. (2023). *JEP 444: Virtual threads*. OpenJDK. https://openjdk.org/jeps/444

Raptis, T. P., Cicconetti, C., & Passarella, A. (2024). Efficient topic partitioning of Apache Kafka for high-reliability real-time data streaming applications. *Future Generation Computer Systems, 154*, 173–188. https://doi.org/10.1016/j.future.2023.12.028

Repin, V., & Sidorov, A. (2025). Distributed caching system with strong consistency model. *Frontiers in Computer Science, 7*, Article 1511161. https://doi.org/10.3389/fcomp.2025.1511161

Sobri, N. A. N., Abas, M. A. H., Yassin, I. M., Megat Ali, M. S. A., Md Tahir, N., Zabidi, A., & Rizman, Z. I. (2022). A study of database connection pool in microservice architecture. *International Journal on Informatics Visualization, 6*(2-2), 566–571. https://doi.org/10.30630/joiv.6.2-2.1094

Tavakolisomeh, S., Bruno, R., & Ferreira, P. (2023). BestGC: An automatic GC selector. *IEEE Access, 11*, 72357–72373. https://doi.org/10.1109/ACCESS.2023.3294398

Xing, J., Giannoukos, A., Loh, P., Wang, S., Qiu, J., Demoulin, H. M., Kallas, K., & Lee, B. C. (2025). Rajomon: Decentralized and coordinated overload control for latency-sensitive microservices. In *22nd USENIX symposium on networked systems design and implementation (NSDI 25)* (pp. 21–36). USENIX Association. https://www.usenix.org/conference/nsdi25/presentation/xing

Zbarcea, A., & Tudose, C. (2024). Migrating from developing asynchronous multi-threading programs to reactive programs in Java. *Applied Sciences, 14*(24), Article 12062. https://doi.org/10.3390/app142412062

Zhang, H., Kallas, K., Pavlatos, S., Alur, R., Angel, S., & Liu, V. (2024). MuCache: A general framework for caching in microservice graphs. In *21st USENIX symposium on networked systems design and implementation (NSDI 24)* (pp. 221–238). USENIX Association. https://www.usenix.org/conference/nsdi24/presentation/zhang-haoran

Zhao, W., Blackburn, S. M., & McKinley, K. S. (2022). Low-latency, high-throughput garbage collection. In *Proceedings of the 43rd ACM SIGPLAN international conference on programming language design and implementation* (pp. 76–91). ACM. https://doi.org/10.1145/3519939.3523440

Zhu, X., She, G., Xue, B., Zhang, Y., Zhang, Y., Zou, X. K., Duan, X., He, P., Krishnamurthy, A., Lentz, M., Zhuo, D., & Mahajan, R. (2023). Dissecting overheads of service mesh sidecars. In *Proceedings of the 2023 ACM symposium on cloud computing* (pp. 142–157). ACM. https://doi.org/10.1145/3620678.3624652
