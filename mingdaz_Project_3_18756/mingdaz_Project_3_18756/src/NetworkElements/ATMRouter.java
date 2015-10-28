/**
 * @author andy
 * @since 1.0
 * @version 1.2
 * @date 24-10-2008
 */

package NetworkElements;

import java.util.*;

import DataTypes.*;

public class ATMRouter implements IATMCellConsumer{
	private int address; // The AS address of this router
	private ArrayList<ATMNIC> nics = new ArrayList<ATMNIC>(); // all of the nics in this router
	private TreeMap<Integer, ATMNIC> nextHop = new TreeMap<Integer, ATMNIC>(); // a map of which interface to use to get to a given router on the network
	private TreeMap<Integer, NICVCPair> VCtoVC = new TreeMap<Integer, NICVCPair>(); // a map of input VC to output nic and new VC number
	private boolean trace=false; // should we print out debug code?
	private int traceID = (int) (Math.random() * 100000); // create a random trace id for cells
	private ATMNIC currentConnAttemptNIC = null; // The nic that is currently trying to setup a connection
	private boolean displayCommands = true; // should we output the commands that are received?
	// self modified
	private ArrayList<Integer> vc = new ArrayList<Integer>();
	private ArrayList<Boolean> vcfree = new ArrayList<Boolean>();
	/**
	 * The default constructor for an ATM router
	 * @param address the address of the router
	 * @since 1.0
	 */
	public ATMRouter(int address){
		this.address = address;
	}
	
	/**
	 * Adds a nic to this router
	 * @param nic the nic to be added
	 * @since 1.0
	 */
	public void addNIC(ATMNIC nic){
		this.nics.add(nic);
	}
	
	/**
	 * This method processes data and OAM cells that arrive from any nic in the router
	 * @param cell the cell that arrived at this router
	 * @param nic the nic that the cell arrived on
	 * @since 1.0
	 */
	public void receiveCell(ATMCell cell, ATMNIC nic){
		if(trace)
			System.out.println("Trace (ATMRouter): Received a cell " + cell.getTraceID());
		
		int dest_address;
		int vc;
		ATMNIC nextnic = null;
		ATMCell conn = null;
		NICVCPair newpair = null;
		
		if(cell.getIsOAM()){
			// What's OAM for?
			String[] command = cell.getData().split(" ");
			
			switch (command[0]) {
	         case "setup":
	        	 dest_address = Integer.parseInt(command[1]);
	        	 
	        	 if (this.currentConnAttemptNIC == null){
	        		 // rec setup
	        		 this.receivedSetup(cell);
	        		 // snd callpro
	        		 conn = new ATMCell(0, "call proceeding" , this.getTraceID());
	        		 nextnic = this.nextHop.get(dest_address);
	        		 conn.setIsOAM(true);
	        		 this.sentCallProceeding(conn);
	        		 nic.sendCell(conn , this);
	        		if (dest_address != this.address){
	     	        	 // snd setup
	        			 this.setcurrentConnAttemptNIC(nic);
	        			 nextnic.sendCell(cell,this);
	        			 this.sentSetup(cell);
	        		 }else{	        			 
	        			 vc = this.getVCnum();
			        	 this.setVCtoVC(vc,null);
			        	 
	        			 this.decideVC(vc);
	        			 conn = new ATMCell(vc, "connect " + vc  , this.getTraceID());
	        			 conn.setIsOAM(true);
	        			 this.sentConnect(conn);
			        	 nic.sendCell(conn , this);
	        		 }
	        	 } else{
	        		 conn = new ATMCell(0, "wait " + dest_address , this.getTraceID());
	        		 conn.setIsOAM(true);
		        	 this.sentWait(conn);
		        	 nic.sendCell(conn , this);
	        	 }
	        	 break;
	         case "wait":
	        	 dest_address = Integer.parseInt(command[1]);
	        	 	 // rec wait
	        		 this.receivedWait(cell);
	        		 // snd callpro
	        		 conn = new ATMCell(0, "setup "+ dest_address , this.getTraceID());
	        		 conn.setIsOAM(true);
	        		 this.sentSetup(conn);
	        		 nic.sendCell(conn , this);
	        	 break;
	         case "call":
	        	 this.receivedCallProceeding(cell);
	        	 break;
	         case "connect":
	        	 if (command[1].equals("ack")){
	        		 this.receiveConnectAck(cell);
	        	 }else{
	        		 // receive connect
		        	 this.receivedConnect(cell);
		        	 // store vc nicvc pair
		        	 vc = Integer.parseInt(command[1]); //output vc
		        	 newpair = new NICVCPair(nic,vc);
		        	 vc = this.getVCnum();	// input vc
		        	 this.setVCtoVC(vc,newpair);
		        	 // sent connect
		        	 conn = new ATMCell(vc, "connect " + vc , this.getTraceID());
	    			 nextnic = this.currentConnAttemptNIC;
	    			 this.setcurrentConnAttemptNIC(null);
	    			 conn.setIsOAM(true);
	    			 this.sentConnect(conn);
	    			 nextnic.sendCell(conn , this);
	    			 // sent ack
	    			 conn = new ATMCell(cell.getVC(), "connect ack" , this.getTraceID());
	    			 conn.setIsOAM(true);
	    			 this.sentConnectAck(conn);
	    			 nic.sendCell(conn , this);
	        	 }
	        	 break;
	         case "end":
	        	 if (command[1].equals("ack")){
	        		 this.receivedEndAck(cell);
	        	 }else{
	        		 // receive end
		        	 this.recieveEnd(cell);
		        	 // delete vc nicvc pair
		        	 vc = Integer.parseInt(command[1]); //input vc
		        	 if(this.VCtoVC.containsKey(vc)){
		        		// release vc
		    			 this.releaseVC(vc);
			        	 newpair = this.VCtoVC.remove(vc);
			        	 if(newpair!=null){
			        	 // sent end
			        	 conn = new ATMCell(newpair.getVC(), "end " + newpair.getVC() , this.getTraceID());
		    			 nextnic = newpair.getNIC();
		    			 conn.setIsOAM(true);
		    			 this.sentEnd(conn);
		    			 nextnic.sendCell(conn , this);
			        	 }
		    			 // sent ack
		    			 conn = new ATMCell(cell.getVC(), "end ack" , this.getTraceID());
		    			 conn.setIsOAM(true);
		    			 this.sentEndAck(conn);
		    			 nic.sendCell(conn , this);
		        	 }else{
		        		 this.cellNoVC(cell);
		        	 }	    			 
	        	 }
	        	 break;
	         default:
	             break;
	     }
		}
		else{
			// find the nic and new VC number to forward the cell on
			// otherwise the cell has nowhere to go. output to the console and drop the cell
			vc = cell.getVC();
			newpair = this.VCtoVC.get(vc);
			if(newpair==null){
				this.cellDeadEnd(cell);
			}else{
				nextnic = newpair.getNIC();
				vc = newpair.getVC();
				conn = new ATMCell(vc, cell.getData(), cell.getTraceID());
				nextnic.sendCell(conn,this);
			}
		}		
	}
	

	/**
	 * Gets the number from the end of a string
	 * @param string the sting to try and get a number from
	 * @return the number from the end of the string, or -1 if the end of the string is not a number
	 * @since 1.0
	 */
	private int getIntFromEndOfString(String string){
		// Try getting the number from the end of the string
		try{
			String num = string.split(" ")[string.split(" ").length-1];
			return Integer.parseInt(num);
		}
		// Couldn't do it, so return -1
		catch(Exception e){
			if(trace)
				System.out.println("Could not get int from end of string");
			return -1;
		}
	}
	
	/**
	 * This method returns a sequentially increasing random trace ID, so that we can
	 * differentiate cells in the network
	 * @return the trace id for the next cell
	 * @since 1.0
	 */
	public int getTraceID(){
		int ret = this.traceID;
		this.traceID++;
		return ret;
	}
	
	/**
	 * Tells the router the nic to use to get towards a given router on the network
	 * @param destAddress the destination address of the ATM router
	 * @param outInterface the interface to use to connect to that router
	 * @since 1.0
	 */
	public void addNextHopInterface(int destAddress, ATMNIC outInterface){
		this.nextHop.put(destAddress, outInterface);
	}
	
	/**
	 * Makes each nic move its cells from the output buffer across the link to the next router's nic
	 * @since 1.0
	 */
	public void clearOutputBuffers(){
		for(int i=0; i<this.nics.size(); i++)
			this.nics.get(i).clearOutputBuffers();
	}
	
	/**
	 * Makes each nic move all of its cells from the input buffer to the output buffer
	 * @since 1.0
	 */
	public void clearInputBuffers(){
		for(int i=0; i<this.nics.size(); i++)
			this.nics.get(i).clearInputBuffers();
	}
	
	/**
	 * Sets the nics in the router to use tail drop as their drop mechanism
	 * @since 1.0
	 */
	public void useTailDrop(){
		for(int i=0; i<this.nics.size(); i++)
			nics.get(i).setIsTailDrop();
	}
	
	/**
	 * Sets the nics in the router to use RED as their drop mechanism
	 * @since 1.0
	 */
	public void useRED(){
		for(int i=0; i<this.nics.size(); i++)
			nics.get(i).setIsRED();
	}
	
	/**
	 * Sets the nics in the router to use PPD as their drop mechanism
	 * @since 1.0
	 */
	public void usePPD(){
		for(int i=0; i<this.nics.size(); i++)
			nics.get(i).setIsPPD();
	}
	
	/**
	 * Sets the nics in the router to use EPD as their drop mechanism
	 * @since 1.0
	 */
	public void useEPD(){
		for(int i=0; i<this.nics.size(); i++)
			nics.get(i).setIsEPD();
	}
	
	/**
	 * Sets if the commands should be displayed from the router in the console
	 * @param displayComments should the commands be displayed or not?
	 * @since 1.0
	 */
	public void displayCommands(boolean displayCommands){
		this.displayCommands = displayCommands;
	}
	
	/**
	 * Outputs to the console that a cell has been dropped because it reached its destination
	 * @since 1.0
	 */
	public void cellDeadEnd(ATMCell cell){
		if(this.displayCommands)
		System.out.println("The cell is destined for this router (" + this.address + "), taken off network " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a cell has been dropped as no such VC exists
	 * @since 1.0
	 */
	public void cellNoVC(ATMCell cell){
		if(this.displayCommands)
		System.out.println("The cell is trying to be sent on an incorrect VC " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect message has been sent
	 * @since 1.0
	 */
	private void sentSetup(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND SETUP: Router " +this.address+ " sent a setup " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a setup message has been sent
	 * @since 1.0
	 */
	private void receivedSetup(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC SETUP: Router " +this.address+ " received a setup message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a call proceeding message has been received
	 * @since 1.0
	 */
	private void receivedCallProceeding(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC CALLPRO: Router " +this.address+ " received a call proceeding message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect message has been sent
	 * @since 1.0
	 */
	private void sentConnect(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND CONN: Router " +this.address+ " sent a connect message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect message has been received
	 * @since 1.0
	 */
	private void receivedConnect(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC CONN: Router " +this.address+ " received a connect message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect ack message has been sent
	 * @since 1.0
	 * @version 1.2
	 */
	private void sentConnectAck(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND CALLACK: Router " +this.address+ " sent a connect ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect ack message has been received
	 * @since 1.0
	 */
	private void receiveConnectAck(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC CALLACK: Router " +this.address+ " received a connect ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an call proceeding message has been received
	 * @since 1.0
	 */
	private void sentCallProceeding(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND CALLPRO: Router " +this.address+ " sent a call proceeding message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end message has been sent
	 * @since 1.0
	 */
	private void sentEnd(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND ENDACK: Router " +this.address+ " sent an end message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end message has been received
	 * @since 1.0
	 */
	private void recieveEnd(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC ENDACK: Router " +this.address+ " received an end message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end ack message has been received
	 * @since 1.0
	 */
	private void receivedEndAck(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC ENDACK: Router " +this.address+ " received an end ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end ack message has been sent
	 * @since 1.0
	 */
	private void sentEndAck(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND ENDACK: Router " +this.address+ " sent an end ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a wait message has been sent
	 * @since 1.0
	 */
	private void sentWait(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND WAIT: Router " +this.address+ " sent a wait message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a wait message has been received
	 * @since 1.0
	 */
	private void receivedWait(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC WAIT: Router " +this.address+ " received a wait message " + cell.getTraceID());
	}
	
//	self define function
	private void setcurrentConnAttemptNIC(ATMNIC nic){
		this.currentConnAttemptNIC = nic;
	}
	
	private int getVCnum(){
		int size = this.vc.size();
		if(size==0){
			this.vc.add(1);
			this.vcfree.add(false);
			return 1;
		}
		for(int i=0;i<size;i++){
			if(this.vcfree.get(i)){
				this.vcfree.set(i,false);
				return this.vc.get(i);
			}
		}
		this.vc.add(size+1);
		this.vcfree.add(false);		
		return size+1;
	}
	
	private void decideVC(int vc){
		if(this.displayCommands)
		System.out.println("Trace (ATMRouter): First free VC = " + vc);
	}

	public NICVCPair getVCtoVC(int vc) {
		return this.VCtoVC.get(vc);
	}

	public void setVCtoVC(int vc,NICVCPair nicvcpair) {
		this.VCtoVC.put(vc,nicvcpair);
	}
	
	private void releaseVC(int vc) {
		// TODO Auto-generated method stub
		int size = this.vc.size();
		int ind = this.vc.indexOf(vc);
		if(size-1==ind){
			this.vc.remove(ind);
			this.vcfree.remove(ind);
		}
		else{
			this.vcfree.set(ind,true);
		}
	}
}
