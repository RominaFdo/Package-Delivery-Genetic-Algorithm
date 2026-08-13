import io.jenetics.Chromosome;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.PermutationChromosome;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

public class PermutationGA_Simple {
    
     private static double eval(Genotype<EnumGene<Integer>> gt) {
        Chromosome<EnumGene<Integer>> chromosome = gt.chromosome();

        double total = 0;
        for(int i = 0; i < chromosome.length(); i++){
            total += chromosome.get(i).allele();
        }
        return total;
    }

    public static void main(String[] args){
        int n = 9;
        Factory<Genotype<EnumGene<Integer>>> gtf = Genotype.of(PermutationChromosome.ofInteger(n));

        Engine<EnumGene<Integer>, Double> engine = Engine
            .builder(PermutationGA_Simple::eval, gtf)
            .populationSize(100)
            .alterers(
                new PartiallyMatchedCrossover<>(0.8),
                new SwapMutator<>(0.15)
            )
            .maximizing()
            .build();



         Genotype<EnumGene<Integer>> best = engine.stream()
            .limit(10)
            .collect(EvolutionResult.toBestGenotype());

        System.out.println("Best permutation found: " + best);
        System.out.println("Its  fitness: " + eval(best));
    }
}