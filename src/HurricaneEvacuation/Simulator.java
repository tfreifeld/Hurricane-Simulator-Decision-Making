package HurricaneEvacuation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class Simulator {

    private static Graph graph;

    private static Scanner sc = new Scanner(System.in);
    private static Map<BeliefState, Optimal> stateUtilityMap = new LinkedHashMap<>();
    private static List<BeliefState> initialStates = new LinkedList<>();

    static DecimalFormat decimalFormat = new DecimalFormat("#.####");

    public static void main(String[] args) {

        graph = new Graph(new File(args[0]));
        graph.constructGraph();

        createBeliefSpace();
        valueIteration();
        writeBeliefSpaceToFile();
        writeOptimalPathToFile();


        sc.close();
    }


    private static void valueIteration() {

        var utilChangeRef = new Object() {
            boolean hasUtilityChanged;
        };

        do {

            utilChangeRef.hasUtilityChanged = false;

            Map<BeliefState, Optimal> preMap = new LinkedHashMap<>(stateUtilityMap);
            /* A snapshot of the map before the iteration*/
            writeBeliefSpaceToFile();
            preMap.keySet().forEach(beliefState -> {

                if (!beliefState.isTerminal()) {

                    Optimal optimalAction = new Optimal(0, null);
                    beliefState.getChildren().forEach((actionDst, children) -> {

                        /* This lambda computes the expected utility for an action*/

                        var euRef = new Object() {
                            double expectedUtility = 0.0;
                            // This is encapsulated so it can be assigned from inner lambda

                        };

                        children.forEach(childState -> {

                            Probability childProbability = computeChildProbability(actionDst, childState);
                            double childUtility = preMap.get(childState).utility;
                            euRef.expectedUtility += childUtility * childProbability.getProbability();

                        });

                        if (optimalAction.getUtility() < euRef.expectedUtility) {
                            optimalAction.setUtility(euRef.expectedUtility);
                            optimalAction.setActionDst(actionDst);
                        }
                    });

                    double changeInUtility =
                            Math.abs(optimalAction.getUtility() - preMap.get(beliefState).getUtility());
                    if (changeInUtility > 0) {
                        stateUtilityMap.replace(beliefState, optimalAction);
                        utilChangeRef.hasUtilityChanged = true;
                    }
                }
            });

        } while (utilChangeRef.hasUtilityChanged);

    }

    private static Probability computeChildProbability(Vertex actionDst, BeliefState childState) {

        Probability probability = new Probability(1);
        childState.getBlockedEdges().forEach((edgeId, blockedState) -> {

            /* This lambda method computes the probability that childState happens*/

            if (childState.getParent().getBlockedEdges().get(edgeId) == BlockedState.UNKNOWN) {

                /* Compute probability only if the last action revealed
                the edge's blockage status. Otherwise, the probability of the state remains 1.*/

                Edge edge = actionDst.getEdges().get(edgeId);
                if (!(edge == null)) {
                    Probability blockedProb = actionDst.getEdges().get(edgeId).getBlockedProb();
                    if (blockedState == BlockedState.BLOCKED) {
                        probability.multiply(blockedProb);
                    } else if (blockedState == BlockedState.OPEN) {
                        probability.multiply(blockedProb.complement());
                    }
                }
            }
        });

        return probability;
    }

    /**
     * Creates the belief state space
     */
    private static void createBeliefSpace() {

        BeliefState.createInitialStates().forEach(initialState -> {
            stateUtilityMap.put(initialState, new Optimal(0, null));
            initialStates.add(initialState);
            createBeliefSpace(initialState);
        });

    }

    /**
     * This method recursively create descendant states until a terminal state
     * is reached (i.e. the deadline is reached).
     *
     * @param parent parent state
     */
    private static void createBeliefSpace(BeliefState parent) {

        if (!parent.isTerminal()) {
            parent.getLocation().getNeighbours().forEach((edge, vertex) -> {
                if (parent.getBlockedEdges().getOrDefault(edge.getId(), BlockedState.OPEN)
                        == BlockedState.OPEN) {
                    /* If edge is a possibly blocked edge, but is open, or just a regular edge -
                     * create descendants stemming from crossing edge */
                    parent.createDescendants(edge, vertex).forEach(descendant -> {

                        int utility = 0;
                        if (descendant.isTerminal()) {
                            utility = descendant.getSaved();
                            /* If descendant is a terminal state, set its utility to be
                             * number of people saved */
                        }

                        stateUtilityMap.putIfAbsent(descendant, new Optimal(utility, null));
                        createBeliefSpace(descendant);
                        /* Recursively continue to create descendant's descendants */
                    });
                }
            });
        }

    }

    private static void writeBeliefSpaceToFile() {

        Path file = Paths.get("beliefSpace.txt");
        List<String> lines = new LinkedList<>();

        stateUtilityMap.forEach((beliefState, utility) ->
                lines.add("STATE: " + beliefState + ", ACTION: " + utility.getActionDstToString()
                        + ", UTILITY: " + decimalFormat.format(utility.getUtility())));

        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeOptimalPathToFile() {

        Path file = Paths.get("optimalPath.txt");
        List<String> lines = new LinkedList<>();

        lines.add("This file contains a detailed description only of states which are relevant to the agent's actions" +
                " in the optimal policy.\n");

        if (initialStates.size() > 1){

            initialStates.forEach(initialState -> {

                lines.add("INITIAL CASE:");
                lines.add("{");
                writeStateDetailed(lines, initialState);
                lines.add("}\n\n\n");
            });
        }
        else{
            writeStateDetailed(lines, initialStates.get(0));
        }


        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeStateDetailed(List<String> lines, BeliefState state) {
        lines.add("State: " + state.toString() + "\n");

        if (!state.isTerminal()) {

            lines.add("     Possible actions: ");
            state.getChildren().forEach((destination, children) -> {
                if (!children.isEmpty()) {
                    var euRef = new Object() {
                        double expectedUtility = 0;
                    };

                    lines.add("         Go to " + destination.getId() + ":");
                    lines.add("             Resulting states:");
                    lines.add("             {");

                    children.forEach(child -> {

                        Probability childProbability = computeChildProbability(destination, child);
                        double childUtility = stateUtilityMap.get(child).getUtility();
                        euRef.expectedUtility += childUtility * childProbability.getProbability();

                        lines.add("                 State: " + child.toString());
                        lines.add("                 Probability: " + childProbability);
                        lines.add("                 Utility: " + decimalFormat.format(childUtility) + "\n");
                    });

                    lines.add("             }");
                    lines.add("             Utility: " + decimalFormat.format(euRef.expectedUtility) + "\n");

                }
            });

            lines.add("     Chosen action: " + stateUtilityMap.get(state).getActionDstToString());

        }
        else{
            lines.add("     Terminal state");
        }
        lines.add("     Utility: " + decimalFormat.format(stateUtilityMap.get(state).getUtility()) + "\n");

        if(stateUtilityMap.get(state).getActionDst() != null) {
            /* Continue printing states only if the optimal action is not NoOp (or in a terminal state)*/

            state.getChildren().get(stateUtilityMap.get(state).
                    getActionDst()).forEach(child -> writeStateDetailed(lines, child));
        }
    }

    static Graph getGraph() {
        return graph;
    }

    /**
     * A simple class containing a utility and optimal action (in a form of vertex destination)
     */
    static class Optimal {

        private double utility;
        private Vertex actionDst;

        Optimal(double utility, Vertex actionDst) {
            this.utility = utility;
            this.actionDst = actionDst;
        }

        double getUtility() {
            return utility;
        }

        Vertex getActionDst() {
            return actionDst;
        }

        String getActionDstToString() {
            if (actionDst == null) {
                return "NoOp";

            }
            return "Go to vertex " + actionDst.toString();
        }

        void setUtility(double utility) {
            this.utility = utility;
        }

        void setActionDst(Vertex actionDst) {
            this.actionDst = actionDst;
        }
    }

}
