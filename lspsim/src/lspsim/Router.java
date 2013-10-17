/*
 * Router.java
 *
 * Created on November 2, 2005, 9:53 PM
 */

package lspsim;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * This class implements the Router
 * @author Jignesh
 */
public class Router extends Thread{
    
    /** Router node: includes Router ID and Interface List */
    Node node;
    
    /** List of Router LSAs */
    Vector routerLSAList = new Vector();
    
    /** Routing Table */
    String[][] routingTable;
    
    /** Datagram socket */
    DatagramSocket socket;
    
    /** Datagram Packet */
    DatagramPacket packet;
    
    /** Buffer for packet */
    byte[] buffer;
    
    /** Neighbour list */
    Vector neighbourList = new Vector();
    
    /** Vector of links */
    Vector links = new Vector();
    
    /** Config parser object */
    ConfigParser cp = null;
    
    public Router(Vector lks){
        this.links = lks;
    }
    /** Timer Tasks class */
    public class HelloTimer extends TimerTask {
        int port;
        public HelloTimer(int p){
            this.port = p;
        }
        public void run(){
            try{
                //System.out.println("Timer called dhinka tikar ... "+this.port);
                sendHello(this.port);
            }catch(Exception e){System.out.println("EXCEPTION in HELOTIMER what about this ????  "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /** Timer Tasks class */
    public class DeadTimer extends TimerTask {
        String routerId;
        public DeadTimer(String id){
            this.routerId = id;
        }
        public void run(){
            try{
                //Neighbour is Dead so clean up from all the list.
                System.out.println("DeadTimer fired by "+node.routerId+"***************%%%%%%%%%%%%%%%%%%%%%% for neighbour "+this.routerId);
                //sendHello(this.port);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    /** Timer task for retransmitting pkts */
    public class RxmtTimer extends TimerTask {
        Neighbour nb;
        int port;
        public RxmtTimer(Neighbour n, int p){
            this.nb = n;
            this.port = p;
        }
        public void run(){
            try{
                System.out.println("RxmtTimer expired retransmiting DD...");
                sendLastDD(port, nb);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
     /** Timer task for retransmitting LSR pkts */
    public class LSRRxmtTimer extends TimerTask {
        Neighbour nb;
        int port;
        public LSRRxmtTimer(Neighbour n, int p){
            this.nb = n;
            this.port = p;
        }
        public void run(){
            try{
                System.out.println(getName()+"> LSRRxmtTimer expired retransmiting LSR...");
                sendLSR(port, nb);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /** Creates a new instance of Router */
    public Router(Node nd, Vector lks, ConfigParser cp) throws Exception{
        super("Router "+nd.routerId);
        System.out.println("----------[ Booting Router > "+nd.routerId);
        this.cp = cp;
        this.node = nd;
        this.links = lks;
        socket = new DatagramSocket();
        buffer = new byte[4096];
        CommonHeader ch = new CommonHeader(CommonHeader.PacketType_s.JOIN,6 , this.node.routerId);
        
        for(int i = 0; i < this.node.interfaceList.size(); i++)
        {
            Interface inf = (Interface) this.node.interfaceList.get(i);
            sendObjTo(ch,inf.port);
            inf.up(this);
        }
        /** code moved under section NSM - HELLO-RCD 
         // Build the routerLSA and store it in the database routerLSAList
        for (int i = 0;i < this.node.interfaceList.size(); i++)
        {
            Interface inf = (Interface) this.node.interfaceList.get(i);
            //System.out.println("Inf->neighbour: " + inf.neighbour);
            Link lk1 = getLink(inf.port);
            if (inf == null || lk1 == null){
                System.out.println("Failed to get the interface/Link details for port no: " + inf.port);
            }
            else {
                // generate the sequence number for LSHeader
                long sno =  System.currentTimeMillis();
                // construct the LSHeader
                LSHeader lsh = new LSHeader(0,sno,this.node.routerId,this.node.routerId);
                // construct the LSAPacket
                LSAPacket lsp = new LSAPacket(1,this.node.routerId,inf.port,inf.cost,inf.delay,lk1.speed,cp.param_wcost,cp.param_wdelay,cp.param_wbandwidth,lsh);
                // display details of the packet
                System.out.println("LS Age: " + lsp.age + " LS Seq no: " + lsp.seq_no + "  LS Link ID: " + lsp.linkID + " Adv_router: " + lsp.adv_router);
                System.out.println("----------------------------------------------------------------------------------------------------------");
                System.out.println("LSA");
                System.out.println("No of links: " + lsp.no_links + " Port no: " + lsp.linkdata);
                // add the lsa packet to database
                routerLSAList.add(lsp);
            }
        } */
    }
    public void sendHello(int port) throws Exception
    {
        CommonHeader ch = new CommonHeader(CommonHeader.PacketType_s.HELLO,6 , this.node.routerId);
        Interface inf = getInterface(port);
        String neighbourlist = "";
        
        if(this.neighbourList!=null){
            for(int n = 0; n < this.neighbourList.size(); n++){
                Neighbour nb = (Neighbour) this.neighbourList.get(n);
                neighbourlist += nb.routerId+" ";
            }
        }
        //else
          //  neighbourlist="";
        
        HelloPacket hh = new HelloPacket(ch,inf.helloInterval,inf.deadInterval,neighbourlist);
        //System.out.println("SendHello: inf.port = "+inf.port+ " neighbour list "+neighbourlist);
        sendObjTo(hh,port);
    }
    public void run(){
        while(true){
            try{
                
                    CommonHeader ch;
                    HelloPacket hh;
                    Object obj = new Object();
                    obj = recvObjFrom();
                    //System.out.println("Router recieved something?????????????????????/");
                    ch = (CommonHeader) obj;
                    int end = packet.getPort();
                    switch(ch.type){
                         case JOIN:
                             System.out.println(this.getName()+">"+"######################## Router can't get JOIN packet !");
                              break;
                         case HELLO:
                             HelloPacket hp = new HelloPacket();
                             hp = (HelloPacket)obj;
                             //System.out.println(this.getName()+">"+" # Recieved Hello Packet : "+hp.helloInterval +" "+hp.deadInterval +" "+hp.neighbourList +" From "+ch.routerID+ " using "+ end);
                             processHello(hp, end);
                             break;
                        case DB_DESC:
                            //System.out.println("\n"+this.getName()+">"+"~~~~~~~DB description pkt recieved ~~~~~~~~~~~~~~~~ from "+ch.routerID+"\n");
                            DDPacket dd = new DDPacket();
                            dd = (DDPacket)obj;
                            processDD(dd, end);
                            break;
                        case LSR:
                            LSRPacket lsr = new LSRPacket();
                            lsr = (LSRPacket) obj;
                            processLSR(lsr,end);
                            break;
                        case LSU:
                            LSUPacket lsu = new LSUPacket();
                            lsu = (LSUPacket) obj;
                            processLSU(lsu,end);
                            break;
                         default:
                             break;
                    }
            }
            //System.out.println("got pkt ;)- "+ch.routerID+" "+ch.type+" "+ch.version+" "+ch.pktlength);
            catch(Exception e){
                System.out.println(this.getName()+">"+" Exception in Router run "+e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public synchronized void processLSU(LSUPacket lsu, int port){
        System.out.println("\n"+this.getName()+"> ^^^^^^^^^^ LSU Pkt recieved : "+ lsu.lsa.linkID+" "+lsu.lsa.adv_router+" "+lsu.lsa.linkdata+ " ^^^^^^^^^^^^^ from "+lsu.routerID+"\n");
        Neighbour nb = null;
        String action = "";
        LSAPacket lsp = null;
        int indexFound = 0;
        /** find the neighbour */
        nb = getNeighbour(lsu.routerID);
        
        if(nb.state == Neighbour.NeighbourState_s.DOWN || nb.state == Neighbour.NeighbourState_s.INIT || nb.state == Neighbour.NeighbourState_s.ATTEMPT
                || nb.state == Neighbour.NeighbourState_s.EXSTART){
            return; // discard this pkt...
        }            
        boolean found = false;
            for(int i=0; i< this.routerLSAList.size();i++){
                lsp = (LSAPacket) this.routerLSAList.get(i);
                if(lsp.adv_router.equals(lsu.lsa.adv_router) && lsp.linkID.equals(lsu.lsa.linkID)){
                    found = true;
                    indexFound = i;
                    break;
                }
            }
        boolean nbfound = false;
            for(int i=0; i< this.neighbourList.size(); i++){
                Neighbour nbr = (Neighbour) this.neighbourList.get(i);
                if(nbr.state == Neighbour.NeighbourState_s.EXCHANGE || nbr.state == Neighbour.NeighbourState_s.LOADING){
                    nbfound = true;
                    break;
                }
            }
        if(lsu.lsa.age == this.cp.param_age && !found && !nbfound){
            // ack the lsa by sending lsack
            CommonHeader ch = new CommonHeader(CommonHeader.PacketType_s.LSA,6 , this.node.routerId);
            LSAckPacket lsack = new LSAckPacket(ch,lsu.lsa);
            sendObjTo(lsack,port);
            return;
        }
        // to do the flooding here
        else{
            // if its first time
            if(!found){
                // cancel the timer
                try{nb.lsrRetransmitTimer.cancel();}catch(Exception e){}
                // install the lsa
                this.routerLSAList.add(lsu.lsa);
                // remove from link state request list
                nb.linkStateRequestList.remove(0);
                // send next request
                if(nb.linkStateRequestList.size()==0){
                    // this is it, we are done with all requests
                    // generate the Loading done event
                    action = nb.nsm(Neighbour.NSMEvent_s.LOADING_DONE,this,port);
                }
                else{
                    sendLSR(port, nb);
                }
                return;
            }
            // see if its a new or old lsa
            boolean identical = false;
            boolean newer = false;
            if(lsp.seq_no < lsu.lsa.seq_no){
                newer = true;
            }
            else if (lsp.seq_no == lsu.lsa.seq_no){
                if(lsp.age==this.cp.param_age && lsu.lsa.age !=this.cp.param_age){
                    // alreadery newer
                }
                else if(lsp.age==this.cp.param_age && lsu.lsa.age !=this.cp.param_age){
                    newer = true;
                }
                else if(Math.abs(lsp.age - lsu.lsa.age) > this.cp.param_agediff){
                    if(lsp.age > lsu.lsa.age){
                        newer = true;
                    }
                }
            }
            else{
                identical = true;
            }
            
            if(!identical && newer){
                // flood this lsa out
                
                // remove the old lsa from current database
                //this.routerLSAList.remove(indexFound);
                // install the new lsa
                this.routerLSAList.setElementAt(lsp,indexFound);
            }
        }
    }
    public synchronized void processLSR(LSRPacket lsr, int port){
        System.out.println("\n"+this.getName()+"> ???????????? LSR Pkt recieved : "+ lsr.linkID+" "+lsr.adv_router+" ???????????????? from "+lsr.routerID+"\n");
        
        Neighbour nb = null;
        String action = "";
        
        /** find the neighbour */
        nb = getNeighbour(lsr.routerID);
        
        if(nb.state == Neighbour.NeighbourState_s.EXCHANGE || nb.state == Neighbour.NeighbourState_s.LOADING 
                || nb.state == Neighbour.NeighbourState_s.FULL){
            System.out.println(this.getName()+"> LSU accepted "+this.routerLSAList.size());
            boolean found = false;
            
            for(int i=0; i< this.routerLSAList.size();i++){
                LSAPacket lsa = (LSAPacket) this.routerLSAList.get(i);
                if(lsa.adv_router.equals(lsr.adv_router) && lsa.linkID.equals(lsr.linkID)){
                    found = true;
                    //LSAPacket lsa = (LSAPacket) this.routerLSAList.get(i);
                    // send link state update packet to neighbour
                    System.out.println(this.getName()+"> %%%%%%%%%%%% Sending LSU pkt .%%%%%%%%%%%%%%%%%");
                    CommonHeader ch = new CommonHeader(CommonHeader.PacketType_s.LSU,6 , this.node.routerId);
                    LSUPacket lsu = new LSUPacket(ch,1,lsa);
                    sendObjTo(lsu,port);
                    break;
                }
            }
            if(!found){
                // something wrong with Database exchange process
                action = nb.nsm(Neighbour.NSMEvent_s.BAD_LS_REQ,this,port);
            }
        }
        else
            return; // discarding the packet
    }
    
    public synchronized void processDD(DDPacket dd, int port){
        System.out.println("\n"+this.getName()+">"+"~~~~~~~DD pkt recieved : "+dd.dd_sno+" "+dd.flags+" ~~~~~~~~~~~~~~~~ from "+dd.routerID+"\n");
        Interface nif = getInterface(port);
        Neighbour nb = null;
        String action = "";
        boolean rfcUnleashed = false;
        
        /** find the neighbour */
        nb = getNeighbour(dd.routerID);
        
        System.out.println(this.getName()+"> start: Neighbour "+nb.routerId+" state "+nb.state);
        if(nb.state == Neighbour.NeighbourState_s.DOWN || nb.state == Neighbour.NeighbourState_s.ATTEMPT || nb.state == Neighbour.NeighbourState_s.TWO_WAY){
            System.out.println(this.getName()+"Neighbour state down/attempt/2way. dropping packet");
            return;
        }
        if(nb.state == Neighbour.NeighbourState_s.INIT){
            action = nb.nsm(Neighbour.NSMEvent_s.TWO_WAY_RECVD,this,port);
            System.out.println(this.getName()+"Neighbour state from init to "+action+" ");
            
            if(action.equals("exstart"))
                return;
        }
        if(nb.state == Neighbour.NeighbourState_s.EXSTART){
            System.out.println(this.getName()+">"+" Neighbour "+nb.routerId+" state is EXSTART "
                    +dd.getIFlag(dd.flags)+dd.getMFlag(dd.flags)+dd.getMSFlag(dd.flags)+" "
                    +dd.routerID);
            
            if(dd.getIFlag(dd.flags)==1 && dd.getMFlag(dd.flags)==1 && dd.getMSFlag(dd.flags)==1 
                    && (dd.routerID.compareTo(this.node.routerId) > 0)){
                /* i m slave */
                System.out.println(this.getName()+">"+" I am slave1");
                nb.M_OR_S = "0";
                //nb.I = "0";
                
                nb.dd_seq = dd.dd_sno;
                /** call the neighbour state machine */
                action = nb.nsm(Neighbour.NSMEvent_s.NEGOTIATION_DONE,this,port);
                rfcUnleashed = true;
               // nb.lastDD = dd;
            }
            else if(dd.getIFlag(dd.flags)==0 && dd.getMSFlag(dd.flags)==0 && nb.lastSent.dd_sno == dd.dd_sno
                    && (dd.routerID.compareTo(this.node.routerId) < 0)){
                /* i am master */
                System.out.println(this.getName()+">"+" I am Master1");
                /** call the neighbour state machine */
                action = nb.nsm(Neighbour.NSMEvent_s.NEGOTIATION_DONE,this,port);
                rfcUnleashed = true;
            }
            //this.sendDD(port, nb);
            if(!rfcUnleashed){
                this.sendDD(port, nb);
                return;
            }
            //this.sendDD(port, nb);
        }
        if(nb.state == Neighbour.NeighbourState_s.EXCHANGE){
            System.out.println(this.getName()+">"+" Neighbour "+nb.routerId+" state is EXCHANGE");
            if(nb.dd_seq == dd.dd_sno && rfcUnleashed == false){
                /** duplicate packet */
                if(dd.getMSFlag(dd.flags)==1){
                    // i am the slave 
                    // retransmit the last packet and return;
                    this.sendLastDD(port, nb); 

                    /** drop this packet */
                    System.out.println(this.getName()+">"+" Duplicate DD recieved dropping this pkt...1");
                    return;
                }
                else{
                    /** i am the master */
                }
            }
            /* next in sequence */
            if((dd.getIFlag(dd.flags) == 1 || Integer.parseInt(nb.M_OR_S) == dd.getMSFlag(dd.flags))
                && rfcUnleashed == false){
                System.out.println(this.getName()+">"+" Sequence number mismatch 1");
                action = nb.nsm(Neighbour.NSMEvent_s.SEQ_NUMBER_MISMATCH,this,port);
                return;
            }
            if(dd.getMSFlag(dd.flags)==0 && dd.dd_sno == nb.dd_seq || rfcUnleashed == true){
                /* i am master */
                //System.out.println(this.getName()+">"+" i m master");
            }
            else if(dd.getMSFlag(dd.flags)==1 && dd.dd_sno == nb.dd_seq+1 || rfcUnleashed == true){
                /* i m slave */
                //System.out.println(this.getName()+">"+"i m slave");
            }
            else {
                System.out.println(this.getName()+">"+" Sequence number mismatch 2 dd.dd_sno = " +dd.dd_sno
                        +" nb.dd_seq = "+nb.dd_seq);
                action = nb.nsm(Neighbour.NSMEvent_s.SEQ_NUMBER_MISMATCH,this,port);
                return;
            }
        }
        else if(nb.state == Neighbour.NeighbourState_s.LOADING || nb.state == Neighbour.NeighbourState_s.FULL){
            if(dd.getIFlag(dd.flags) == 1){
                System.out.println(this.getName()+">"+" Sequence number mismatch 4");
                action = nb.nsm(Neighbour.NSMEvent_s.SEQ_NUMBER_MISMATCH,this,port);
                return;
            }
            if(nb.dd_seq == dd.dd_sno){
                /** duplicate packet */
                if(dd.getMSFlag(dd.flags)==1){
                    // i am the slave 
                    // retransmit the last packet and return;
                    this.sendLastDD(port, nb); 
                    /** drop this packet */
                    System.out.println(this.getName()+">"+" Duplicate DD recieved dropping this pkt...");
                    return;
                }
                else{
                    /** i am the master */
                }
            }
        }
        // recieved dd packet is next in sequence
        // see if we have copy of this lsa?
        boolean found = false;
        System.out.println(this.getName()+"> dd.lsa "+ dd.lsa);
        if(!rfcUnleashed && dd.lsa !=null){
            for(int i=0; i< this.routerLSAList.size();i++){
                LSHeader lsah = (LSHeader) this.routerLSAList.get(i);
                System.out.println(this.getName()+"> Got the LSHeader with link id: " + lsah.linkID + " Adv router " + lsah.adv_router);
                if(lsah.linkID == dd.lsa.linkID && lsah.adv_router == dd.lsa.adv_router ){
                    //* lsa present 
                    // check if new lsa?
                    found = true;
                    if(lsah.seq_no < dd.lsa.seq_no){
                        //* this is new LSA 
                        // add to request list
                        nb.linkStateRequestList.add(dd.lsa);
                    }
                }
            }
        }
        if(!found){
             // no copy of lsa 
            // lsa recieved first timem.out.prin
            // add to request list
            if(dd.lsa != null) nb.linkStateRequestList.add(dd.lsa);
        }
        // see if you are the master or slave
        if(dd.getMSFlag(dd.flags)==1){
            // i am the slave 
            // retransmit the last packet and return;
            nb.dd_seq = dd.dd_sno;
            if(dd.getMFlag(dd.flags)==0 && nb.databaseSummaryList.size()==0){
                nb.M = "0";
                action = nb.nsm(Neighbour.NSMEvent_s.EXCHANGE_DONE,this,port);
                this.sendDD(port,nb);
            }
            else
                this.sendDD(port, nb);
        }
        else if(dd.getMSFlag(dd.flags)==0){
            /** i am the master */
            // increment the sequence number
            nb.dd_seq++;
            if(dd.getMFlag(dd.flags)==0 && nb.databaseSummaryList.size()==0){
                nb.M = "0";
                action = nb.nsm(Neighbour.NSMEvent_s.EXCHANGE_DONE,this,port);
            }
            else 
                this.sendDD(port, nb);  
        }
        
        System.out.println(this.getName()+"> end: Neighbour "+nb.routerId+" state "+nb.state);
    }
    public void processHello(HelloPacket hp, int port)throws Exception {
        /** identify the interface from which hello packet was received */
        Interface nif = getInterface(port);
        Neighbour nb= null;
        String action = "";
        
        /** neighbour seen first time */
        if (nif.neighbour.equals(null) || nif.neighbour == ""){
            //System.out.println("updated the neighbour list");
            nif.neighbour = hp.routerID;
            nb = new Neighbour(hp.routerID, hp.deadInterval);
            ///System.out.println("new neighbour "+nb.routerId);
            this.addNeighbour(nb);
            nb.resetInactivity(this);
        }
        else{
            try{
                nb = getNeighbour(hp.routerID);
            }catch(Exception e){
                nb = new Neighbour(hp.routerID, hp.deadInterval);
                //System.out.println("new nb "+nb.routerId);
                this.neighbourList.add(nb);
                nb.resetInactivity(this);
            }
        }
        
        /** call the neighbour state machine */
        action = nb.nsm(Neighbour.NSMEvent_s.HELLO_RECVD,this,port);
 
        /** check the neighbourList of recieved hello packet */
        if(hp.neighbourList.indexOf(this.node.routerId)!= -1) {
            action = nb.nsm(Neighbour.NSMEvent_s.TWO_WAY_RECVD,this,port);
        }
        else{
            action = nb.nsm(Neighbour.NSMEvent_s.ONE_WAY_RECVD,this,port);
        }

        //System.out.println("Processing Hello... with port "+port+ " nif "+nif.port + " neighbour " +nif.neighbour + " action performed"+action);

    }
    public synchronized void sendLSR(int port, Neighbour nb){
        CommonHeader ch = new CommonHeader(CommonHeader.PacketType_s.LSR,6 , this.node.routerId);
        Interface inf = getInterface(port);
        LSHeader lh = null;
        if(nb.linkStateRequestList.size()>0){
            lh = (LSHeader) nb.linkStateRequestList.firstElement();
        }
        else
            return;
        LSRPacket lsr = new LSRPacket(ch,lh.linkID,lh.adv_router);
        sendObjTo(lsr,port);

        // start lsr retransmitTimer 
        nb.lsrRetransmitTimer = new Timer();
        //Interface inf = r.getInterface(port);
        Router.LSRRxmtTimer lsrRxmt = new LSRRxmtTimer(nb,port);
        nb.lsrRetransmitTimer.schedule(lsrRxmt,inf.retransmitInterval*1000, inf.retransmitInterval* 1000);
    }
    
    public synchronized void sendDD(int port, Neighbour nb){
        CommonHeader ch = new CommonHeader(CommonHeader.PacketType_s.DB_DESC,6 , this.node.routerId);
        Interface inf = getInterface(port);
        LSHeader lh = null;
        if(nb.state == Neighbour.NeighbourState_s.EXSTART){
        }
        else if(nb.state == Neighbour.NeighbourState_s.EXCHANGE){
            if(nb.databaseSummaryList.size()>1){
                lh = (LSHeader) nb.databaseSummaryList.firstElement();
                nb.I = "0";
                nb.M = "1";
                nb.databaseSummaryList.remove(0);
            }
            else if(nb.databaseSummaryList.size() == 1){
                lh = (LSHeader) nb.databaseSummaryList.firstElement();
                nb.I = "0";
                nb.M = "0";
                nb.databaseSummaryList.remove(0);
            }
            else {
                nb.I = "0";
                nb.M = "0";
            }
        }
        
        String flags = nb.I+nb.M+nb.M_OR_S;
        DDPacket dh = new DDPacket(512,flags,nb.dd_seq,lh,ch);
        
        // save the last packet 
        nb.lastSent = dh;
        
        System.out.println(this.getName()+">"+" sending dd packet to "+nb.routerId+" via link "+port);
        sendObjTo(dh,port);
    }
    public synchronized void sendLastDD(int port,Neighbour nb){
        System.out.println(this.getName()+"sending last dd again to "+nb.routerId+" via link "+port);
        sendObjTo(nb.lastSent,port);
    }
    private void addNeighbour(Neighbour nb){
        for(int t = 0; t < this.neighbourList.size(); t ++){
            Neighbour temp = (Neighbour) this.neighbourList.get(t);
            if(temp.routerId.equals(nb.routerId))
                return;
        }
        this.neighbourList.add(nb);
    }
    public Neighbour getNeighbour(String nid){
        for(int t = 0; t < this.neighbourList.size(); t ++){
            Neighbour nb = (Neighbour) this.neighbourList.get(t);
            if(nb.routerId.equals(nid))
                return nb;
        }
        return null;
    }
    public Interface getInterface(int p){
        for (int t = 0; t < this.node.interfaceList.size(); t++){
            Interface inf = (Interface)this.node.interfaceList.get(t);
            if ( inf.port == p )
                return inf;
        }
        return null;
    }
    public Link getLink(int p){
        for (int t = 0; t < this.links.size(); t++){
            Link lk = (Link)this.links.get(t);
            if (lk.LinkID == p)
                return lk;
        }
        return null;
    }
    public synchronized void sendObjTo(Object o, int desPort){    
        try {
             InetAddress address = InetAddress.getByName("localhost");
              ByteArrayOutputStream byteStream = new
                  ByteArrayOutputStream(4096);
              ObjectOutputStream os = new ObjectOutputStream(new
                                      BufferedOutputStream(byteStream));
              os.flush();
              os.writeObject(o);
              os.flush();
              //retrieves byte array
              byte[] sendBuf = byteStream.toByteArray();
              packet = new DatagramPacket(
                                  sendBuf, sendBuf.length, address, desPort);
              int byteCount = packet.getLength();
              socket.send(packet);
              os.close();
        }
        catch (Exception e){
          System.out.println(this.getName()+">"+" Exception:  " + e.getMessage());
          e.printStackTrace();
        }
    }
    public Object recvObjFrom(){
        try {
            byte[] recvBuf = new byte[4096];
            packet = new DatagramPacket(recvBuf,
                                                         recvBuf.length);
            socket.receive(packet);
            int byteCount = packet.getLength();
            ByteArrayInputStream byteStream = new
                                        ByteArrayInputStream(recvBuf);
            ObjectInputStream is = new
                   ObjectInputStream(new BufferedInputStream(byteStream));
            Object o = is.readObject();
            is.close();
            return(o);
        }
        catch (IOException e){
            System.out.println(this.getName()+">"+" Exception:  " + e.getMessage());
        }
        catch (ClassNotFoundException e){ 
            System.out.println(this.getName()+">"+" Exception:  " + e.getMessage());
        }
        
        return(null);  
    }
}
