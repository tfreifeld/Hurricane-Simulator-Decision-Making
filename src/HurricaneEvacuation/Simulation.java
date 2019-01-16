package HurricaneEvacuation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class Simulation {


    private HashMap<Integer, BlockedState> blockMap;
    private HashMap<Integer, Edge> edges;
    private BeliefState state;
    private boolean givenUp;
    private static boolean shouldPrint;

    private Simulation() {
        randomBlockages();
        if(shouldPrint) printBlockedEdges();
        findNextState(Main.getInitialStates());
        if(shouldPrint) printAgentStatus();

        givenUp = false;
    }

    private void runSimulation() {


        while (!state.isTerminal() && !givenUp) {
            Vertex newLocation = Main.getStateUtilityMap().get(state).getActionDst();
            findNextState(state.getChildren().get(newLocation));
            if(!givenUp && shouldPrint) {
                System.out.println("Go to vertex " + state.getLocation().getId());
                printAgentStatus();
            }



        }
    }

    private void findNextState(List<BeliefState> states) {

        if(states == null){
            if(shouldPrint) System.out.println("The agent has given up.");
            givenUp = true;
        }
        else {
            for (BeliefState state : states) {
                boolean isCorrect = true;
                for (Map.Entry<Integer, BlockedState> entry :
                        state.getBlockedEdges().entrySet()) {

                    Integer id = entry.getKey();
                    BlockedState blockedState = entry.getValue();

                    if (blockedState != BlockedState.UNKNOWN) {
                        if (blockedState != blockMap.get(id))
                            isCorrect = false;
                    }
                }
                if (isCorrect) {
                    this.state = state;
                    return;
                }
            }
        }
    }

    private void randomBlockages() {
        Random random = new Random();

        blockMap = new HashMap<>();
        edges = Main.getGraph().getEdges();

        Main.getGraph().getPossibleBlockedEdges().forEach(id -> {

            Probability probBlocked = edges.get(id).getBlockedProb();
            if (random.nextDouble() <= probBlocked.getProbability()) {
                edges.remove(id);
                blockMap.put(id, BlockedState.BLOCKED);
            } else {
                blockMap.put(id, BlockedState.OPEN);
            }
        });

    }

    private void printAgentStatus() {

        System.out.println("Location: " + state.getLocation().getId());
        System.out.println("Carrying: " + state.getCarrying());
        System.out.println("Saved: " + state.getSaved());
        System.out.println("Time: " + state.getTime());
        System.out.println();
    }

    private void printBlockedEdges() {
        System.out.print("Blocked edges: ");
        blockMap.forEach((id, blockedState) -> {
            if (blockedState == BlockedState.BLOCKED){
                System.out.print(id + " ");
            }
        });
        System.out.println();
    }

    static void makeSimulation(){

        shouldPrint = true;

        do {
            Simulation simulation = new Simulation();
            simulation.runSimulation();
            System.out.println("Run another simulation? (y/n)");
        } while (Main.sc.next("[y|n]").equals("y"));

        System.out.println("Bye!");

    }

    static void runTenThousand() {

        shouldPrint = false;

        do {
            double accumulatedScore = 0;

            for (int i = 0; i < 10000; i++) {

                Simulation simulation = new Simulation();
                simulation.runSimulation();
                accumulatedScore += simulation.state.getSaved();

            }
            System.out.println("average score: " + (accumulatedScore / 10000));

            System.out.println("Run again? (y/n)");

        } while (Main.sc.next("[y|n]").equals("y"));

        System.out.println("Bye!");
    }
}
