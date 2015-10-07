package NetworkElements;

import java.util.*;
import java.net.*;

import DataTypes.*;

public class IPRouter implements IPConsumer{
	private ArrayList<IPNIC> nics = new ArrayList<IPNIC>();
	private HashMap<Inet4Address, IPNIC> forwardingTable = new HashMap<Inet4Address, IPNIC>();
	private int time = 0;
	private Boolean fifo=true, rr=false, wrr=false, wfq=false, routeEntirePacket=true;
	private HashMap<IPNIC, FIFOQueue> inputQueues = new HashMap<IPNIC, FIFOQueue>();
	private int lastNicServiced=-1, weightFulfilled=1;
	// remembering the queue rather than the interface number is useful for wfq
	private FIFOQueue lastServicedQueue = null;
	private double virtualTime = 0.0;
	
	// self define variable for fifo
	private IPPacket lastServerPacket = null;
	private FIFOQueue fifoQueue = null;
	private int lastServicedSize = 0; 
	
	// self define variable for bit-round robin
//	private int lastservenic = 0;
	private Iterator<FIFOQueue> rr_queues;
	private Iterator<Integer> rr_size;
	private HashMap<IPNIC, Integer> rrQueue = new HashMap<IPNIC, Integer>();
	
	/**
	 * The default constructor of a router
	 */
	public IPRouter(){
		
	}
	
	/**
	 * adds a forwarding address in the forwarding table
	 * @param destAddress the address of the destination
	 * @param nic the nic the packet should be sent on if the destination address matches
	 */
	public void addForwardingAddress(Inet4Address destAddress, IPNIC nic){
		forwardingTable.put(destAddress, nic);
	}
	
	/**
	 * receives a packet from the NIC
	 * @param packet the packet received
	 * @param nic the nic the packet was received on
	 */
	public void receivePacket(IPPacket packet, IPNIC nic){
//		this.forwardPacket(packet);
		if(this.rr){
			if(this.inputQueues.containsKey(nic)){
				this.inputQueues.get(nic).offer(packet);
			}else{
				FIFOQueue q = new FIFOQueue();
				q.offer(packet);
				this.inputQueues.put(nic,q);
				this.rrQueue.put(nic,0);
			}
		}
		if (this.fifo){
			this.fifoQueue.offer(packet);
		}
		// If wfq set the expected finish time
		if(this.wfq){
			
		}
	}
	
	public void forwardPacket(IPPacket packet){
		forwardingTable.get(packet.getDest()).sendIPPacket(packet);
	}
	
	public void routeBit(){
		/*
		 *  FIFO scheduler
		 */
		if(this.fifo) this.fifo();
			
		
		/*
		 *  RR scheduler
		 */
		if(this.rr) this.rr();
			
		
		/*
		 *  WRR scheduler
		 */
		if(this.wrr) this.wrr();
			
		
		/*
		 * WFQ scheduler
		 */
		if(this.wfq) this.wfq();
	}
	
	/**
	 * Perform FIFO scheduler on the queue
	 */
	private void fifo(){
		
		if(this.lastServicedSize == 0 ){
			while(this.lastServicedSize == 0){
				this.lastServerPacket = this.fifoQueue.peek();
				if (this.lastServerPacket ==null) break;
				this.lastServicedSize = this.lastServerPacket.getSize();
				if(this.lastServicedSize == 0 ){
					this.forwardPacket(this.lastServerPacket);
				}
			}
			if(this.lastServicedSize != 0 ){
				this.lastServicedSize --;
			}
		} else{
			this.lastServicedSize --;
		}
		if(this.lastServicedSize == 0 && this.lastServerPacket!=null){
			this.forwardPacket(this.lastServerPacket);
			this.fifoQueue.remove();
		}
	}
	
	/**
	 * Perform round robin on the queue
	 */
	private void rr(){
//		private int lastNicServiced=-1, weightFulfilled=1;
//		private Iterator<FIFOQueue> rr_queues;
//		private Iterator<Integer> rr_size;
//		private HashMap<IPNIC, Integer> rrQueue = new HashMap<IPNIC, Integer>();
//		private HashMap<IPNIC, FIFOQueue> inputQueues = new HashMap<IPNIC, FIFOQueue>();
//		private ArrayList<IPNIC> nics = new ArrayList<IPNIC>();
		for(int i = 0;i<this.nics.size();i++){
			if (this.lastNicServiced == this.nics.size()-1){
				this.lastNicServiced = 0;
			} else{
				this.lastNicServiced ++;
			}
			IPNIC nic = this.nics.get(this.lastNicServiced);
			if(!this.inputQueues.containsKey(nic)){
				continue;
			}
			// not packet need to server
			if(this.inputQueues.get(nic).peek()==null){
				this.rrQueue.put(nic,0);
				continue;
			}
			int remainsize = this.rrQueue.get(nic);
			if (remainsize==0){
				IPPacket servepacket = this.inputQueues.get(nic).peek();
				remainsize = servepacket.getSize()-1;
			}else{
				remainsize--;
			}
			if(remainsize == 0 ){
				this.forwardPacket(this.inputQueues.get(nic).remove());
			}
			this.rrQueue.put(nic,remainsize);
			break;
		}
		
//		if(this.lastServicedSize == 0 ){
//			while(this.lastServicedSize == 0){
//				this.lastServerPacket = this.fifoQueue.peek();
//				if (this.lastServerPacket ==null) break;
//				this.lastServicedSize = this.lastServerPacket.getSize();
//				if(this.lastServicedSize == 0 ){
//					this.forwardPacket(this.lastServerPacket);
//				}
//			}
//			if(this.lastServicedSize != 0 ){
//				this.lastServicedSize --;
//			}
//		} else{
//			this.lastServicedSize --;
//		}
//		if(this.lastServicedSize == 0 && this.lastServerPacket!=null){
//			this.forwardPacket(this.lastServerPacket);
//			this.fifoQueue.remove();
//		}
	}
	
	/**
	 * Perform weighted round robin on the queue
	 */
	private void wrr(){

	}
	
	/**
	 * Perform weighted fair queuing on the queue
	 */
	private void wfq(){

	}
	
	/**
	 * adds a nic to the consumer 
	 * @param nic the nic to be added
	 */
	public void addNIC(IPNIC nic){
		this.nics.add(nic);
	}
	
	/**
	 * sets the weight of queues, used when a weighted algorithm is used.
	 * Example
	 * Nic A = 1
	 * Nic B = 4
	 * 
	 * For every 5 bits of service, A would get one, B would get 4.
	 * @param nic the nic queue to set the weight of
	 * @param weight the weight of the queue
	 */
	public void setQueueWeight(IPNIC nic, int weight){
		if(this.inputQueues.containsKey(nic))
			this.inputQueues.get(nic).setWeight(weight);
		
		else System.err.println("(IPRouter) Error: The given NIC does not have a queue associated with it");
	}
	
	/**
	 * moves time forward 1 millisecond
	 */
	public void tock(){
		this.time+=1;
		
		// Add 1 delay to all packets in queues
		ArrayList<FIFOQueue> delayedQueues = new ArrayList<FIFOQueue>();
		for(Iterator<FIFOQueue> queues = this.inputQueues.values().iterator(); queues.hasNext();){
			FIFOQueue queue = queues.next();
			if(!delayedQueues.contains(queue)){
				delayedQueues.add(queue);
				queue.tock();
			}
		}
		if(this.fifo){
			this.fifoQueue.tock();
		}
		// calculate the new virtual time for the next round
		if(this.wfq){
			
		}
		
		// route bit for this round
		this.routeBit();
	}
	
	/**
	 * set the router to use FIFO service
	 */
	public void setIsFIFO(){
		this.fifo = true;
		this.rr = false;
		this.wrr = false;
		this.wfq = false;
		
		// Setup router for FIFO under here
		this.fifoQueue = new FIFOQueue();
	}
	
	/**
	 * set the router to use Round Robin service
	 */
	public void setIsRoundRobin(){
		this.fifo = false;
		this.rr = true;
		this.wrr = false;
		this.wfq = false;
		
		// Setup router for Round Robin under here
		this.lastNicServiced =  this.nics.size()-1;
	}
	
	/**
	 * sets the router to use weighted round robin service
	 */
	public void setIsWeightedRoundRobin(){
		this.fifo = false;
		this.rr = false;
		this.wrr = true;
		this.wfq = false;
		
		// Setup router for Weighted Round Robin under here
		
	}
	
	/**
	 * sets the router to use weighted fair queuing
	 */
	public void setIsWeightedFairQueuing(){
		this.fifo = false;
		this.rr = false;
		this.wrr = false;
		this.wfq = true;
		
		// Setup router for Weighted Fair Queuing under here
		
	}
	
	/**
	 * sets if the router should route bit-by-bit, or entire packets at a time
	 * @param	routeEntirePacket if the entire packet should be routed
	 */
	public void setRouteEntirePacket(Boolean routeEntirePacket){
		this.routeEntirePacket=routeEntirePacket;
	}
}
