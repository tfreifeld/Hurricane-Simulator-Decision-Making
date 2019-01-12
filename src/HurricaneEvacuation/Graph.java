package HurricaneEvacuation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

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
        if (sc.hasNext("P")) {
            sc.skip(ignoreNonDigitsRegex);
            evacuees = sc.nextInt();
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

        int blockedProb = 0;
        if (sc.hasNext("B")) {
            sc.skip(ignoreNonDigitsRegex);
            blockedProb = sc.nextInt();
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

    public int getDeadline() {
        return deadline;
    }

    public Vertex getStartVertex() {
        return startVertex;
    }

    public Vertex getShelterVertex() {
        return shelterVertex;
    }
}
