package HurricaneEvacuation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Simulator {

    private static Graph graph;

    private static Scanner sc = new Scanner(System.in);
    private static Map<BeliefState, Integer> stateUtilityMap = new LinkedHashMap<>();

    public static void main(String[] args) {

        graph = new Graph(new File(args[0]));
        graph.constructGraph();

        createBeliefSpace();
        valueIteration();
        writeBeliefSpaceToFile();


        sc.close();
    }

    private static void valueIteration() {

        /* A snapshot of the map before the iteration*/
        Map<BeliefState, Integer> preMap= new LinkedHashMap<>(stateUtilityMap);
        preMap.forEach((beliefState, utility) -> {

            int max = 0;

            beliefState.getChildren().forEach((vertex, child) -> {

            });
        });
    }

    /**
     * Creates the belief state space
     */
    private static void createBeliefSpace() {

        BeliefState.createInitialStates().forEach(beliefState -> {
            stateUtilityMap.put(beliefState, 0);
            createBeliefSpace(beliefState);
        });

    }

    /**
     * This method recursively create descendant states until a terminal state
     * is reached (i.e. the deadline is reached).
     *
     * @param parent parent state
     */
    private static void createBeliefSpace(BeliefState parent) {

        if (parent.getTime() != Simulator.getGraph().getDeadline()) {
            parent.getLocation().getNeighbours().forEach((edge, vertex) -> {
                if (parent.getBlockedEdges().getOrDefault(edge.getId(), BlockedState.OPEN)
                        == BlockedState.OPEN) {
                    parent.createDescendants(edge, vertex).forEach(descendant -> {

                        int utility = 0;
                        if(descendant.getTime() == Simulator.getGraph().getDeadline()){
                            utility = descendant.getSaved();
                        }

                        stateUtilityMap.putIfAbsent(descendant, utility);
                        createBeliefSpace(descendant);
                    });
                }
            });
        }

    }

    private static void writeBeliefSpaceToFile() {

        Path file = Paths.get("output.txt");
        List<String> lines = new LinkedList<>();

        stateUtilityMap.forEach((beliefState, utility) ->
                lines.add("STATE: " + beliefState + " UTILITY: " + utility));
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static Graph getGraph() {
        return graph;
    }

}
