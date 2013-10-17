
package lspsim;

import java.io.*;
import java.util.*;
import java.net.*;
/**
 * ConfigParser class does the parsing of config.txt 
 * and constructs the related data-structures & parameters
 * @author Palani
 */
public class ConfigParser{
    
	/** fileName of  the config file */
	String fileName;
        /** Input bufferedreader for file input */
	BufferedReader rd;
        /** File object */
	File file = null;
        /** StringTokenizer for tokenizing the input string */
	StringTokenizer st=null;
        /** Input line read from file */
        String line;
        /** Vector storing objects of type Link */
        Vector links;
        /** Vector storing objects of type Interface */
        Vector interfaces;
        /** Vector storing objects of type Node */
        Vector nodes;
        /** Vector storing objects of type Association */
        Vector associations;
        /** Global variable max age */
        int param_age;
        /** Global variable age_diff */
        int param_agediff;
        /** Global variable for cost weight */
        float param_wcost;
        /** Global variable for delay weight */
        float param_wdelay;
        /** Global variable for bandwidth weight */
        float param_wbandwidth;
        /**
         * Constructor for the class
         * @param filename: Name of the input file
         */
       	public ConfigParser(String fileName) throws Exception {
                
		if (fileName == "" || fileName == null){
			//System.out.println("Invalid file name/path");
			System.exit(0);
		}
                links = new Vector(); 
                interfaces = new Vector();
                nodes = new Vector();
                associations = new Vector();
		file = new File(fileName);
                rd = new BufferedReader(new FileReader(file));
                populateVectors();
	}
        /** Function to populate all the vectors
         * @throws exception
         */
        private void populateVectors() throws Exception{
            /** read a line from file */
            line = rd.readLine();
            while (line != null){
                /** skip the comments and categories */
                if (line.startsWith("#")){
                    line = rd.readLine();
                    continue;
                }
                if (line.startsWith(".")){
                    switch (selectCategory(line)){
                        case 0:
                            /** code to handle link */
                            line = rd.readLine();
                            while (!line.equals(""))
                            {    
                                st = new StringTokenizer(line);
                                while (st.hasMoreTokens()){
                                    int id = Integer.parseInt(st.nextToken());
                                    int speed = Integer.parseInt(st.nextToken());
                                    int mtu = Integer.parseInt(st.nextToken());
                                    float delay = Float.parseFloat(st.nextToken());
                                    float error = Float.parseFloat(st.nextToken());
                                    Link lk = new Link(id,speed,mtu,delay,error);
                                    Association as = new Association(id);
                                    associations.addElement(as);
                                    links.addElement(lk);
                                }
                                line = rd.readLine();
                            }
                            break;
                          
                        case 2:
                            /** code to handle interfaces, nodes and associations */
                            line = rd.readLine();
                            while (!line.equals("")) {
                                st = new StringTokenizer(line);
                                String endpoint = st.nextToken();
                                Node nd = new Node(endpoint);
                                while (st.hasMoreTokens()){
                                    String temp = st.nextToken();
                                    /** string temp has interface details along with node details
                                     * use a sub tokenizer to get the individual items listed in
                                     * the string **/
                                    StringTokenizer st1 = new StringTokenizer(temp,":");
                                    while (st1.hasMoreTokens()){
                                        /** populate the interface class */
                                        int portid = Integer.parseInt(st1.nextToken());
                                        Association as = new Association(portid);

                                        String ipandmask = st1.nextToken();
                                        String ip = getIP(ipandmask);
                                        String mask = getMask(ipandmask);
                                        int hinterval = Integer.parseInt(st1.nextToken());
                                        int dinterval = Integer.parseInt(st1.nextToken());
                                        int rt = Integer.parseInt(st1.nextToken());
                                        int cost = Integer.parseInt(st1.nextToken());
                                        int delay = Integer.parseInt(st1.nextToken());
                                        Interface it = new Interface(portid,Interface.InterfaceType_s.P_2_P, ip, mask, hinterval, dinterval, cost, rt, delay, Interface.InterfaceState_s.IF_DOWN);
                                        interfaces.addElement(it);
                                        nd.interfaceList.addElement(it);
                                    }
                                }
                                nodes.addElement(nd);
                                line = rd.readLine();
                            }
                            break;
                            
                        case 3:
                            /** code to handle the parameters from configuration file */
                            line = rd.readLine();
                            while (!line.equals("")){
                                StringTokenizer stk = new StringTokenizer(line,":");
                                while (stk.hasMoreTokens()){
                                    /** get the attribute and initialize the global variables */
                                    String attribute = stk.nextToken();
                                    if (attribute.equals("MaxAge"))
                                        param_age = Integer.parseInt(stk.nextToken());
                                    else if (attribute.equals("MaxAgeDiff"))
                                        param_agediff = Integer.parseInt(stk.nextToken());
                                    else if (attribute.equals("w_cost"))
                                        param_wcost = Float.parseFloat(stk.nextToken());
                                    else if (attribute.equals("w_delay"))
                                        param_wdelay = Float.parseFloat(stk.nextToken());
                                    else if (attribute.equals("w_bandwidth"))
                                        param_wbandwidth = Float.parseFloat(stk.nextToken());
                                    else 
                                        throw new Exception("Unrecognized attribute in LSParameters category");
                                }
                                line = rd.readLine();
                            }
                        default:
                            break;
                    }
                }
                line = rd.readLine();
            }
        }
        
       
        private int selectCategory(String category){
            /** cat is 0 for link, 1 for node, 2 for topology 
             *  3 for lsparams and -1 for no match */
            int cat = -1;
            if (category.equals(".link"))
                cat = 0;
            else if (category.equals(".topology"))
                cat = 2;
            else if (category.equals(".LSRparameters"))
                cat = 3;
            return cat;
        }
        
        private String getIP(String address){
            /** extract the ip address from cidr notation */
            return address.substring(0, address.indexOf('/'));
            
        }
        
        private String getMask(String address){
            /** extract the mask from cidr notation */
            String ret = null;
            String temp = address.substring(address.indexOf('/')+1, address.length());
            //System.out.println(temp);
            if (temp.equals("8"))
                ret = "255.0.0.0";
            else if (temp.equals("16"))
                ret = "255.255.0.0";
            else if (temp.equals("24"))
                ret = "255.255.255.0";
            return ret;
            
        }
        
       private int checkAssociations(int port){
           /** This routine checks if the association list has an association object containing
            *  the port number specified */
           int index = -1;
           int i = associations.size();
           for (int j=0;j < i;j++){
                Association a = (Association)associations.get(j);
                if (port == a.port){
                    index = j;
                    break;
                }
           }
           return index;
       }
}