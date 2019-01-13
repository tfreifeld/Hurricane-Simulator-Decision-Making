package HurricaneEvacuation;

import java.io.File;
import java.util.*;

public class Simulator {

    private static Graph graph;

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        graph = new Graph(new File(args[0]));
        graph.constructGraph();

        findOptimalPolicy();

        sc.close();
    }

    private static void findOptimalPolicy() {

        BeliefState state = new BeliefState();
        System.out.println(state);

        state.getLocation().getNeighbours().forEach((edge, vertex) -> {
            List<BeliefState> descendants = state.createDescendants(edge, vertex);
            descendants.forEach(System.out::println);

        });
    }


    static Graph getGraph() {
        return graph;
    }

}
