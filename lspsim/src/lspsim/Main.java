/*
 * Main.java
 * Driver program for the Link State Protocol implementation
 */

package lspsim;

/**
 * This is the main simulator class
 * @author Jignesh
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  throws Exception {
        
        ConfigParser cp = new ConfigParser("config.txt");
        //cp.printVectors();
        int i = cp.associations.size();
        NetworkServer ns;
        for (int j=0;j<i;j++){
            Association a = (Association)cp.associations.get(j);
            Link l = (Link)cp.links.get(j);
            //System.out.println("main:"+a.port);
            new NetworkServer(a,l).start();
            
            //ns.start();
        }
        for (int n = 0; n < cp.nodes.size(); n++)
        {
            Router r = new Router((Node)cp.nodes.get(n),cp.links,cp);
            r.start();
        }

        
        
        //System.exit(0);
    }
}
