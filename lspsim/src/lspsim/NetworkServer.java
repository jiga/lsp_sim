/*
 * NetworkServer.java
 *
 * Created on November 3, 2005, 10:28 PM
 */

package lspsim;

import java.io.*;
import java.net.*;

import java.util.Vector;
import java.net.*;
import java.io.*;
/**
 * This class implements the simulation underlying point-to-point network 
 * @author Jignesh
 */

public class NetworkServer extends Thread{
    
    /** instance of association class */
    Association link_assoc;
    
    /** instance of link class */
    Link link;
    
    /** Datagram server socket */
    DatagramSocket socket;
    
    /** Datagram Packet */
    DatagramPacket packet;
    
        
    /** Buffer to store the packets */
    byte[] buffer;
    
    
    /** Creates a new instance of NetworkServer */
    public NetworkServer(Association a, Link l) throws Exception {
         super("NetworkServer:"+a.port);
         this.link_assoc = a;
         this.link = l;
         this.buffer = new byte[l.mtu];
        // System.out.println(a.port);
         socket = new DatagramSocket(a.port);
         packet = new DatagramPacket(buffer,buffer.length);
       //  System.out.println("Network Server started and is listening on port :" + a.port);
         
    }
    
     public void run(){
         
         while (true){
             try{
                 //System.out.println("Network Server is running ");
                 Object obj = new Object();
                 CommonHeader ch = new CommonHeader();
                 obj = recvObjFrom();
                 ch = (CommonHeader) obj;
                 int end = packet.getPort();
                 int peer = -1;
                 //System.out.println("Network Server got pkt from port : "+end);
                 switch(ch.type){
                     case JOIN:
                         System.out.println("NS "+this.link.LinkID+": recieved JOIN from "+ end+ " adding to the association");
                         
                         this.link_assoc.addEndPoint(end);
                         break;
                     /*case HELLO:
                         HelloHeader hp = new HelloHeader();
                         hp = (HelloHeader)obj;
                        // System.out.println("Hello Packet  from "+packet.getPort()+": "+hp.helloInterval +" "+hp.deadInterval +" "+hp.neighbourList);
                         
                         peer = this.link_assoc.getPeer(end);
                         System.out.println("NS: recieved HELLO from "+ end+ " Forwarding to : "+peer);
                         //if(ch.routerID.equals("A"))
                         sendObjTo(obj,peer);
                         break;*/
                     default:
                         int random = (int) Math.floor(Math.random()*100);
                         //System.out.println("Random value "+random);
                         /* do not delete this comment
                          if(this.link.error_rate> random){
                             System.out.println("NS: Link error - Packet dropped![][][][][][][][][][][][][][][][][][][][][][][][][][][] "+random);
                             return;
                         }
                         //*/

                         peer = this.link_assoc.getPeer(end);
                         sendObjTo(obj,peer);
                         break;
                 }
                     
             }catch (Exception ioe) {
                 System.out.println(ioe.getMessage());
             }
             
         }
    }
     
    public void sendObjTo(Object o, int desPort){    
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
          System.out.println("Exception:  " + e.getMessage());
        }
    }
    public Object recvObjFrom(){
        try {
            //System.out.println("Network server is recieving object ...........................");
            byte[] recvBuf = new byte[4096];
            packet = new DatagramPacket(recvBuf, recvBuf.length);
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
            System.out.println("Exception:  " + e.getMessage());
        }
        catch (ClassNotFoundException e){ 
            System.out.println("Exception:  " + e.getMessage());
        }
        
        return(null);  
    }
}
