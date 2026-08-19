# Assignment 2 Project Proposal Skeleton

> Project direction: [Assignment 2 Project Starting Point](02-project-core-idea.md)

> Table of Contents
>
> - [0. Front Matter](#0-front-matter)
>   - [0.1. Title Page](#01-title-page)
>   - [0.2. Abstract](#02-abstract)
>   - [0.3. Table of Contents](#03-table-of-contents)
>   - [0.4. Lists](#04-lists)
> - [1. Introduction](#1-introduction)
> - [2. Research Background](#2-research-background)
>   - [2.1. What Existing Research Shows](#21-what-existing-research-shows)
>   - [2.2. What Is Missing](#22-what-is-missing)
>   - [2.3. How This Project Responds](#23-how-this-project-responds)
> - [3. Problem Statement](#3-problem-statement)
> - [4. Research Questions](#4-research-questions)
> - [5. Aim and Objectives](#5-aim-and-objectives)
>   - [5.1. Research Aim](#51-research-aim)
>   - [5.2. Research Objectives](#52-research-objectives)
> - [6. Scope of the Research](#6-scope-of-the-research)
> - [7. Significance of the Research](#7-significance-of-the-research)
> - [8. Research Methodology](#8-research-methodology)
>   - [8.1. Research Design](#81-research-design)
>   - [8.2. Experimental System and Setup](#82-experimental-system-and-setup)
>   - [8.3. Experimental Configurations](#83-experimental-configurations)
>   - [8.4. Experimental Procedure](#84-experimental-procedure)
>   - [8.5. Measurement and Data Analysis](#85-measurement-and-data-analysis)
>   - [8.6. Reliability, Validity, Ethics, and Limitations](#86-reliability-validity-ethics-and-limitations)
> - [9. Research Plan](#9-research-plan)
> - [10. Summary](#10-summary)
> - [11. References](#11-references)
> - [12. Appendices](#12-appendices)

The Abstract and Sections 1-10 are required. Lists, hypotheses, and appendices are included only when applicable.

## 0. Front Matter

### 0.1. Title Page

Use the confirmed details in the [Project Details section of `AGENTS.md`](../AGENTS.md#2-project-details):

- Project title
- Student name and student ID
- Intake code
- Degree and programme
- Module name and applicable module code
- Module lecturer
- Supervisor
- Submission date

### 0.2. Abstract

This is required front matter. Write it last as one paragraph of approximately 150-250 words. Summarize:

- Research problem and question
- Research aim
- Research design and methodology
- Data, workload, or sampling approach
- Evaluation and analysis procedures
- Expected contribution

### 0.3. Table of Contents

- Generate the final table of contents after completing the proposal.

### 0.4. Lists

Include only when applicable:

- List of Figures
- List of Tables
- List of Abbreviations, Symbols, or Terminology

## 1. Introduction

- Explain the need to process a high volume of transactions reliably.
- Explain that many areas of the system offer opportunities to increase TPS.
- State that maximising sustainable completed TPS is the main goal.
- Treat performance techniques as tools for increasing sustainable completed TPS. Bottleneck identification is one step in deciding where to apply them.
- Note that the general approach may transfer beyond Java, while the findings remain limited to the selected Java system.
- Briefly explain how the proposal is organized.

## 2. Research Background

### 2.1. What Existing Research Shows

- Summarize the main finding from previous research.
- Group related techniques instead of listing every technology.
- Use a small number of strong, recent references.

### 2.2. What Is Missing

- Explain that previous studies examine many useful techniques, but their results depend on the system, workload, and environment.
- Explain why the available evidence does not show how far TPS can be increased through a consistent end-to-end improvement process.

### 2.3. How This Project Responds

- Explain how the project will measure the baseline, identify improvement opportunities, apply relevant techniques, and retest repeatedly to increase sustainable completed TPS while checking latency, errors, stability, and resource usage.
- Connect the background directly to the Problem Statement.

## 3. Problem Statement

One-sentence problem statement:

> Many areas of a high-concurrency Java transaction-processing system can be improved to increase throughput. The problem is how to identify and apply the most effective improvements across the system to push sustainable completed TPS as high as possible.

Then explain:

- Why different areas of the system offer different improvement opportunities.
- Why the effectiveness of an improvement depends on the system and workload.
- Why improvements must be selected and measured across the complete system.
- Why performance techniques are tools rather than the goal of the research.
- How the proposed research will address the problem.
- Use recent literature to support the explanation.

## 4. Research Questions

For the purpose of this study, the following research questions are addressed:

1. RQ1: What TPS can the current system sustain before any improvements?
2. RQ2: Which applied techniques increase sustainable TPS, and by how much?
3. RQ3: What is the highest sustainable TPS achieved after applying the relevant improvements?

Add a hypothesis only if the final research design requires one.

## 5. Aim and Objectives

### 5.1. Research Aim

Research aim:

> To maximise the sustainable TPS of the selected Java transaction-processing system.

### 5.2. Research Objectives

Research objectives:

1. O1: To measure the system's current sustainable TPS.
2. O2: To identify suitable techniques for increasing TPS.
3. O3: To apply the selected techniques.
4. O4: To determine the highest sustainable TPS achieved.

Each objective must support the aim and correspond to at least one research question.

## 6. Scope of the Research

This research focuses on improving the sustainable TPS of one selected Java transaction-processing system. It will first establish the system's current TPS. Relevant performance techniques will then be applied and tested in the same environment. The study will determine the highest sustainable TPS achieved within the available project time and resources.

The work is limited to components available in the selected system and techniques that can be implemented during the project. The findings will explain what worked under the chosen workload and environment. They will not be treated as a universal configuration for every Java system.

## 7. Significance of the Research

This research matters because a system that sustains a higher TPS can complete more transactions during heavy demand. Measuring the complete system will show whether an improvement produces a real end-to-end gain rather than only making one component faster (Henning & Hasselbring, 2024; Meijer et al., 2024). The final result will show how far TPS was increased and the conditions under which that increase remained sustainable.

The practical value is a clear record of the changes that worked in the selected system. Developers can adapt the same measurement-based process when improving similar systems. The research also extends the Assignment 1 literature review by testing relevant techniques in a working system and producing experimental evidence.

## 8. Research Methodology

### 8.1. Research Design

- First define the simplified stock-exchange backend simulator, its limit-order workflow, and its boundaries before introducing the name Exchange Lab.
- Then explain the controlled quantitative design, the six sequential configurations, the fixed test conditions, the measurements, the validity rule, and why incremental comparison was selected.

### 8.2. Experimental System and Setup

- Briefly define the limit-order workflow used as the common experimental transaction.
- Include one business-flow diagram showing submission, reservation, matching, trade recording, settlement, and completion.
- Use one table for the fixed execution environment, platforms, test data, workload, and observation tools.
- State that exact machine, JVM, load, and timing values will be fixed and recorded before formal testing.

### 8.3. Experimental Configurations

- Give C0, C1, C2, and C3 separate short explanations and Mermaid diagrams using a consistent layout.
- Show synchronous MySQL processing in C0, Kafka in C1, in-memory matching in C2, and Redis reservation in C3.
- Treat the application as one exchange system throughout.
- Use historical Git versions or isolated worktrees rather than destructively changing the current system.

### 8.4. Experimental Procedure

- Begin with one three-phase overview: prepare the configuration, run the load test, and verify and record the result. Do not add comparison or optimisation to this flow.
- Under Phase 1, cover loading the version, resetting and seeding the data, health checks, warm-up, and recording the setup.
- Under Phase 2, cover the k6 workload, increasing request rates, the sustainable boundary, the drain period, and performance collection.
- Under Phase 3, cover SQL validation, rejection of invalid runs, three repeated tests under the same conditions, and result storage.

### 8.5. Measurement and Data Analysis

- Use a measurement table covering target, accepted, and completed TPS; completion ratio; latency and errors; backlog; resource use; and SQL correctness.
- State that completed TPS is calculated before the drain period and that post-drain counters are used only to identify unfinished work.
- Provide the formulas for completed TPS, completion ratio, and percentage improvement.
- Define a valid rate using correctness, failed requests or iterations, completed versus accepted TPS, backlog, and three repeated runs.
- Use a C0-C3 result-summary table and label the existing C3 110 TPS result as preliminary rather than final.

### 8.6. Reliability, Validity, Ethics, and Limitations


## 9. Research Plan

The project is planned over six months, as shown in the Gantt chart below.

![Provisional six-month Gantt chart for the research plan](assets/research-plan-gantt.svg)

*Figure 7. Provisional research plan. The schedule will be updated when the official project start and submission dates are confirmed.*

## 10. Summary

- Restate the research need.
- Restate the proposed contribution.
- Summarize the methodological approach.
- Reinforce the project's feasibility.
- Do not introduce new evidence or claims.

## 11. References

- Include only sources cited in the proposal.
- Use APA referencing consistently.
- Prioritize recent and reliable academic sources.

## 12. Appendices

Include when applicable:

- Gantt chart
- Methodology flowchart
- System architecture diagram
- Experimental configuration details
- Ethics documents
- Supporting instruments or materials
