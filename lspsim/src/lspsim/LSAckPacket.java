/*
 * LSAckPacket.java
 */

package lspsim;

/**
 * This class implements the Link state Acknowledgement 
 * Header (LSAck)
 * @author prr35f
 */
public class LSAckPacket extends CommonHeader {
    
    /** LSAPacket */
    LSAPacket lsa;
    
    /** Creates a new instance of LSAckPacket */
    public LSAckPacket() {
        lsa = null;
    }
    
    public LSAckPacket(CommonHeader ch, LSAPacket lsp){
        super(ch);
        this.lsa = lsp;
    }
    
}
