/*
 * LSRPacket.java
 */

package lspsim;

/**
 * This class implements the Protocol Common Header
 * @author prr35f
 */
public class LSRPacket extends CommonHeader{
    /** Link ID. This is the port number **/
    String linkID;
    
    /** Advertising router */
    String adv_router;
    
    /** Creates a new instance of LSRPacket */
    public LSRPacket() {
        linkID = "";
        adv_router = "";
    }
    
    /** Overloaded constructor 
     * @param linkID
     * @param adv_router
     */
    public LSRPacket(CommonHeader ch,String id, String router){
        /* initialize the base class constructor */
        super(ch);
        this.linkID = id;
        this.adv_router = router;
    }
    
}
