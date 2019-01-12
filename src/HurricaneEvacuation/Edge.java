package HurricaneEvacuation;

import java.util.ArrayList;
import java.util.List;

class Edge {

    private int id;
    private List<Vertex> vertices = new ArrayList<>(2);
    private int weight;
    private Probability blockedProb;

    Edge(int id, Vertex in, Vertex out, int weight, float blockedProb) {

        this.id = id;
        this.vertices.add(in);
        this.vertices.add(out);
        this.weight = weight;
        this.blockedProb = new Probability(blockedProb);

        in.submitEdge(this);
        out.submitEdge(this);
    }

    List<Vertex> getVertices() {

        return vertices;
    }

    Probability getBlockedProb() {
        return blockedProb;
    }

    Vertex getNeighbour(Vertex vertex) {
        if (vertex.equals(vertices.get(0))) {
            return vertices.get(1);
        } else {
            return vertices.get(0);
        }
    }

    int getWeight() {
        return weight;
    }

    int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }
}
