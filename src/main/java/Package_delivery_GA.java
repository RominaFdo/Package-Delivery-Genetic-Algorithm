import java.util.List;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import io.jenetics.Chromosome;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.PermutationChromosome;
import io.jenetics.SwapMutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

public class Package_delivery_GA {

    // DATASET

    static final double[][] LOCATIONS = {
            {0, 0},     // 0: Depot
            {2, 3},     // 1
            {5, 4},     // 2
            {1, 7},     // 3
            {6, 8},     // 4
            {9, 5},     // 5
            {8, 1},     // 6
            {4, 1},     // 7
            {3, 8},     // 8
            {9, 9},     // 9
            {6, 2}      // 10
    };

    // 20 packages, two per location (locations 1-10).
    // packageDestinations[k] = which location package k is going to.
    static final int[] PACKAGE_DESTINATIONS = {
            1, 1,
            2, 2,
            3, 3,
            4, 4,
            5, 5,
            6, 6,
            7, 7,
            8, 8,
            9, 9,
            10, 10
    };

    // DIST[a][b] = Euclidean distance between location a and location b
    static final double[][] DIST = buildDistanceMatrix(LOCATIONS);

    private static double[][] buildDistanceMatrix(double[][] loc) {
        int n = loc.length;
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double dx = loc[i][0] - loc[j][0];
                double dy = loc[i][1] - loc[j][1];
                dist[i][j] = Math.sqrt(dx * dx + dy * dy);
            }
        }
        return dist;
    }

    // 1. PROBLEM REPRESENTATION
    //   Chromosome = permutation of the 10 delivery locations (1..10).
    //   Gene at position i = which location is visited at stop i overall.
    //   Split: first 5 genes -> Vehicle 1's route (in order)
    //          last  5 genes -> Vehicle 2's route (in order)
    //   Both vehicles start and end at the depot (location 0).
    //   Fitness = total distance driven by BOTH vehicles (minimize).

    private static double eval(Genotype<EnumGene<Integer>> gt) {
        Chromosome<EnumGene<Integer>> c = gt.chromosome();
        int n = c.length();
        int half = n / 2;
        return routeDistance(c, 0, half) + routeDistance(c, half, n);
    }

    private static double routeDistance(Chromosome<EnumGene<Integer>> c, int start, int end) {
        double d = 0;
        int prev = 0; // depot
        for (int i = start; i < end; i++) {
            int loc = c.get(i).allele();
            d += DIST[prev][loc];
            prev = loc;
        }
        d += DIST[prev][0]; // back to depot
        return d;
    }

    public static void main(String[] args) throws IOException {

        // 2. Evoultionary Algorithm configuration

        Factory<Genotype<EnumGene<Integer>>> gtf = Genotype.of(PermutationChromosome.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
 
        Engine<EnumGene<Integer>, Double> engine = Engine
            .builder(Package_delivery_GA::eval, gtf)
            .populationSize(100)                        // population size
            .selector(new TournamentSelector<>(5))       // selection mechanism
            .alterers(
                new PartiallyMatchedCrossover<>(0.8),    // PMX crossover, p=0.8
                new SwapMutator<>(0.15)                  // Swap mutation, p=0.15
            )
            .minimizing()
            .build();

        final int MAX_GENERATIONS = 200; // maximum number of generations to run
        
        // 3. Run the evolution, recording best and average fitness per generation
        List<String> statsRows = new ArrayList<>();
        statsRows.add("generation,bestFitness,avgFitness");

        Genotype<EnumGene<Integer>> best = engine.stream()
            .limit(MAX_GENERATIONS)
            .peek(result -> {
                double bestFit = result.bestFitness();
                double avgFit = result.population().stream()
                        .mapToDouble(pt -> pt.fitness())
                        .average()
                        .orElse(0);
                statsRows.add(result.generation() + "," + bestFit + "," + avgFit);
            })
            .collect(EvolutionResult.toBestGenotype());

        try (PrintWriter pw = new PrintWriter(new FileWriter("generation_stats.csv"))) {
            for (String row : statsRows) pw.println(row);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("locations.csv"))) {
            pw.println("id,x,y");
            for (int i = 0; i < LOCATIONS.length; i++) {
                pw.println(i + "," + LOCATIONS[i][0] + "," + LOCATIONS[i][1]);
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("best_route.csv"))) {
            pw.println("vehicle,stop_order,location_id");
            Chromosome<EnumGene<Integer>> c = best.chromosome();
            int half = c.length() / 2;
            for (int i = 0; i < half; i++) {
                pw.println("1," + i + "," + c.get(i).allele());
            }
            for (int i = half; i < c.length(); i++) {
                pw.println("2," + (i - half) + "," + c.get(i).allele());
            }
        }

        double bestDistance = eval(best);
        Chromosome<EnumGene<Integer>> bestChromosome = best.chromosome();
        int half = bestChromosome.length() / 2;

        System.out.println("=== Best Delivery Plan Found ===");
        System.out.println("Full chromosome: " + best);
        System.out.println("Vehicle 1 route: " + routeToString(bestChromosome, 0, half));
        System.out.println("Vehicle 2 route: " + routeToString(bestChromosome, half, bestChromosome.length()));
        System.out.println("Total travel distance: " + bestDistance);
        System.out.println("Generations run: " + (statsRows.size() - 1));

        System.out.println();
        System.out.println("=== Package Allocation ===");
        for (int pkg = 0; pkg < PACKAGE_DESTINATIONS.length; pkg++) {
            int destLocation = PACKAGE_DESTINATIONS[pkg];
            int vehicle = locationServedByVehicle(bestChromosome, half, destLocation);
            System.out.println("Package " + (pkg + 1) + " -> Location " + destLocation
                    + " -> Vehicle " + vehicle);
        }

        System.out.println();
        System.out.println("Wrote generation_stats.csv, locations.csv, best_route.csv");
        System.out.println("Run plot_graphs.py next to generate the 3 graphs.");
    }

    private static String routeToString(Chromosome<EnumGene<Integer>> c, int start, int end) {
        StringBuilder sb = new StringBuilder("Depot -> ");
        for (int i = start; i < end; i++) {
            sb.append("Loc").append(c.get(i).allele()).append(" -> ");
        }
        sb.append("Depot");
        return sb.toString();
    }

    // Returns 1 or 2 depending on which vehicle's route (first half vs second half of the chromosome) contains the given location.
    private static int locationServedByVehicle(Chromosome<EnumGene<Integer>> c, int half, int location) {
        for (int i = 0; i < half; i++) {
            if (c.get(i).allele() == location) return 1;
        }
        return 2;
    }
}