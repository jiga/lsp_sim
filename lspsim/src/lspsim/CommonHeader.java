/*
 * CommonHeader.java
 * This class stores the common header used by all OSPF packets
 * Created on November 3, 2005, 11:10 PM
 */

package lspsim;

/**
 * This class stores the common header used by all OSPF packets
 * @author Jignesh
 */
public class CommonHeader implements java.io.Serializable{
    /** Version number 1 byte **/
    byte version;
    
    /** Total packet length - number of 32 bit words including the header */
    int pktlength;
    
    /** Router ID */
    String routerID;
    
    /** Routing Packet types 
     * can be <ul>
     * <li>HELLO 
     * <li>DB_DESC
     * <li>LSR
     * <li>LSU
     * <li>LSA
     * <li>JOIN
     * <li>LEAVE
     * </ul>
     */
    static enum PacketType_s {HELLO,DB_DESC,LSR,LSU,LSA,JOIN,LEAVE}
    
    PacketType_s type;
    
    /** Creates a new instance of CommonHeader */
    public CommonHeader() {
        pktlength = 6;
        routerID = "";
        version = 1;
        /** Fill in the type for hello packet */
        type = PacketType_s.JOIN; 
    }
    
    /** Creates a new instance of CommonHeader 
     * overloaded constructor
     * @param instance of CommonHeader class
     */
    public CommonHeader(CommonHeader ch) {
        pktlength = ch.pktlength;
        routerID = ch.routerID;
        version = ch.version;
        type = ch.type; 
    }
    /** Creates a new instance of CommonHeader 
     * overloaded constructor
     * @param instance of CommonHeader class
     * @param PacketType enumeration
     * @param packetlenght - length of packet
     * @param rid -  router id
     */
    
    public CommonHeader(CommonHeader.PacketType_s t, int pl, String rid){
        version = 1;
        type = t;
        pktlength = pl;
        routerID = rid;
    }
}
