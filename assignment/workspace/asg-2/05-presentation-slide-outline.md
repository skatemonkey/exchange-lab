# Presentation Slide Outline

## Presentation Goal

By the end of the presentation, the supervisor and assessors should understand why sustainable completed TPS must be measured across the complete transaction path, how the four configurations were tested, and why 55 TPS is the highest verified result for Exchange Lab.

## Main Slide Sequence

### Slide 1 — Project Title

Main message: Introduce the research topic and presenter.

Content:

- End-to-End Techniques for Improving Sustainable Throughput in High-Concurrency Java Microservices
- Student name and ID
- Programme and module
- Supervisor and lecturer

Visual: Minimal title slide using the project title and a subtle exchange-system background.

### Slide 2 — Many Techniques Can Improve TPS

Main message: High-concurrency systems offer many possible performance techniques, but their actual value depends on the system and workload.

Content:

- TPS can be improved through application concurrency, messaging, in-memory processing, caching, database optimisation, and resource scaling.
- Different techniques target different parts of the system and may produce very different gains.
- The project establishes a baseline, applies selected techniques one at a time, and measures the resulting TPS change.
- Research value: provide evidence for which selected techniques genuinely improve the tested system.

Visual: Performance options on the left and the baseline → one change → remeasure approach on the right.

### Slide 3 — Research Problem and Gap

Main message: Performance techniques are often evaluated separately, so an apparent improvement may only move the bottleneck.

Content:

- Java systems can be improved at the application, messaging, memory, cache, and database layers.
- The effect of a technique depends on the system and workload.
- The project tests each change on the same end-to-end transaction path under consistent conditions.
- Problem statement: Which improvements genuinely increase sustainable completed TPS?

Visual: One transaction path with successive bottlenecks highlighted.

### Slide 4 — Research Questions, Aim, and Objectives

Main message: The research establishes a baseline, measures each improvement, and identifies the highest sustainable TPS.

Content:

- RQ1: What TPS can the original system sustain?
- RQ2: Which techniques increase sustainable TPS, and by how much?
- RQ3: What is the highest sustainable TPS achieved?
- Aim: Maximise the sustainable TPS of the selected Java transaction-processing system.
- Objectives: Measure, select, apply, and evaluate the improvements.

Visual: Three research questions flowing into one research aim.

### Slide 5 — Exchange Lab Transaction Workflow

Main message: Exchange Lab provides a complete, verifiable transaction workflow modelled on limit-order processing.

Content:

- Submit a limit buy or sell order.
- Reserve cash or stock.
- Match compatible orders by price and time.
- Record the trade.
- Settle cash and stock balances.
- Use synthetic traders, balances, positions, and orders.

Visual: Submit → Reserve → Match → Record → Settle → Complete.

### Slide 6 — Experimental Design and Pass Rules

Main message: A rate passes only when throughput, stability, and correctness all pass together.

Content:

- Controlled quantitative comparison using the same machine, seed data, workload, and 30-second measurement window.
- Target TPS: requests configured in k6.
- Accepted TPS: orders accepted each second.
- Completed TPS: settlements completed each second.
- At least 98% of accepted asynchronous orders must complete during measurement.
- After draining: zero unfinished work and zero Kafka lag.
- No request failures; all SQL checks pass; C3 also passes Redis checks.

Visual: Three-part pass gate: Throughput + Stability + Correctness.

### Slide 7 — Four Staged Configurations

Main message: Each configuration adds one major performance technique to the preceding system.

Content:

- C0: Synchronous processing with MySQL.
- C1: Add Kafka between intake, matching, and settlement.
- C2: Add an in-memory price-time order book.
- C3: Add Redis-based cash and stock reservation.

Visual: Horizontal progression from C0 to C3, showing the added technique at each stage.

### Slide 8 — Experimental Procedure

Main message: Every measured rate follows the same reset, test, and verification process.

Content:

1. Prepare: stop applications, clear old consumers, reset state, seed data, and start fresh services.
2. Confirm readiness: health endpoints pass, Kafka partitions are assigned, and lag is zero.
3. Test: run k6 at the selected target rate for 30 seconds.
4. Drain and verify: record unfinished work, Kafka lag, SQL checks, and Redis checks where applicable.
5. Confirm: repeat the highest passing rate twice.

Visual: Prepare → Confirm readiness → Test → Drain and verify → Repeat.

### Slide 9 — Sustainable TPS Increased from 25 to 55

Main message: The staged improvements increased the highest sustainable target by 120%.

Content:

| Configuration | Highest passing target | Median completed TPS | Improvement |
|---|---:|---:|---:|
| C0: Synchronous MySQL | 25 TPS | 25.03 TPS | Baseline |
| C1: Kafka | 30 TPS | 30.00 TPS | +19.86% |
| C2: In-memory matching | 35 TPS | 35.00 TPS | +16.67% |
| C3: Redis reservation | 55 TPS | 54.93 TPS | +56.94% |

Visual: Bar chart using 25, 30, 35, and 55 TPS, with a 120% overall increase callout.

### Slide 10 — The Failed Boundaries Confirm the Scores

Main message: The next tested rate failed for every configuration, supporting the selected sustainable boundary.

Content:

| Configuration | Highest pass | Nearest failure | Reason for failure |
|---|---:|---:|---|
| C0 | 25 TPS | 30 TPS | Cash and stock conservation checks failed |
| C1 | 30 TPS | 35 TPS | 52.24% completed; unfinished matching work remained |
| C2 | 35 TPS | 40 TPS | 73.67% completed in the repeatability run |
| C3 | 55 TPS | 60 TPS | 96.78% completed, below the 98% requirement |

Visual: Pass/fail boundary chart or compact comparison table with the failure reason emphasized.

### Slide 11 — Each Improvement Moved the Bottleneck

Main message: The value of each technique became clear only after measuring the complete transaction path again.

Content:

- C0: Synchronous database processing reached its correctness boundary.
- C1: Kafka improved stage isolation, but database-backed matching could not keep pace at 35 TPS.
- C2: In-memory matching improved throughput, then financial processing became the constraint.
- C3: Redis reservation produced the largest gain and sustained 55 TPS.
- Reliability: identical workload and pass rules, fresh application runs, confirmation repetitions, and correctness checks.
- Limitation: one synthetic workload, one local machine, and 30-second measurement windows.

Visual: Bottleneck progression across C0 → C1 → C2 → C3.

### Slide 12 — Six-Week Research Plan

Main message: The work progresses from preparation and baseline measurement to implementation, evaluation, and submission.

Content:

- Week 1: Literature updates and system preparation.
- Week 2: Baseline measurement and technique selection.
- Weeks 3–4: Apply and test improvements.
- Week 5: Final TPS evaluation.
- Weeks 4–6: Report writing.
- Week 6: Review and submission.

Visual: Use the existing six-week Gantt chart from the proposal.

### Slide 13 — Final Answer and Questions

Main message: End-to-end measurement guided the system from a 25 TPS baseline to a verified 55 TPS result.

Content:

- RQ1: The original synchronous system sustained 25 TPS.
- RQ2: Kafka, in-memory matching, and Redis reservation produced measurable increases.
- RQ3: C3 achieved the highest verified target of 55 TPS.
- Overall improvement: 120% above the baseline target.
- Final takeaway: Optimise one bottleneck, measure the complete path again, and accept an improvement only when correctness and stability remain intact.
- Questions and discussion.

Visual: Large 25 TPS → 55 TPS result with the final takeaway beneath it.

## Source and Reference Plan

- Use the proposal as the primary source for the problem, methodology, results, discussion, and limitations.
- Add short citations to the research-background slides for Henning and Hasselbring (2024), Meijer et al. (2024), Navarro et al. (2023), Xing et al. (2025), and Zhang et al. (2024).
- Place the complete reference details in speaker notes or a backup reference slide when the PowerPoint is created.
- Keep detailed C0-C3 rate-search tables as backup slides rather than placing every run in the main presentation.
