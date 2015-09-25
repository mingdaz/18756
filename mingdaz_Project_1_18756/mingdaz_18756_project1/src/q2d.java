import java.util.Arrays;
import java.util.ArrayList;
import DataTypes.SONETFrame;
import DataTypes.SPE;
import NetworkElements.OpticalNICTA;
import NetworkElements.OtoOLink;
import NetworkElements.SONETRouter;


public class q2d {
	public void twoRings(){
		System.out.println("Setting up two rings");
		// Create three SONET routers
		SONETRouter router1 = new SONETRouter("00:11:22");
		SONETRouter router2 = new SONETRouter("88:77:66");
		SONETRouter router3 = new SONETRouter("33:44:55");
			
		// tell routers a wavelength to add/drop on (in this case their own frequencies)
		router1.addDropWavelength(1310);
		router2.addDropWavelength(1490);
		router3.addDropWavelength(1550);
		
		// tell router 1 the wavelength each router is add/dropping on
		router1.addDestinationFrequency("00:11:22", 1310);
		router1.addDestinationFrequency("88:77:66", 1490);
		router1.addDestinationFrequency("33:44:55", 1550);
		
		// tell router 2 the wavelength each router is add/dropping on
		router2.addDestinationFrequency("00:11:22", 1310);
		router2.addDestinationFrequency("88:77:66", 1490);
		router2.addDestinationFrequency("33:44:55", 1550);
		
		// tell router 3 the wavelength each router is add/dropping on
		router3.addDestinationFrequency("00:11:22", 1310);
		router3.addDestinationFrequency("88:77:66", 1490);
		router3.addDestinationFrequency("33:44:55", 1550);
		
		
				
		// Create an interface for each router
		// working NIC,clockwise true
		OpticalNICTA nicRouter121 = new OpticalNICTA(router1);
		nicRouter121.setID(121);
		OpticalNICTA nicRouter131 = new OpticalNICTA(router1);
		nicRouter131.setID(131);
		OpticalNICTA nicRouter211 = new OpticalNICTA(router2);
		nicRouter211.setID(211);
		OpticalNICTA nicRouter231 = new OpticalNICTA(router2);
		nicRouter231.setID(231);
		OpticalNICTA nicRouter311 = new OpticalNICTA(router3);
		nicRouter311.setID(311);
		OpticalNICTA nicRouter321 = new OpticalNICTA(router3);
		nicRouter321.setID(321);
		// protection NIC, clockwise false
		OpticalNICTA nicRouter122 = new OpticalNICTA(router1);
		nicRouter122.setID(122);
		OpticalNICTA nicRouter132 = new OpticalNICTA(router1);
		nicRouter132.setID(132);
		OpticalNICTA nicRouter212 = new OpticalNICTA(router2);
		nicRouter212.setID(212);
		OpticalNICTA nicRouter232 = new OpticalNICTA(router2);
		nicRouter232.setID(232);
		OpticalNICTA nicRouter312 = new OpticalNICTA(router3);
		nicRouter312.setID(312);
		OpticalNICTA nicRouter322 = new OpticalNICTA(router3);
		nicRouter322.setID(322);
		

		
		//set direction
		nicRouter121.setClockwise(true);
		nicRouter131.setClockwise(true);
		nicRouter211.setClockwise(true);
		nicRouter231.setClockwise(true);
		nicRouter311.setClockwise(true);
		nicRouter321.setClockwise(true);
		
		//set protection pointer
		nicRouter121.setIsProtection(nicRouter122);
		nicRouter131.setIsProtection(nicRouter132);
		nicRouter211.setIsProtection(nicRouter212);
		nicRouter231.setIsProtection(nicRouter232);
		nicRouter311.setIsProtection(nicRouter312);
		nicRouter321.setIsProtection(nicRouter322);
		//set working pointer
		nicRouter122.setIsWorking(nicRouter121);
		nicRouter132.setIsWorking(nicRouter131);
		nicRouter212.setIsWorking(nicRouter211);
		nicRouter232.setIsWorking(nicRouter231);
		nicRouter312.setIsWorking(nicRouter311);
		nicRouter322.setIsWorking(nicRouter321);	
		
		// Create two-uni directional links between the routers
		// Create ring 1
		OtoOLink OneToTwo1 = new OtoOLink(nicRouter121, nicRouter211);
		OtoOLink TwoToOne1 = new OtoOLink(nicRouter211, nicRouter121);
		OtoOLink OneToThree1 = new OtoOLink(nicRouter131, nicRouter311);
		OtoOLink ThreeToOne1 = new OtoOLink(nicRouter311, nicRouter131);
		OtoOLink TwoToThree1 = new OtoOLink(nicRouter231, nicRouter321);
		OtoOLink ThreeToTwo1 = new OtoOLink(nicRouter321, nicRouter231);
	
		//Creat ring 2
		OtoOLink OneToTwo2 = new OtoOLink(nicRouter122, nicRouter212);
		OtoOLink TwoToOne2 = new OtoOLink(nicRouter212, nicRouter122);
		OtoOLink OneToThree2 = new OtoOLink(nicRouter132, nicRouter312);
		OtoOLink ThreeToOne2 = new OtoOLink(nicRouter312, nicRouter132);
		OtoOLink TwoToThree2 = new OtoOLink(nicRouter232, nicRouter322);
		OtoOLink ThreeToTwo2 = new OtoOLink(nicRouter322, nicRouter232);
	
		//setInandoutlink
		nicRouter121.setInLink(TwoToOne1);
		nicRouter121.setOutLink(OneToTwo1);
		nicRouter122.setInLink(TwoToOne2);
		nicRouter122.setOutLink(OneToTwo2);
		nicRouter131.setInLink(ThreeToOne1);;
		nicRouter131.setOutLink(OneToThree1);
		nicRouter132.setInLink(ThreeToOne2);
		nicRouter132.setOutLink(OneToThree2);
		nicRouter211.setInLink(OneToTwo1);
		nicRouter211.setOutLink(TwoToOne1);
		nicRouter212.setInLink(OneToTwo2);
		nicRouter212.setOutLink(TwoToOne2);
		nicRouter231.setInLink(ThreeToTwo1);
		nicRouter231.setOutLink(TwoToThree1);
		nicRouter232.setInLink(ThreeToTwo2);
		nicRouter232.setOutLink(TwoToThree2);		
		nicRouter311.setInLink(OneToThree1);
		nicRouter311.setOutLink(ThreeToOne1);
		nicRouter312.setInLink(OneToThree2);
		nicRouter312.setOutLink(ThreeToOne2);
		nicRouter321.setInLink(TwoToThree1);
		nicRouter321.setOutLink(ThreeToTwo1);
		nicRouter322.setInLink(TwoToThree2);
		nicRouter322.setOutLink(ThreeToTwo2);
		
		// tell router 1 the nexthop
		router1.addDestinationHopCount(1490, new ArrayList<Integer>(Arrays.asList(0,0,0,0)));
		router1.addDestinationHopCount(1550, new ArrayList<Integer>(Arrays.asList(0,0,0,0)));
		// tell router 2 the nexthop
		router2.addDestinationHopCount(1310, new ArrayList<Integer>(Arrays.asList(0,0,0,0)));
		router2.addDestinationHopCount(1550, new ArrayList<Integer>(Arrays.asList(0,0,0,0)));
		// tell router 3 the nexthop
		router3.addDestinationHopCount(1310, new ArrayList<Integer>(Arrays.asList(0,0,0,0)));
		router3.addDestinationHopCount(1490, new ArrayList<Integer>(Arrays.asList(0,0,0,0)));
		
		System.out.println("Create hop table:");
		router1.source(new SONETFrame(new SPE(0),"1310:0"), 1490);
		router1.source(new SONETFrame(new SPE(0),"1310:0"), 1550);
		router2.source(new SONETFrame(new SPE(0),"1490:0"), 1310);
		router2.source(new SONETFrame(new SPE(0),"1490:0"), 1550);
		router3.source(new SONETFrame(new SPE(0),"1550:0"), 1310);
		router3.source(new SONETFrame(new SPE(0),"1550:0"), 1490);
		System.out.println("---------------------------------");
		
		
		// tell router 1 the nexthop
		router1.addDestinationHopCount(1490, new ArrayList<Integer>(Arrays.asList(1,2,1,2)));
		router1.addDestinationHopCount(1550, new ArrayList<Integer>(Arrays.asList(2,1,2,1)));
		// tell router 2 the nexthop
		router2.addDestinationHopCount(1310, new ArrayList<Integer>(Arrays.asList(1,2,1,2)));
		router2.addDestinationHopCount(1550, new ArrayList<Integer>(Arrays.asList(2,1,2,1)));
		// tell router 3 the nexthop
		router3.addDestinationHopCount(1310, new ArrayList<Integer>(Arrays.asList(1,2,1,2)));
		router3.addDestinationHopCount(1490, new ArrayList<Integer>(Arrays.asList(2,1,2,1)));
		
		/*
		 * Sent a frame on the network
		 */
		System.out.println("test1:");
		router1.source(new SONETFrame(new SPE(0)), 1490);
		System.out.println("---------------------------------");
		
		System.out.println("test2:");
		OneToTwo1.cutLink();
		TwoToOne1.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		System.out.println("---------------------------------");
		
		System.out.println("test3:");
		OneToTwo2.cutLink();
		TwoToOne2.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		System.out.println("---------------------------------");
		
		System.out.println("test4:");
		OneToTwo2.uncutLink();
		TwoToOne2.uncutLink();
		ThreeToTwo2.cutLink();
		TwoToThree2.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		System.out.println("---------------------------------");
		
		System.out.println("test5:");
		ThreeToTwo1.cutLink();
		TwoToThree1.cutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
		System.out.println("---------------------------------");
		
		System.out.println("test6:");
		OneToThree1.cutLink();
		ThreeToOne1.cutLink();
		ThreeToTwo1.uncutLink();
		TwoToThree1.uncutLink();
		ThreeToTwo2.uncutLink();
		TwoToThree2.uncutLink();
		router1.source(new SONETFrame(new SPE(0)), 1490);
	}
	
	public static void main(String args[]){
		q2d go = new q2d();
		go.twoRings();
	}
}
