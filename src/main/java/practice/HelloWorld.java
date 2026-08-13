import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

public class HelloWorld{

    // Fitness function
    private static int eval(Genotype<BitGene> gt) {
        return gt.chromosome()
                .as(BitChromosome.class)
                .bitCount();
    }


    public static void main(String[] args){

        // Create initial population template
        Factory<Genotype<BitGene>> gtf = 
                Genotype.of(
                    BitChromosome.of(50, 0.5)
                );

        // Create genetic algorithm engine
        Engine<BitGene, Integer> engine = 
            Engine.builder(
                HelloWorld::eval, 
                gtf
            )
            .build(); 

        // Run evolution for 100 generations
        Genotype<BitGene> result = 
            engine.stream()
            .limit(100)
            .collect(
                EvolutionResult.toBestGenotype()
            );

        System.out.println(
            "Hello World:\n" + result
        );

        BitChromosome chromosome = result.chromosome().as(BitChromosome.class);

        System.out.println("Chromosome : " + chromosome);
        System.out.println("Length     : " + chromosome.length());
        System.out.println("Fitness    : " + chromosome.bitCount());

    } 
}