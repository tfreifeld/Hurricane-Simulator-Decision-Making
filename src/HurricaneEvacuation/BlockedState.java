package HurricaneEvacuation;

enum BlockedState {

    OPEN(-1), UNKNOWN(0), BLOCKED(1);

    private int state;
    BlockedState(int state){
        this.state = state;
    }


    @Override
    public String toString() {
        if (state == 1){
            return "True";
        }
        else if (state == -1){
            return "False";
        }
        else{
            return "Unknown";
        }
    }


}
