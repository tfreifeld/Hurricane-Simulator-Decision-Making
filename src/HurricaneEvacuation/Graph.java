package HurricaneEvacuation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class Graph {

    private static final String edgeEncoding = "#E[0-9]*";
    private static final String vertexEncoding = "#V";
    private static final String ignoreNonDigitsRegex = "[^0-9]*";
    private String deadlineEncoding = "#Deadline";
    private String startVertexEncoding = "#Start";
    private String shelterEncoding = "#Shelter";

    private Scanner sc;
    private HashMap<Integer, Vertex> vertices = new HashMap<>();
    private HashMap<Integer, Edge> edges = new HashMap<>();
    private int deadline;
    private Vertex startVertex;
    private Vertex shelterVertex;

    private List<Integer> possibleBlockedEdges = new ArrayList<>();
    private List<Integer> evacueeVertices = new ArrayList<>();

    Graph(File file) {

        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    void constructGraph() {

        readNumOfVertices();

        while (sc.hasNextLine()) {


            if (sc.hasNext(edgeEncoding)) {

                readEdge();

            } else if (sc.hasNext(vertexEncoding)) {

                readVertex();

            } else if (sc.hasNext(deadlineEncoding)) {

                readDeadline();

            } else if (sc.hasNext(startVertexEncoding)) {

                readStartVertex();

            } else if (sc.hasNext(shelterEncoding)) {

                readShelter();

            } else {

                throw new InputMismatchException("Unknown input parameter");

            }

            sc.nextLine();
        }

        sc.close();

    }

    private void readNumOfVertices() {

        if (sc.hasNext(vertexEncoding)) {

            sc.skip(vertexEncoding);
            int numOfVertices = sc.nextInt();
            if (numOfVertices > 10){
                throw new RuntimeException("There must be 10 vertices at most.");
            }
            for (int i = 1; i <= numOfVertices; i++) {
                vertices.put(i, new Vertex(i));
            }

            sc.nextLine();

        } else {
            throw new InputMismatchException("Missing number of vertices");
        }
    }

    private void readVertex() {
        sc.skip(vertexEncoding);
        int vertexNum = sc.nextInt();
        int evacuees = 0;
        if (sc.hasNext("P[0-9]*")) {
            sc.skip(ignoreNonDigitsRegex);
            int read = sc.nextInt();
            if (read > 4){
                throw new RuntimeException("Evacuees at each node must be less than 5");
            }
            else {
                evacuees = read;
                evacueeVertices.add(vertexNum);
            }
        }
        vertices.get(vertexNum).setEvacuees(evacuees);
    }

    private void readEdge() {
        sc.skip("#E");
        int edgeNum = sc.nextInt();
        int in = sc.nextInt();
        int out = sc.nextInt();
        sc.skip(ignoreNonDigitsRegex);
        int weight = sc.nextInt();

        float blockedProb = 0;
        if (sc.hasNext("B[0-9|.]*")) {
            if(possibleBlockedEdges.size() == 10){
                throw new RuntimeException("There must be 10 possible blockages at most.");
            }
            sc.skip(ignoreNonDigitsRegex);
            blockedProb = sc.nextFloat();
            possibleBlockedEdges.add(edgeNum);

        }

        Edge edge = new Edge
                (edgeNum, vertices.get(in), vertices.get(out), weight, blockedProb);
        edges.put(edgeNum, edge);
    }

    private void readDeadline() {
        sc.skip(deadlineEncoding);
        deadline = sc.nextInt();


    }

    private void readStartVertex() {
        sc.skip(startVertexEncoding);
        startVertex = vertices.get(sc.nextInt());
    }

    private void readShelter() {
        sc.skip(shelterEncoding);
        shelterVertex = vertices.get(sc.nextInt());
    }

    int getNumberOfVertices() {

        return vertices.size();

    }

    int getNumberOfEdges() {
        return edges.size();
    }

    HashMap<Integer, Vertex> getVertices() {
        return vertices;
    }

    HashMap<Integer, Edge> getEdges() {
        return edges;
    }

    Vertex getVertex(int index) {
        return vertices.getOrDefault(index, null);
    }


    Edge getEdge(int index) {
        return edges.getOrDefault(index, null);
    }

    int getDeadline() {
        return deadline;
    }

    Vertex getStartVertex() {
        return startVertex;
    }

    Vertex getShelterVertex() {
        return shelterVertex;
    }

    List<Integer> getPossibleBlockedEdges() {
        return possibleBlockedEdges;
    }

    List<Integer> getEvacueeVertices() {
        return evacueeVertices;
    }
}
