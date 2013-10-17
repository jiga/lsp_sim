/*
 * Neighbour.java
 *
 * Created on November 2, 2005, 10:37 PM
 */

package lspsim;

import java.net.*;
import java.util.*;

/**
 * This class implements the Neighbour data-structure
 * @author Jignesh
 */
public class Neighbour extends Node{
    /** Neighbour state */
    enum NeighbourState_s {DOWN, ATTEMPT, INIT, TWO_WAY, EXSTART, EXCHANGE, LOADING, FULL}
    NeighbourState_s state;
    
    /** flag - Master/slave */
    String M_OR_S;
    /** flag for Initial bit */
    String I;
    /** flag for more bit */
    String M;
    
    /** DD sequence number */
    long dd_seq = System.currentTimeMillis()+ (int) Math.floor(Math.random()*100000);;
    
    /** inactivity timer interval */
    int inactivityInterval;
    
    /** Inactivity Timer : Single shot Timer*/
    public Timer inactivityTimer = null;
    
    /** retransmit timer : single shot timer */
    public Timer retransmitTimer = null;
    
    /** LSR retransmit timer: single shot timer */
    public Timer lsrRetransmitTimer = null;
    
    /** last DD packet recieved - DD header */
    DDPacket lastDD;
    
    
    /** last DD packet sent to this neighbour */
    DDPacket lastSent;
    
    /** list of LSAs */
    Vector lsaList = new Vector();
    
    /**     Link state retransmission list
     * The list of LSAs that have been flooded but not acknowledged on
     * this adjacency.  These will be retransmitted at intervals until
     * they are acknowledged, or until the adjacency is destroyed.
     */
    Vector linkStateRetransmissionList = new Vector();
    
    /**     Link state request list
     * The list of LSAs that need to be received from this neighbor in
     * order to synchronize the two neighbors' link-state databases.
     * This list is created as Database Description packets are
     * received, and is then sent to the neighbor in Link State Request
     * packets.  The list is depleted as appropriate Link State Update
     * packets are received.
     */
    Vector linkStateRequestList = new Vector();
    
    /**     Database summary list
     * The complete list of LSAs that make up the link-state
     * database, at the moment the neighbor goes into Database Exchange
     * state.  This list is sent to the neighbor in Database
     * Description packets. This is same as link-state database since
     * we are implementing only routerLSAs
     */
    Vector databaseSummaryList = new Vector(); // this is same as Link state database     
    
    
    static enum NSMEvent_s {START, HELLO_RECVD, ONE_WAY_RECVD, TWO_WAY_RECVD, NEGOTIATION_DONE, EXCHANGE_DONE, LOADING_DONE,
                            SEQ_NUMBER_MISMATCH, BAD_LS_REQ, KILL_NBR, INACTIVITY_TIMER}
    
    /** Neighbour state machine */
    public String nsm(NSMEvent_s e, Router r, int port){
        switch(e){
            case START:
                if(state == NeighbourState_s.DOWN){
                    state = NeighbourState_s.ATTEMPT;
                    return "attempt";
                }
                return "no-op";
            case HELLO_RECVD:
                if(state == NeighbourState_s.DOWN){
                    state = NeighbourState_s.INIT;
                    this.resetInactivity(r);
                    // Build the routerLSA and store it in the database routerLSAList
                    
                        Interface inf = r.getInterface(port);
                        //System.out.println("Inf->neighbour: " + inf.neighbour);
                        Link lk1 = r.getLink(inf.port);
                        if (inf == null || lk1 == null){
                            System.out.println("Failed to get the interface/Link details for port no: " + inf.port);
                        }
                        else {
                            // generate the sequence number for LSHeader
                            long sno =  System.currentTimeMillis();
                            // construct the LSHeader
                            LSHeader lsh = new LSHeader(0,sno,inf.neighbour,r.node.routerId);
                            // construct the LSAPacket
                            LSAPacket lsp = new LSAPacket(1,inf.neighbour,inf.port,inf.cost,inf.delay,lk1.speed,r.cp.param_wcost,r.cp.param_wdelay,r.cp.param_wbandwidth,lsh);
                            // display details of the packet
                            System.out.println("NSM IN HELLO-RECEIVED : FROM INIT STATE");
                            System.out.println("LS Age: " + lsp.age + " LS Seq no: " + lsp.seq_no + "  LS Link ID: " + lsp.linkID + " Adv_router: " + lsp.adv_router);
                            System.out.println("----------------------------------------------------------------------------------------------------------");
                            System.out.println("LSA");
                            System.out.println("No of links: " + lsp.no_links + " Port no: " + lsp.linkdata);
                            // add the lsa packet to database
                            r.routerLSAList.add(lsp);
                        }
                    
                    return "startInactivityTimer";
                }
                if(state == NeighbourState_s.ATTEMPT){
                    state = NeighbourState_s.INIT;
                    this.resetInactivity(r);
                    return "restartInactivityTimer";
                }
                // no state change
                this.resetInactivity(r);
                return "restartInactivityTimer";
                
            case ONE_WAY_RECVD:
                if(state == NeighbourState_s.DOWN || state == NeighbourState_s.INIT){
                    return "no-op";
                }
                state = NeighbourState_s.INIT;
                // action
                return "clearAllLSA";
                
            case TWO_WAY_RECVD:
                if(state == NeighbourState_s.DOWN){
                    return "no-op";
                }
                if(state == NeighbourState_s.INIT){
                    // find if adjacency is required
                    // since the network is Point-to-Point the adjacency should
                    // be formed heree
                    state = NeighbourState_s.EXSTART;
                    // increment the dd seq number of this neighbour data structure
                    dd_seq++; //= System.currentTimeMillis();
                    

                    // set I , M /S bits to 1
                    M_OR_S = "1";
                    I = "1";
                    M = "1";
                    System.out.println(r.getName()+"> neighbour "+this.routerId+" state changing from init to exstart");
                    r.sendDD(port, this);
                    
                    // start retransmitTimer 
                    this.retransmitTimer = new Timer();
                    Interface inf = r.getInterface(port);
                    Router.RxmtTimer rxmt = r.new RxmtTimer(this,port);
                    this.retransmitTimer.schedule(rxmt,inf.retransmitInterval*1000, inf.retransmitInterval* 1000);
                    
                    return "exstart";
                }
                // no state change
                return "no-op";
                
            case NEGOTIATION_DONE:
                if(state == NeighbourState_s.EXSTART){
                    // cancel the timer since the state is changed
                    this.retransmitTimer.cancel();
                    this.I = "0";
                    System.out.println("Router "+r.node.routerId+" NSM: Negotiation done for neighbour "+this.routerId);
                    state = NeighbourState_s.EXCHANGE;
                    // copy the routerlsa list to database summary list
                    for(int i =0; i< r.routerLSAList.size(); i++){
                        LSHeader lsh = (LSHeader) r.routerLSAList.get(i);
                        if(lsh.age >= r.cp.param_age){
                            this.linkStateRetransmissionList.add(lsh);
                        }
                        else{
                            this.databaseSummaryList.add(lsh);
                        }
                    }
                   // r.sendDD(port, this);
                    return "exchange";
                }
                return "no-op";
            
            case EXCHANGE_DONE:
                if(state == NeighbourState_s.EXCHANGE){
                    this.retransmitTimer.cancel();
                    
                    if(this.linkStateRequestList.size()==0){
                        state = NeighbourState_s.FULL;
//                        /* print the link state request */
//                        System.out.println(r.getName()+"> +++++++++++++++++ Link State Requests for "+this.routerId+" +++++++++++++++");
//                        for(int i=0; i< this.linkStateRequestList.size();i++){
//                            LSHeader lh= (LSHeader) this.linkStateRequestList.get(i);
//                            System.out.println(r.getName()+"> lsr "+i+ " adv router = "+lh.adv_router+ " linkid = "+lh.linkID+ " seqno "+lh.seq_no);
//                        }
                    }
                    else{
                        state = NeighbourState_s.LOADING;
                        // start sending LSR packets 
                        // 
                        r.sendLSR(port,this); 
                    }
                        
                    if(state == NeighbourState_s.LOADING){
                        this.retransmitTimer.cancel();
                        this.lsrRetransmitTimer.cancel();
                        if(this.linkStateRequestList.size()==0){
                            state = NeighbourState_s.FULL;
//    ////                            /* print the link state request */
//    ////                            System.out.println(r.getName()+"> +++++++++++++++++ Link State Requests for "+this.routerId+" +++++++++++++++");
//    ////                            for(int i=0; i < this.linkStateRequestList.size();i++){
//    ////                                LSHeader lh = (LSHeader) this.linkStateRequestList.get(i);
//    ////                                System.out.println(r.getName()+">lsr "+i+ " adv router = "+lh.adv_router+ " linkid = "+ lh.linkID + " seqno "+lh.seq_no);
//    ////                            }
                        }
                    }
                    return "full-or-loading-exchangedone";
                }
                return "no-op";
            case LOADING_DONE:
                if(state == NeighbourState_s.LOADING){
                    state = NeighbourState_s.FULL;
                    System.out.println(r.getName()+"> neighbour "+this.routerId+" is now FULL !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    return "no-op";
                }
                return "no-op";
            case SEQ_NUMBER_MISMATCH:
            case BAD_LS_REQ:
                if(state == NeighbourState_s.DOWN || state == NeighbourState_s.ATTEMPT || state == NeighbourState_s.INIT
                        || state == NeighbourState_s.EXSTART){
                    return "no-op";
                }
                state = NeighbourState_s.EXSTART;
                return "start-exchange-seqmismatch";
                
            case KILL_NBR:
            case INACTIVITY_TIMER:
                state = NeighbourState_s.DOWN;
                return "clearAllLSA";
            default:
                break;
        }
        
        return "no-op";
    }
    
    /** Creates a new instance of Neighbour */
    public Neighbour() {
        this.state = NeighbourState_s.DOWN;
        this.M_OR_S = "1";
        this.I = "1";
        this.M = "1";
    }
    
    public Neighbour(String id, int inactivity){
        super.routerId = id;
        this.inactivityInterval = inactivity;
        this.state = NeighbourState_s.DOWN; 
        
        this.M_OR_S = "1"; // assume master
        this.I = "1";
        this.M = "1";
        
        this.lastDD = new DDPacket();
        this.lastDD.dd_sno = -1;
        this.lastDD.flags = "xxx";
        this.lastDD.mtu = 0;
        this.lastDD.lsa = null;
    }
    
    /** reset Inactivity Timer */
    public void resetInactivity(Router r){
        //System.out.println("reset Inactivity timer called ");
        if(this.inactivityTimer != null) {
            this.inactivityTimer.cancel();
            //this.inactivityTimer.st
            //System.out.println("Inactivity timer cancelled");
        }
        //this.inactivityTimer.cancel();
        //System.out.println("Inactivity timer restarted");
        this.inactivityTimer = new Timer();
        //Router r = new Router();
        Router.DeadTimer dt = r.new DeadTimer(this.routerId);
        this.inactivityTimer.schedule(dt,this.inactivityInterval*1000);
    }
}
