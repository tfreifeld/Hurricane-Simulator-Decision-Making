package HurricaneEvacuation;

class Probability {

    private double probability;


    Probability(double probability) {

        if (probability < 0 || probability > 1) {
            throw new NotAProbability(probability);
        }
        this.probability = probability;
    }

    double getProbability() {
        return probability;
    }

    void multiply(Probability multiplier) {
        probability *= multiplier.getProbability();
    }

    Probability complement() {
        return new Probability(1 - this.getProbability());
    }

    class NotAProbability extends RuntimeException {

        NotAProbability(double probability) {
            super("Not a probability:" + probability);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(Main.decimalFormat.format(probability));
    }
}
