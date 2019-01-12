package HurricaneEvacuation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Probability {

    private double probability;

    private static DecimalFormat format = new DecimalFormat("#.####");


    Probability(double probability) {

        if (probability < 0 || probability > 1) {
            throw new NotAProbability(probability);
        }
        this.probability = probability;
    }

    double getProbability() {
        return probability;
    }

    static Probability multiply(Probability a, Probability b) {
        return new Probability(a.getProbability() * b.getProbability());
    }

    static Probability sum(Probability a, Probability b) {
        return new Probability(a.getProbability() + b.getProbability());
    }

    static Probability complement(Probability a) {
        return new Probability(1 - a.getProbability());
    }

    static List<Probability> normalize(Probability a, Probability b) {
        double sum = a.getProbability() + b.getProbability();
        Probability resultA = new Probability(a.getProbability() / sum);
        Probability resultB = new Probability(b.getProbability() / sum);
        return new ArrayList<>(Arrays.asList(resultA, resultB));
    }

    class NotAProbability extends RuntimeException {

        NotAProbability(double probability) {
            super("Not a probability:" + probability);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(format.format(probability));
    }
}
