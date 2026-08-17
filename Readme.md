# Package Delivery Genetic Algorithm

A small Java project exploring **Jenetics**, a Java library for implementing Genetic Algorithms and Evolutionary Algorithms.

The project includes:

* Basic Jenetics usage
* Permutation-based chromosomes
* PMX crossover
* Swap mutation
* Fitness evaluation
* Evolution tracking
* Route optimization
* Visualization of evolutionary results

## Tech Stack

* Java
* Jenetics
* Maven
* Python
* Matplotlib

## Project Structure

```text
JeneticsDemo/
├── .gitignore
├── README.md
├── pom.xml
│
├── locations.csv
├── generation_stats.csv
├── best_route.csv
│
├── graph1_convergence.png
├── graph2_population_behaviour.png
├── graph3_route_map.png
│
├── plot_graphs.py
│
└── src/
    └── main/
        └── java/
            ├── Package_delivery_GA.java
            │
            └── practice/
                ├── HelloWorld.java
                ├── PermutationGA.java
                └──PermutationGA_Simple.java
```
###
Note: The generated CSV files and graphs contain the results of the evolutionary runs.
