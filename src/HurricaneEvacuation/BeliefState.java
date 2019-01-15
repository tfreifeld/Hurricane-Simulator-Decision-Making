package HurricaneEvacuation;

import java.util.*;

/**
 * Implementation of belief states
 */
class BeliefState {

    private BeliefState parent;
    /* children is a mapping of target vertices to belief states resulting
     * from the action of going to those targets (or NoOp) */
    private Map<Vertex, Set<BeliefState>> children = new HashMap<>();

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

        initChildren();
        this.parent = original.getParent();
        submitToParent();

    }

    /**
     * Constructor for a template for descendant states. Does not update blockedEdges and probability fully -
     * createInitialStates takes care of that.
     * Since this is a template, and not a completed state, it does not submit children.     *
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
        this.parent = parent;
        initChildren();
    }

    private void initChildren() {
        children = new HashMap<>();
        location.getNeighbours().forEach((edge, vertex) ->
                children.put(vertex, new HashSet<>()));

        /* A degenerate children list for action that breach the deadline */
        children.put(location, new HashSet<>());
    }

    /**
     * Submits a child state to its parent
     */
    private void submitToParent() {

        if (!(parent == null)) {
            /* Make sure this is not an initial state which has no parent */
            parent.getChildren().get(this.getLocation()).add(this);
        }
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
            /* If initial state */
            template = new BeliefState();
        } else {
            template = new BeliefState(parent, edge, target);
        }

        Map<Integer, BlockedState> unknownEdgesMap = new HashMap<>();

        template.getBlockedEdges().forEach((edgeId, blockedState) -> {
            if (blockedState == BlockedState.UNKNOWN)
                if (template.getLocation().getEdges().containsKey(edgeId)) {
                    unknownEdgesMap.put(edgeId, BlockedState.UNKNOWN);
                }
        });

        /* Now unknownEdgesMap contains the indices
         * of unknown possibly blocked edges incident on the new location*/

        if (!unknownEdgesMap.isEmpty()) {
            /* If the new location is incident with unknown possibly blocked edges */
            ArrayList<Integer> keysList = new ArrayList<>(unknownEdgesMap.keySet());

            fillEdgeVarsInStates(template, ans, unknownEdgesMap,
                    keysList, 0);
        } else {
            ans.add(template);
            template.submitToParent();

        }

        return ans;
    }

    /**
     * This method recursively fills a list of states based on template, with every possible
     * true/false combination of the template state's incident unknown possibly blocked edgeStateMap
     *
     * @param template     which the final list of descendant states is based on
     * @param stateList    the list to fill
     * @param edgeStateMap a map of edge indices to BlockedStates
     * @param keysList     a list of keys  of edgeStateMap to facilitate the process
     * @param counter      which counts how many edgeStateMap have been assigned a BlockedState. Also
     *                     used as a stop condition.
     */
    private static void fillEdgeVarsInStates(BeliefState template, List<BeliefState> stateList,
                                             Map<Integer, BlockedState> edgeStateMap,
                                             ArrayList<Integer> keysList, int counter) {

        if (counter == edgeStateMap.size()) {
            /* All edgeStateMap have been given a BlockedState */

            BeliefState newState = new BeliefState(template);
            edgeStateMap.forEach((edgeId, blockedState) ->
                    newState.getBlockedEdges().replace(edgeId, blockedState));

            stateList.add(newState);

        } else {
            edgeStateMap.replace(keysList.get(counter), BlockedState.BLOCKED);
            fillEdgeVarsInStates(template, stateList, edgeStateMap, keysList, counter + 1);
            edgeStateMap.replace(keysList.get(counter), BlockedState.UNKNOWN);
            edgeStateMap.replace(keysList.get(counter), BlockedState.OPEN);
            fillEdgeVarsInStates(template, stateList, edgeStateMap, keysList, counter + 1);
            edgeStateMap.replace(keysList.get(counter), BlockedState.UNKNOWN);

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

    private HashMap<Integer, Boolean> getPeople() {
        return people;
    }

    HashMap<Integer, BlockedState> getBlockedEdges() {
        return blockedEdges;
    }

    private int getCarrying() {
        return carrying;
    }

    int getSaved() {
        return saved;
    }

    private int getTime() {
        return time;
    }

    BeliefState getParent() {
        return parent;
    }

    Map<Vertex, Set<BeliefState>> getChildren() {
        return children;
    }


    @Override
    public String toString() {
        return "{location: " + location + ", people: " + people + ", edges: "
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

    boolean isTerminal() {
        return time == Simulator.getGraph().getDeadline();
    }
}
