package HurricaneEvacuation;

import java.util.*;

/**
 * Implementation of belief states
 */
class BeliefState {

    private BeliefState parent;
    /* children is a mapping of target vertices to belief states resulting
    * from the action of going to those targets*/
    private Map<Vertex,List<BeliefState>> children = new HashMap<>();
    private Probability probability;

    /*** State variables ***/
    private Vertex location;
    /*A map from vertex id to whether there are people in that vertex*/
    private HashMap<Integer, Boolean> people;
    /*A map from edge id to whether that edge is blocked, unblocked or unknown*/
    private HashMap<Integer, BlockedState> blockedEdges;
    private int carrying;
    private int saved;
    private int time;


    /**
     * Constructor for initial state only
     */
    private BeliefState() {
        location = Simulator.getGraph().getStartVertex();

        people = new HashMap<>();
        Simulator.getGraph().getEvacueeVertices().forEach(
                vertexId -> people.put(vertexId, true)
        );

        blockedEdges = new HashMap<>();
        Simulator.getGraph().getPossibleBlockedEdges().forEach(
                edgeId -> blockedEdges.put(edgeId, BlockedState.UNKNOWN));

        carrying = 0;
        saved = 0;
        time = 0;

        probability = new Probability(1);
        parent = null;
        initChildren();
    }

    /**
     * Copy constructor (does not copy children)
     *
     * @param original to copy from
     */
    private BeliefState(BeliefState original) {

        location = original.getLocation();
        people = new HashMap<>(original.getPeople());
        blockedEdges = new HashMap<>(original.getBlockedEdges());
        carrying = original.getCarrying();
        saved = original.getSaved();
        time = original.getTime();

        probability = new Probability(original.getProbability());
        initChildren();
        submitParentChild(original.getParent());

    }

    /**
     * Constructor for a template for descendant states. Does not update blockedEdges and probability fully -
     * createInitialStates takes care of that.
     *
     * @param parent parent state
     * @param target new location
     */
    private BeliefState(BeliefState parent, Edge edge, Vertex target) {

        initializeFromParent(parent);
        time = parent.getTime() + edge.getWeight();

        if (time > Simulator.getGraph().getDeadline()) {
            /* If an attempt is made to cross an edge which breaches the deadline,
             * the new state will be identical to its parent, except the time will
             * be equal to the deadline. */
            time = Simulator.getGraph().getDeadline();

        } else {

            location = target;

            if (people.containsKey(target.getId()) && people.get(target.getId())) {
                people.replace(target.getId(), false);
                carrying += target.getEvacuees();
            }

            if (location.equals(Simulator.getGraph().getShelterVertex())) {
                saved += carrying;
                carrying = 0;
            }
        }

        probability = new Probability(parent.getProbability());
        this.parent = parent;
        initChildren();
    }

    private void initChildren() {
        children = new HashMap<>();
        location.getNeighbours().forEach((edge, vertex) -> {
            children.put(vertex, new ArrayList<>());
        });
    }

    /**
     * Submits a child state to its parent, and a parent state to its child
     * @param parent parent state
     */
    private void submitParentChild(BeliefState parent) {

        this.parent = parent;
        parent.getChildren().get(this.getLocation()).add(this);

    }

    /**
     * Create an initial state(s).
     *
     * @return a list of states
     */
    static List<BeliefState> createInitialStates() {

        return createStates(null, null, Simulator.getGraph().getStartVertex());
    }

    /**
     * Create descendant state(s) stemming from an action that will be made by the agent.
     *
     * @param edge   edge traversed
     * @param target new location
     * @return a list of descendant state(s)
     */
    List<BeliefState> createDescendants(Edge edge, Vertex target) {

        return createStates(this, edge, target);

    }

    /**
     * Create states from descending from parent, or an initial state(s).
     *
     * @param parent parent state, or null if initial state
     * @param edge   traversed to the new location, or null if initial state
     * @param target the target location
     * @return a list of states
     */
    private static List<BeliefState> createStates(BeliefState parent, Edge edge, Vertex target) {

        List<BeliefState> ans = new ArrayList<>();
        BeliefState template;

        /* template serves as a template for stochastic states.
         * If the target location has no incident possibly blocked edges,
         * it will be the only state. Otherwise, several
         * states will be created.*/

        if (edge == null) {
            template = new BeliefState();
        } else {
            template = new BeliefState(parent, edge, target);
        }

        Map<Integer, BlockedState> indexBlockedStateMap = new HashMap<>();

        template.getBlockedEdges().forEach((edgeId, blockedState) -> {
            if (blockedState == BlockedState.UNKNOWN)
                if (template.getLocation().getEdges().containsKey(edgeId)) {
                    indexBlockedStateMap.put(edgeId, BlockedState.UNKNOWN);
                }
        });

        /* Now indexBlockedStateMap contains the indices
         * of unknown possibly blocked edges incident on the new location*/

        if (!indexBlockedStateMap.isEmpty()) {

            ArrayList<Integer> keysList = new ArrayList<>(indexBlockedStateMap.keySet());

            fillStates(template, ans, indexBlockedStateMap,
                    keysList, 0);
        } else {
            ans.add(template);
        }

        return ans;
    }

    /**
     * This method recursively fills a list of states based on template, with every possible
     * true/false combination of the template state's incident unknown possibly blocked edges
     *
     * @param template             which the final list of descendant states is based on
     * @param stateList            the list to fill
     * @param indexBlockedStateMap a map of edge indices to BlockedStates
     * @param keysList             a list of keys  of indexBlockedStateMap to facilitate the process
     * @param counter              which counts how many edges have been assigned a BlockedState. Also
     *                             used as a stop condition.
     */
    private static void fillStates(BeliefState template, List<BeliefState> stateList,
                                   Map<Integer, BlockedState> indexBlockedStateMap,
                                   ArrayList<Integer> keysList, int counter) {

        if (counter == indexBlockedStateMap.size()) {
            /* All edges have been given a BlockedState */
            BeliefState newState = new BeliefState(template);
            indexBlockedStateMap.forEach((edgeId, blockedState) -> {

                newState.getBlockedEdges().replace(edgeId, blockedState);

                Probability probability = newState.getLocation()
                        .getEdges().get(edgeId).getBlockedProb();
                if (blockedState == BlockedState.OPEN) {
                    probability = probability.complement();
                }
                newState.multiplyProbability(probability);
            });


            stateList.add(newState);
        } else {
            indexBlockedStateMap.replace(keysList.get(counter), BlockedState.BLOCKED);
            fillStates(template, stateList, indexBlockedStateMap, keysList, counter + 1);
            indexBlockedStateMap.replace(keysList.get(counter), BlockedState.UNKNOWN);
            indexBlockedStateMap.replace(keysList.get(counter), BlockedState.OPEN);
            fillStates(template, stateList, indexBlockedStateMap, keysList, counter + 1);
            indexBlockedStateMap.replace(keysList.get(counter), BlockedState.UNKNOWN);

        }

    }

    /**
     * Initialize belief state fields (except from time) using the parent's values
     *
     * @param parent parent state
     */
    private void initializeFromParent(BeliefState parent) {
        location = parent.getLocation();
        people = new HashMap<>(parent.getPeople());
        blockedEdges = new HashMap<>(parent.getBlockedEdges());
        carrying = parent.getCarrying();
        saved = parent.getSaved();
    }

    /**
     * @return location of this belief state
     */
    Vertex getLocation() {
        return location;
    }

    HashMap<Integer, Boolean> getPeople() {
        return people;
    }

    HashMap<Integer, BlockedState> getBlockedEdges() {
        return blockedEdges;
    }

    int getCarrying() {
        return carrying;
    }

    int getSaved() {
        return saved;
    }

    int getTime() {
        return time;
    }

    BeliefState getParent() {
        return parent;
    }

    Map<Vertex, List<BeliefState>> getChildren() {
        return children;
    }

    Probability getProbability() {
        return probability;
    }


    private void multiplyProbability(Probability multiplier) {
        probability = probability.multiply(multiplier);
    }

    @Override
    public String toString() {
        return "{probability: " + probability + ", location: " + location + ", people: " + people + ", edges: "
                + blockedEdges + ", carrying: " + carrying + ", saved: " + saved + ", time: "
                + time + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, people, blockedEdges, carrying, saved, time);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BeliefState))
            return false;
        else {
            return location.equals(((BeliefState) obj).getLocation()) &&
                    people.equals(((BeliefState) obj).getPeople()) &&
                    blockedEdges.equals(((BeliefState) obj).getBlockedEdges()) &&
                    carrying == ((BeliefState) obj).getCarrying() &&
                    saved == ((BeliefState) obj).getSaved() &&
                    time == ((BeliefState) obj).getTime();
        }
    }
}
