package HurricaneEvacuation;

import java.io.File;
import java.util.*;

public class Simulator {

    private static Graph graph;

    static Scanner sc = new Scanner(System.in);
//    private static Evidence evidence = new Evidence();
//    private static BayesNetwork bayesNetwork = new BayesNetwork();


    public static void main(String[] args) {

        graph = new Graph(new File(args[0]));
        graph.constructGraph();

//        bayesNetwork.construct();

        interactWithUser();

        sc.close();
    }

    private static void interactWithUser() {

        boolean shouldQuit = false;

        while (!shouldQuit) {
            System.out.println("Choose an operation:");
            System.out.println("1. Add to evidence list.");
            System.out.println("2. Display evidence.");
            System.out.println("3. Reset evidence list.");
            System.out.println("4. Run probabilistic reasoning.");
            System.out.println("5. Display Bayesian network.");
            System.out.println("6. Quit.");

            int choice;

            try {
                choice = sc.nextInt();
            } catch (InputMismatchException e) {
                sc.next();
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {

                case 1:
                    evidence.add();
                    break;
                case 2:
                    evidence.display();
                    break;
                case 3:
                    evidence.reset();
                    break;
                case 4:
                    runProbabilisticReasoning();
                    break;
                case 5:
                    displayNetwork();
                    break;
                case 6:
                    shouldQuit = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    private static void runProbabilisticReasoning() {

        boolean shouldBreak = false;

        while (!shouldBreak) {

            System.out.println("Choose the type of goals to reason about:");
            System.out.println("1. Flood.");
            System.out.println("2. Blockage.");
            System.out.println("3. Evacuees.");
            System.out.println("4. Path.");
            System.out.println("5. Return.");

            int choice;

            try {
                choice = sc.nextInt();
            } catch (InputMismatchException e) {
                sc.next();
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {

                case 1:
                    reasonFloods();
                    break;
                case 2:
                    reasonBlockage();
                    break;
                case 3:
                    reasonEvacuees();
                    break;
                case 4:
                    LinkedHashMap<Integer, Edge> path = readPath();
                    reasonPath(new LinkedList<>(path.values()));
                    break;
                case 5:
                    shouldBreak = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
        System.out.println();
    }

    private static void reasonPath(LinkedList<Edge> path) {

        Probability probability = reasonPathRecurse(path);

        System.out.println("Open path probability:");
        System.out.println("(True:" + probability
                + ", False:" + Probability.complement(probability) + ")\n");

    }

    private static Probability reasonPathRecurse(List<Edge> path) {

        Probability result;
        if (path.isEmpty()) {
            result = new Probability(1);
        } else {

            Edge edge = path.get(0);

            Query edgeQuery = new Query(edge.getBlockageNode());
            Probability probability = edgeQuery.run().get(State.FALSE);

            evidence.extend(edge.getBlockageNode(), State.FALSE);
            result = Probability.multiply(probability,
                    reasonPathRecurse(path.subList(1, path.size())));
            evidence.remove(edge.getBlockageNode());
        }

        return result;

    }

    private static void reasonEvacuees() {

        graph.getVertices().forEach((id, vertex) -> {
            Query evacueesQuery = new Query(vertex.getEvacueesNode());
            Map<State, Probability> evacueesResult = evacueesQuery.run();
            System.out.println("Vertex " + id + ":");
            System.out.println("Evacuees: (True:" +
                    evacueesResult.get(State.TRUE)
                    + ", False:" + evacueesResult.get(State.FALSE) + ")\n");

        });
    }

    private static void reasonBlockage() {


        graph.getEdges().forEach((id, edge) -> {
            Query blockagesQuery = new Query(edge.getBlockageNode());
            Map<State, Probability> blockageResult = blockagesQuery.run();
            System.out.println("Edge " + id + ":");
            System.out.println("Blockage: (True:" +
                    blockageResult.get(State.TRUE)
                    + ", False:" + blockageResult.get(State.FALSE) + ")\n");
        });
    }

    private static void reasonFloods() {

        graph.getVertices().forEach((id, vertex) -> {

            Query floodQuery = new Query(vertex.getFloodNode());
            Map<State, Probability> floodResult = floodQuery.run();

            System.out.println("Vertex " + id + ":");
            System.out.println("Flood: (True:" +
                    floodResult.get(State.TRUE)
                    + ", False:" + floodResult.get(State.FALSE) + ")\n");
        });
    }

    private static LinkedHashMap<Integer, Edge> readPath() {
        System.out.println("Enter the path vertices by order," +
                " and press enter after each one.\n" +
                "Enter 0 to finish.");

        LinkedHashMap<Integer, Edge> path = new LinkedHashMap<>();
        LinkedHashMap<Integer, Vertex> chosen = new LinkedHashMap<>();
        int previousChoice = -1;

        for (int i = 0; i <= graph.getNumberOfEdges(); i++) {

            int choice;

            try {
                choice = sc.nextInt();
                if (choice == previousChoice) {
                    System.out.println("You chose the same vertex. This has no effect.");
                    i--;
                    continue;
                }
                if (choice > graph.getNumberOfVertices() || choice < 0) {
                    System.out.println("Not a vertex.");
                    i--;
                    continue;
                }
            } catch (InputMismatchException e) {
                sc.next();
                System.out.println("Invalid input.");
                i--;
                continue;
            }
            if (choice == 0) {
                break;
            } else {
                Vertex vertex = graph.getVertex(choice);
                if (i > 0) {

                    if (chosen.get(previousChoice).getNeighbours().containsKey(choice)) {
                        Edge edge = chosen.get(previousChoice).getNeighbours().get(choice).getValue();
                        if (path.get(edge.getId()) != null) {
                            System.out.println("Cannot repeat edges.");
                            i--;
                            continue;
                        }
                        path.put(edge.getId(), edge);
                    } else {
                        System.out.println("No edge between " + previousChoice
                                + " and " + choice);
                        i--;
                        continue;
                    }
                }

                chosen.put(choice, vertex);
                previousChoice = choice;
            }
        }
        System.out.println("Path " + chosen.values() + ":");
        System.out.println();
        return path;
    }

    private static void displayNetwork() {

        graph.getVertices().forEach((id, vertex) -> {
            System.out.println("VERTEX " + id + ":");
            vertex.getFloodNode().printCpt();
            System.out.println();
            vertex.getEvacueesNode().printCpt();
            System.out.println();
        });

        graph.getEdges().forEach((id, edge) -> {
            System.out.println("EDGE " + id + ":");
            edge.getBlockageNode().printCpt();
            System.out.println();
        });

        System.out.println("\n\n");
    }

    static Graph getGraph() {
        return graph;
    }

    static Evidence getEvidence() {
        return evidence;
    }

    static BayesNetwork getBayesNetwork() {
        return bayesNetwork;
    }
}
