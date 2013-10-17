/*
 * Interface.java
 *
 * Created on November 2, 2005, 9:24 PM
 */

package lspsim;

import java.util.*;
import java.net.*;
        
/**
 * This class implements the Interface data-structure
 * @author Jignesh
 */
public class Interface {
    
    /** Enumeration for Interface Type */
    public static enum InterfaceType_s {P_2_P, BROADCAST, NBMA, P_2_MP}
    public InterfaceType_s type;
    
    /** IP address of the interface */
    public String ipaddress;
    
    /** Link port number to which interface is attached */
    public int port;
    
    /** Subnet mask of the interface */
    public String subnetmask;
    
    /** HelloInterval */
    public int helloInterval;
    
    /** Router Dead Interval */
    public int deadInterval;
    
    /** Neighbour router */
    public String neighbour;
    
       
    /** Cost of this interface */
    public int cost;
    
    /** RetransmitInterval */
    public int retransmitInterval;
    
    /** Delay of this interface */
    public int delay;
    
    /** Hello Timer : Interval Timer*/
    public Timer helloTimer;
    

    /** Interface State */
    public static enum InterfaceState_s {IF_UP, IF_DOWN}
    public InterfaceState_s state;
    
    static enum ISMEvent_s { LINK_UP, LINK_DOWN }
    
    /** Light weight implementation of Interface Finite State Machine */
    public String ism(ISMEvent_s e) {
        if(e == ISMEvent_s.LINK_DOWN && state == InterfaceState_s.IF_UP) {
            this.state = InterfaceState_s.IF_DOWN;
            return "disableTimer";
        }
        
        if(e == ISMEvent_s.LINK_UP && state == InterfaceState_s.IF_DOWN) {
            this.state = InterfaceState_s.IF_UP;
            return "enableTimer";
        }    
        
        return "";
    }
    
    /** Creates a new instance of Interface */
    public Interface() {
        this.type = InterfaceType_s.P_2_P;
        this.ipaddress = null;
        this.subnetmask = null;
        this.helloInterval = 0;
        this.deadInterval = 0;
        this.cost = 0;
        this.retransmitInterval = 0;
        this.delay = 0;
        this.state = InterfaceState_s.IF_DOWN;
        this.neighbour = "";
    }

    /** Creates a new instance of Interface */
    public Interface(int p,Interface.InterfaceType_s t, String ip, String mask, int hello, int dead, int c, int rt,int d, Interface.InterfaceState_s s) {
        this.port = p;
        this.type = t;
        this.ipaddress = ip;
        this.subnetmask = mask;
        this.helloInterval = hello;
        this.cost = c;
        this.retransmitInterval = rt;
        this.deadInterval = dead;
        this.delay = d;
        this.state = s;
        this.neighbour = "";
    }    
    
    /** Interface Up method */
    public void up(Router r){
        this.helloTimer = new Timer();
        //Router r = new Router();
        Router.HelloTimer ht = r.new HelloTimer(this.port);
        helloTimer.schedule(ht,0,this.helloInterval*1000);
    }
    
}
