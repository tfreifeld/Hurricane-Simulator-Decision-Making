package HurricaneEvacuation;

import java.util.*;

class Vertex {

    private int id;

    private HashMap<Integer, Edge> edges;
    private Map<Edge,Vertex> neighbours;
    private int evacuees = 0;

    Vertex(int id) {
        this.id = id;
        this.edges = new HashMap<>();
        this.neighbours = new HashMap<>();
    }

    void submitEdge(Edge edge) {

        this.edges.put(edge.getId(), edge);
        Vertex neighbour = edge.getNeighbour(this);
        neighbours.put(edge, neighbour);

    }


    HashMap<Integer, Edge> getEdges() {
        return edges;
    }

    int getId() {
        return id;
    }

    int getEvacuees() {
        return evacuees;
    }

    void setEvacuees(int evacuees) {
        this.evacuees = evacuees;
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }

    Map<Edge, Vertex> getNeighbours() {
        return neighbours;
    }
}

