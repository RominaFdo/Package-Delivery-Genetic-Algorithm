import io.jenetics.Chromosome;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.PermutationChromosome;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.Factory;

public class PermutationGA {

    private static final double[][] DIST = {
        {0, 2, 9, 10, 7, 3},
        {2, 0, 6, 4, 8, 5},
        {9, 6, 0, 8, 3, 6},
        {10, 4, 8, 0, 5, 7},
        {7, 8, 3, 5, 0, 9},
        {3, 5, 6, 7, 9, 0}
    };

     private static double eval(Genotype<EnumGene<Integer>> gt) {
        Chromosome<EnumGene<Integer>> chromosome = gt.chromosome();
        int n = chromosome.length();

        double totalCost = 0;

         for (int i = 0; i < n; i++) {
            int from = chromosome.get(i).allele();
            int to = chromosome.get((i + 1) % n).allele(); // wrap back to start
            totalCost += DIST[from][to];
        }
        return totalCost; 
    }


     public static void main(String[] args) {
        int n  = 6;
        Factory<Genotype<EnumGene<Integer>>> gtf = Genotype.of(PermutationChromosome.ofInteger(n));

        Engine<EnumGene<Integer>, Double> engine = Engine
            .builder(PermutationGA::eval, gtf)
            .populationSize(100)
            .alterers(
                new PartiallyMatchedCrossover<>(0.8),
                new SwapMutator<>(0.15)
            )
            .minimizing()
            .build();


        EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

        // Run
        Genotype<EnumGene<Integer>> result = engine.stream()
            .limit(10)
            .peek(statistics)
            .collect(EvolutionResult.toBestGenotype());

        System.out.println("Best genotype (permutation): " + result);
        System.out.println("Best fitness (cost): " + eval(result));
        System.out.println();
        System.out.println(statistics);


    }

}