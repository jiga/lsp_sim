
package lspsim;
  /** 
   * This class implements the DD Header
   * @author palani
   */
  
public class DDPacket extends CommonHeader{
	
	/** Interface MTU */
	int mtu;
	
	/** I/M/MS bits */
	String flags;
	
	/** DD Sequence number */
	long dd_sno;
	
	/** LSA Header */
	LSHeader lsa;
	
	public DDPacket(){
		this.mtu = 0;
		dd_sno = 0;
		
	}
	
	public DDPacket(int _mtu, String f, long dds, LSHeader _lsa, CommonHeader ch){
                super(ch);
		this.mtu = _mtu;
		this.flags = f;
		this.dd_sno = dds;
		this.lsa = _lsa;
	}
	
	public long next_sno(){
		return this.dd_sno++;
	}
	
	public int getMFlag(String flag){
		if (flag.length() > 3 )
			return -1;
		int Mbit = Integer.parseInt(flag.substring(1,2));
		return Mbit;
	}
	
   public int getIFlag(String flag){
		if (flag.length() > 3 )
			return -1;
		int Ibit = Integer.parseInt(flag.substring(0,1));
		return Ibit;
	}
	
   public int getMSFlag(String flag){
		if (flag.length() > 3 )
			return -1;
		int MSbit = Integer.parseInt(flag.substring(2,3));
		return MSbit;
	}


	

	
}