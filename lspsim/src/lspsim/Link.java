package lspsim;

/**
 * This class implements the Link data-structure
 * @author  Palani
 */
public class Link {
    
    /** Link ID is the port number */
    public int LinkID;
    /** Speed is the link speed */
    public int speed;
    /** MTU is the link MTU */
    public int mtu;
    /** Delay is the link delay */
    public float delay;
    /** Error_rate is the link error rate percentage */
    public float error_rate;
    
      
    public Link() {
        speed = 0;
        mtu = 0;
        delay = 0;
        error_rate = 0;
        
    }
    /*
     * Overloaded constructor
     * @param ID - Link ID
     * @param sp - Link speed
     * @param mtu - Link MTU
     * @param del - Link delay
     * @param err - Link error rate
     */
    public Link(int ID, int sp, int mtu, float del, float err){
        this.LinkID = ID;
        this.speed = sp;
        this.mtu = mtu;
        this.delay = del;
        this.error_rate = err;
    }
}
