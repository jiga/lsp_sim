
  
package lspsim;
/** 
 * This class implements the Link State 
 * Advertisement (LSA) Header
 * @author palani
 */
public class LSAPacket extends LSHeader {
	
	/** Number of links that are advertised */
	int no_links;
	
	/** Link ID */
	String linkID;
	
	/** Link data */
	int linkdata;
	
	/** Final cost Metric */
	int metric;
	
	public LSAPacket(){
		linkID = null;
		metric = 0;
	}
	
        /** @param n number of links
         *@param id Link ID - router Id
         *@param ld Link data - port no
         *@param cost - cost of link
         *@param delay - delay of link
         *@param band - link bandwidth
         *@param w_cost - cost weight
         *@param w_delay - delay weight
         *@param w_band - bandwidth weight
         *@param lsheader - LSA Header
         */
	public LSAPacket(int n, String id, int ld, float cost, float delay,
					 float bandwidth, float w_cost, float w_delay,
					 float w_band, LSHeader lsh) {
					 	
                                                super(lsh.age,lsh.seq_no,lsh.linkID,lsh.adv_router);
					 	this.no_links = n;
					 	this.linkID = id;
					 	this.linkdata = ld;
					 	this.metric = (int) (w_cost * cost + w_delay * delay + w_band * bandwidth);
					 	
					 }
		
		
	}