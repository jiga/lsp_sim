/*
 * HelloHeader.java
 *
 * Created on November 4, 2005, 8:09 PM
 */

package lspsim;

/**
 * This class implements the HelloHeader
 * @author Jignesh
 */
public class HelloPacket extends CommonHeader{
    /** hello interval */
    int helloInterval;
    /** dead interval */
    int deadInterval;
    /** neighbour list */
    String neighbourList;
    
    /** Creates a new instance of HelloHeader */
    public HelloPacket() {
        helloInterval = 5;
        deadInterval = 25;
        neighbourList = null;
    }
    
    public HelloPacket(CommonHeader ch, int hi, int di, String n) {
        /** common header initializations */
        super(ch);
        helloInterval = hi;
        deadInterval = di;
        neighbourList = n;
    }
    
    public void addNeighbour(String n){
        neighbourList +=" "+n;
    }
}
