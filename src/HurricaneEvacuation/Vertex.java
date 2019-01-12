package HurricaneEvacuation;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

class Vertex {

    private int id;

    private HashMap<Integer, Edge> edges;
    private HashMap<Integer, Map.Entry<Vertex, Edge>> neighbours;
    private int evacuees = 0;

    Vertex(int id) {
        this.id = id;
        this.edges = new HashMap<>();
        this.neighbours = new HashMap<>();
    }

    void submitEdge(Edge edge) {

        this.edges.put(edge.getId(), edge);
        Vertex neighbour = edge.getNeighbour(this);
        neighbours.put(neighbour.getId(),
                new AbstractMap.SimpleEntry<>(neighbour, edge));

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

    HashMap<Integer, Map.Entry<Vertex, Edge>> getNeighbours() {
        return neighbours;
    }
}

