/** This class implements the Link State Common Header
  *
  */

package lspsim;
/**
 * This class implements the Link state Common 
 * Header (LSH)
 * @author prr35f
 */
public class LSHeader implements java.io.Serializable{
	
	/** Age of the Link state message */
	int age;
	
	/** Sequence number of link state message */
	long seq_no;
	
	/** Link state ID - Neighbour Router ID  for p2p*/
	String linkID;
	
	/** Advertising router */
	String adv_router;
	
	public LSHeader(){
		adv_router = null;
	}
	
	public LSHeader(int a, long s, String id, String router){
		this.age = a;
		this.seq_no = s;
		this.linkID = id;
		this.adv_router = router;
                //System.out.println("Created LSHeader for Link ID: " + linkID);
	}
        
	public long next_sno(){
		return this.seq_no++;
	}
	
}