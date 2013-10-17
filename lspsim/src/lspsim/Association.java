/*
 * Association.java
 *
 * Created on November 1, 2005, 8:10 PM
 */

package lspsim;

/**
 * Association class describes the Links associated with each node
 * @author Jignesh
 */
public class Association {
    /** Port number used to represent a link. The lspsim server listens to this port number.*/
    public int port;
    
    /** end points array used to store the source and destination pair of this association */
    int [] endPoint;
    
    /** Enumeration type to store the association type
     * NO_CON - no connection for this association
     * ONE_WAY - one way connection
     * TWO_WAY - two way connection
     */
    static enum State_s {NO_CON, ONE_WAY, TWO_WAY}
    
    /** used to store the state of connection */
    State_s state;
    
    /** index for end points array */
    int index;
    
    /** Creates a new instance of Association */
    public Association() {
        port = 0;
        endPoint = new int[2];
        state = State_s.NO_CON;
        index = 0;
    }
    /** Initialize a new instance of Association 
     * @param p the port number
     */
    public Association(int p){
        this.port = p;
        index = 0;
        state = State_s.NO_CON;
        endPoint = new int[2];
    }
    
    /** Adds a new endpoint to this association 
     * @param end the end point
     * @return none
     */
    public void addEndPoint(int end) throws Exception{
        if(index < 2){
            this.endPoint[index] = end;
            index++;
            this.updateState();
        }
        else
            throw new Exception("Illegal addition: cannot add more than 2 endpoints. You might want to check the topology again.");
    }
    
    /** Update the State of association 
     *  @param none
     *  @return none
     */
    private void updateState() {
        switch(index){
            case 0: this.state = State_s.NO_CON;
                    break;
            case 1: this.state = State_s.ONE_WAY;
                    break;
            case 2: this.state = State_s.TWO_WAY;
                    break;
            default:break;
        }
    }
    
    /** Gets the destination peer for given source of this Association
     * @param end the source port for one end point
     * @return integer - the source port of the other end point
     */
    public int getPeer(int end) throws Exception {
        if(end == this.endPoint[0]) 
            return this.endPoint[1];
        else if(end == this.endPoint[1]) 
            return this.endPoint[0];
        else 
            throw new Exception("Endpoint not found");
    }
}
