package HurricaneEvacuation;

import java.util.*;

/**
 * Implementation of belief states
 */
class BeliefState {


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
    BeliefState() {
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
    }

    /**
     * Copy constructor
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

    }


    /**
     * create descendant states stemming from a movement in the graph
     *
     * @param edge   traversed to the new location
     * @param target the new location
     * @return a list of descendant states
     */
    List<BeliefState> createDescendants(Edge edge, Vertex target) {

        List<BeliefState> ans = new ArrayList<>();

        /* template serves as a template for descendant states.
         * If the new location has no incident possibly blocked edges,
         * it will be the only descendant state. Otherwise, several
         * descendant states will be created.*/
        BeliefState template = new BeliefState(this, edge, target);

        Map<Integer, BlockedState> indexBlockedStateMap = new HashMap<>();

        blockedEdges.keySet().forEach(edgeId -> {
            if (target.getEdges().containsKey(edgeId))
                if (template.getBlockedEdges().get(edgeId) == BlockedState.UNKNOWN) {
                    indexBlockedStateMap.put(edgeId, BlockedState.UNKNOWN);
                }
        });

        /* Now indexBlockedStateMap contains the indices
         * of unknown possibly blocked edges incident on the new location*/

        if (!indexBlockedStateMap.isEmpty()) {

            ArrayList<Integer> keysList = new ArrayList<>(indexBlockedStateMap.keySet());

            fillStates(template, ans, indexBlockedStateMap,
                    keysList, 0);
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
    private void fillStates(BeliefState template, List<BeliefState> stateList,
                            Map<Integer, BlockedState> indexBlockedStateMap,
                            ArrayList<Integer> keysList, int counter) {

        if (counter == indexBlockedStateMap.size()) {
            /* All edges have been given a BlockedState */
            BeliefState newState = new BeliefState(template);
            indexBlockedStateMap.forEach((edgeId, blockedState) -> {
                newState.getBlockedEdges().replace(edgeId, blockedState);
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

   /* private void constructEntries(List<State> states, int counter){

        if (counter == parents.size()){
            this.cpt.add(states);
        }
        else{
            states.add(counter, State.BLOCKED);
            constructEntries(states, counter + 1);
            states.remove(counter);
            states.add(counter, State.OPEN);
            constructEntries(states, counter + 1);
            states.remove(counter);
        }


    }*/

    /**
     * Constructor for descendant states. Does not update blockedEdges fully -
     * createDescendants takes care of that.
     *
     * @param parent parent state
     * @param target new location
     */
    private BeliefState(BeliefState parent, Edge edge, Vertex target) {

        initialFromParent(parent);
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
    }

    /**
     * Initialize belief state fields (except from time) using the parent's values
     *
     * @param parent parent state
     */
    private void initialFromParent(BeliefState parent) {
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

    @Override
    public String toString() {
        return "{location: " + location + ", people: " + people + ", edges: "
                + blockedEdges + ", carrying: " + carrying + ", saved: " + saved + ", time: "
                + time + "}";
    }
}
