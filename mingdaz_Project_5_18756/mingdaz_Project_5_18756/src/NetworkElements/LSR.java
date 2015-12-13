package NetworkElements;

import java.util.*;
import DataTypes.*;

public class LSR{
	private int address; // The AS address of this router
	private ArrayList<LSRNIC> nics = new ArrayList<LSRNIC>(); // all of the nics in this router
	
	//	adding variables 
	private TreeMap<String, Integer> AddLabelpair = new TreeMap<String, Integer>(); 
	private TreeMap<Integer, LSRNIC> LabelNICpair = new TreeMap<Integer, LSRNIC>(); 
	private TreeMap<Integer, LSRNIC> AddNICpair = new TreeMap<Integer, LSRNIC>(); 
	private TreeMap<String,ArrayList<Packet> > DelayPacket = new TreeMap<String,ArrayList<Packet> >();
	private TreeMap<String,ArrayList<Packet> > DiffServQueue = new TreeMap<String,ArrayList<Packet> >();
	private TreeMap<String,Integer > Bandwidth = new TreeMap<String,Integer >();
	private TreeMap<Integer,Integer > RouteTable = new TreeMap<Integer,Integer >();
	
	private int[] dist;
	private int[] prev;
	private boolean DoneDij = false;
	private boolean trace = true;
	// store map information
	static private int count=0;
	static private int[][] MapInfo;
	static private ArrayList<Integer> AddList = new ArrayList<Integer>();
	/**
	 * The default constructor for an ATM router
	 * @param address the address of the router
	 * @since 1.0
	 */
	
	static public void connectMap(int x,int y){
		int i = AddList.indexOf(x);
		int j = AddList.indexOf(y);
		if(i<count&&j<count){
			MapInfo[i][j] = MapInfo[j][i] = 1;
		}
	}
	static private int id = 0;
	public LSR(int address){
		this.address = address;
		LSR.count++;
		LSR.AddList.add(address);
		int c = LSR.count;
		int[][] newMap = new int[c][];
		for(int i=0;i<c-1;i++){
			newMap[i] = new int[c];
			for(int j=0;j<c-1;j++){
				newMap[i][j] = LSR.MapInfo[i][j];
			}
			newMap[i][c-1] = 0;
		}
		newMap[c-1] = new int[c];
		for(int j=0;j<c-1;j++){
			newMap[c-1][j] = 0;
		}
		newMap[c-1][c-1] = 0; 
		MapInfo = newMap;
	}
	
	/**
	 * The return the router's address
	 * @since 1.0
	 */
	public int getAddress(){
		return this.address;
	}
	
	/**
	 * Adds a nic to this router
	 * @param nic the nic to be added
	 * @since 1.0
	 */
	public void addNIC(LSRNIC nic){
		this.nics.add(nic);
	}
	
	/**
	 * This method processes data and OAM cells that arrive from any nic with this router as a destination
	 * @param currentPacket the packet that arrived at this router
	 * @param nic the nic that the cell arrived on
	 * @since 1.0
	 */
	public void receivePacket(Packet currentPacket, LSRNIC nic){	
			int dest_address;
			int DSCP;
			String key;
			if(currentPacket.getIsOAM()){
				String command[] = currentPacket.getData().split(":");
//				Path,Resv,ResvConf,PathErr,ResvErr
				switch (command[0]) {
		         case "Path":
		        	 dest_address = currentPacket.getDest();
		        	 DSCP = currentPacket.getDSCP();
		        	 key = DSCP+":"+dest_address;	 
		        	 if(dest_address==this.address){
		        		 this.id++;
		        		 RouteTable.put(id, 0);
//		        		 System.out.println("table("+this.address+")"+id+":"+0);
		        		 Packet returnpacket = new Packet(currentPacket.getDest(),currentPacket.getSource(),0);
		        		 returnpacket.setOAM(true);
		        		 returnpacket.setData("Resv:"+id);
		        		 nic.sendPacket(returnpacket,this);
		        	 }else{
			        	 //already have this path
			        	 if(this.DiffServQueue.containsKey(key)){
			        		 DiffServQueue.get(key).add(currentPacket);
			        	 }
			        	 else{		        		 
			        		 int nextAdd = LSR.AddList.get(dijkstra(currentPacket.getDest()));
			        		 int label;
			        		 DSCP = currentPacket.getDSCP();
				        	 key = DSCP+":"+nextAdd;	 
			        		 LSRNIC nextnic;
			        		 if(this.AddLabelpair.containsKey(key))
			        		 {
			        			 label = this.AddLabelpair.get(key);
			        			 nextnic = this.LabelNICpair.get(label);
			        		 }
			        		 else{
			        			 nextnic = this.AddNICpair.get(nextAdd);
			        			 nextnic.sendPacket(currentPacket,this);
			        		 }
			        	 }
		        	 }
//		        	 
		        	 break;
		         case "Resv":
		        	 dest_address = currentPacket.getDest();
		        	 DSCP = currentPacket.getDSCP();
		        	 key = DSCP+":"+dest_address;
		        	 if(dest_address==this.address){
		        		 this.AddLabelpair.put(DSCP+":"+currentPacket.getSource(),Integer.parseInt(command[1]));
		        		 this.LabelNICpair.put(Integer.parseInt(command[1]), nic);
		        		 ArrayList<Packet> temp = this.DelayPacket.get(DSCP+":"+currentPacket.getSource());
		        		 if(temp != null){
		        		 for(Packet p:temp){
		        			 this.sendPacket(p);
		        		 }
		        		 this.DelayPacket.remove(DSCP+":"+currentPacket.getSource());
		        		 }
		        	 }else{
		        		 this.id++;
		        		 RouteTable.put(id, Integer.parseInt(command[1]));
//		        		 System.out.println("table("+this.address+")"+id+":"+command[1]);
		        		 LabelNICpair.put(Integer.parseInt(command[1]), nic);
		        		 currentPacket.setData("Resv:"+id);
//		        		 nic.sendPacket(currentPacket,this);
			        	 //already have this path
		        		 int nextAdd = LSR.AddList.get(dijkstra(currentPacket.getDest()));
		        		 int label;
		        		 DSCP = currentPacket.getDSCP();
			        	 key = DSCP+":"+nextAdd;
		        		 LSRNIC nextnic;
		        		 if(this.AddLabelpair.containsKey(key))
		        		 {
		        			 label = this.AddLabelpair.get(key);
		        			 nextnic = this.LabelNICpair.get(label);
		        		 }
		        		 else{
		        			 nextnic = this.AddNICpair.get(nextAdd);
		        			 nextnic.sendPacket(currentPacket,this);
		        		 }
		        	 }
		        	 break;
		         case "ResvConf":
		        	 break;
		         case "PathErr":
		        	 break;
		         case "ResvErr":
		        	 break;
		         default:
		             break;
		     }
				
			}
			else{
//				key = currentPacket.getd()+""
				this.sendPacket(currentPacket);
			}
			if(trace)
				System.out.println("Trace (LSR)("+this.address+"): Received a packet " + currentPacket.print());
				
			
			
	}
	
	/**
	 * This method creates a packet with the specified type of service field and sends it to a destination
	 * @param destination the distination router
	 * @param DSCP the differentiated services code point field
	 * @since 1.0
	 */
	public void createPacket(int destination, int DSCP){
		Packet newPacket= new Packet(this.getAddress(), destination, DSCP);
		this.sendPacket(newPacket);
	}

	/**
	 * This method allocates bandwidth for a specific traffic class
	 * @param Label MPLS label for the LSP
	 * @param PHB 0=EF, 1=AF, 2=BE
	 * @param Class AF classes 1,2,3,4.  (0 if EF or BE)
	 * @param Bandwidth number of packets per time unit for this PHB/Class
	 * @since 1.0
	 */
	public void allocateBandwidth(int dest, int PHB, int Class, int Bandwidth){
		String key = dest+":"+PHB+":"+Class;
		this.Bandwidth.put(key, Bandwidth);
	}
	
	/**
	 * This method forwards a packet to the correct nic or drops if at destination router
	 * @param newPacket The packet that has just arrived at the router.
	 * @since 1.0
	 */
	public void sendPacket(Packet newPacket) {
		int nextAdd;
		int label;
		LSRNIC nic;
		String key;


			if(newPacket.getData().length()>0){
				label = Integer.parseInt(newPacket.getData().split(":")[1]);
				label = this.RouteTable.get(label);
				newPacket.setData("label:"+label);
				nic = this.LabelNICpair.get(label);
				if(nic!=null)
					nic.sendPacket(newPacket,this);
			}
			else {
				key = newPacket.getDSCP()+":"+newPacket.getDest();
				if(this.AddLabelpair.containsKey(key))
				{
					label = this.AddLabelpair.get(key);
					newPacket.setData("label:"+label);
					nic = this.LabelNICpair.get(label);
					nic.sendPacket(newPacket,this);
				}
				else{
					nextAdd = LSR.AddList.get(dijkstra(newPacket.getDest()));
					nic = this.AddNICpair.get(nextAdd);
					Packet pathPacket = new Packet(newPacket.getSource(),newPacket.getDest(),0);
					pathPacket.setOAM(true);
					pathPacket.setData("Path");
					nic.sendPacket(pathPacket,this);
					key = newPacket.getDSCP()+":"+newPacket.getDest();
					ArrayList<Packet> Q = this.DelayPacket.get(key);
					if(Q==null){
						Q = new ArrayList<Packet>();
					}
					Q.add(newPacket);
					this.DelayPacket.put(key, Q);
				}
			}
	}

	/**
	 * Makes each nic move its cells from the output buffer across the link to the next router's nic
	 * @since 1.0
	 */
	public void sendPackets(){
		for(int i=0; i<this.nics.size(); i++)
			this.nics.get(i).sendPackets();
	}
	
	/**
	 * Makes each nic move all of its cells from the input buffer to the output buffer
	 * @since 1.0
	 */
	public void recievePackets(){
		for(int i=0; i<this.nics.size(); i++)
			this.nics.get(i).recievePackets();
	}
	
	private int dijkstra(int dest)
    {
		if(!this.DoneDij){
			int V = LSR.count;
			int src = LSR.AddList.indexOf(this.address);
//			System.out.println("src="+src);
			int[][] graph = LSR.MapInfo;
					
	        this.dist = new int[V]; // The output array. dist[i] will hold
	                                 // the shortest distance from src to i
	        this.prev = new int[V];
	 
	        // sptSet[i] will true if vertex i is included in shortest
	        // path tree or shortest distance from src to i is finalized
	        Boolean sptSet[] = new Boolean[V];
	 
	        // Initialize all distances as INFINITE and stpSet[] as false
	        for (int i = 0; i < V; i++)
	        {
	            dist[i] = Integer.MAX_VALUE;
	            sptSet[i] = false;
	        }
	 
	        // Distance of source vertex from itself is always 0
	        dist[src] = 0;
	        prev[src] = -1;
	 
	        // Find shortest path for all vertices
	        for (int count = 0; count < V-1; count++)
	        {
	            // Pick the minimum distance vertex from the set of vertices
	            // not yet processed. u is always equal to src in first
	            // iteration.
	            int u = minDistance( sptSet);
	 
	            // Mark the picked vertex as processed
	            sptSet[u] = true;
	 
	            // Update dist value of the adjacent vertices of the
	            // picked vertex.
	            for (int v = 0; v < V; v++)
	 
	                // Update dist[v] only if is not in sptSet, there is an
	                // edge from u to v, and total weight of path from src to
	                // v through u is smaller than current value of dist[v]
	                if (!sptSet[v] && graph[u][v]!=0 &&
	                        dist[u] != Integer.MAX_VALUE &&
	                        dist[u]+graph[u][v] < dist[v]){
	                	dist[v] = dist[u] + graph[u][v];
	            		prev[v] = u;
	                }
	                    
	        }
	 
	        // print the constructed distance array
//	        printSolution();x
	        this.DoneDij = true;

		}
		int local = LSR.AddList.indexOf(this.address);
		int ret = LSR.AddList.indexOf(dest);
		while(prev[ret]!=local){
			ret = prev[ret];
		}
		return ret;
    }
	
    private int minDistance(Boolean sptSet[])
    {
        // Initialize min value
    	int V = LSR.count;
        int min = Integer.MAX_VALUE, min_index=-1;
 
        for (int v = 0; v < V; v++)
            if (sptSet[v] == false && dist[v] <= min)
            {
                min = dist[v];
                min_index = v;
            }
 
        return min_index;
    }
	
    private void printSolution()
    {
    	int V = LSR.count;
        System.out.println("Vertex   Distance from Source");
        for (int i = 0; i < V; i++)
            System.out.println(i+" \t\t "+dist[i]+" \t\t "+prev[i]);
    }
    
    public void addpair(int address,LSRNIC nic){
    	this.AddNICpair.put(address, nic);
    }
}
