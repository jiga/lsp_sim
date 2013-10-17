/*
 * Node.java
 *
 * Created on November 2, 2005, 9:42 PM
 */

package lspsim;

import java.util.*;
/**
 * This class implements a generic Node in a network
 * @author Jignesh
 */
public class Node {
    /** Router ID */
    String routerId;
    
    /** List of interfaces */
    Vector interfaceList;
    
    /** Creates a new instance of Node */
    public Node() {
        this.routerId = null;
        interfaceList = new Vector();
    }
    
    public Node(String endPoint){
        this.routerId = endPoint;
        interfaceList = new Vector();
    }
    
}
