/** This class implements the Link State Update Header
  *
  */
package lspsim;
/**
 * This class implements the Link state Update 
 * Header (LSU)
 * @author prr35f
 */
public class LSUPacket extends CommonHeader{
	
	/** Number of LSA'S */
	int no_lsa;
	
	/** LSA */
	LSAPacket lsa;
	
	public LSUPacket(){
		this.no_lsa = 0;
	}
	
	public LSUPacket(CommonHeader ch, int no, LSAPacket _lsa){
            
                super(ch);
		this.no_lsa = no;
		this.lsa = _lsa;
	}
	
}