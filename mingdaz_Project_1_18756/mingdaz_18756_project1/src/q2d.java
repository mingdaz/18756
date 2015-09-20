import DataTypes.SONETFrame;
import DataTypes.SPE;
import NetworkElements.OpticalNICTA;
import NetworkElements.OtoOLink;
import NetworkElements.SONETRouter;


public class q2c {
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
		nicRouter121.setClockwise(true);	
		OpticalNICTA nicRouter131 = new OpticalNICTA(router1);
		nicRouter131.setID(131);
		nicRouter131.setClockwise(true);
		OpticalNICTA nicRouter211 = new OpticalNICTA(router2);
		nicRouter211.setID(211);
		nicRouter211.setClockwise(true);
		OpticalNICTA nicRouter231 = new OpticalNICTA(router2);
		nicRouter231.setID(231);
		nicRouter231.setClockwise(true);
		OpticalNICTA nicRouter311 = new OpticalNICTA(router3);
		nicRouter311.setID(311);
		nicRouter311.setClockwise(true);
		OpticalNICTA nicRouter321 = new OpticalNICTA(router3);
		nicRouter321.setID(321);
		nicRouter321.setClockwise(true);
		// protection NIC, clockwise false
		OpticalNICTA nicRouter122 = new OpticalNICTA(router1);
		nicRouter122.setID(122);
		nicRouter121.setClockwise(false);
		OpticalNICTA nicRouter132 = new OpticalNICTA(router1);
		nicRouter132.setID(132);
		nicRouter121.setClockwise(false);
		OpticalNICTA nicRouter212 = new OpticalNICTA(router2);
		nicRouter212.setID(212);
		nicRouter121.setClockwise(false);
		OpticalNICTA nicRouter232 = new OpticalNICTA(router2);
		nicRouter232.setID(232);
		nicRouter121.setClockwise(false);
		OpticalNICTA nicRouter312 = new OpticalNICTA(router3);
		nicRouter312.setID(312);
		nicRouter121.setClockwise(false);
		OpticalNICTA nicRouter322 = new OpticalNICTA(router3);
		nicRouter322.setID(322);
		nicRouter121.setClockwise(false);
		
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
	
		/*
		 * Sent a frame on the network
		 */
		router1.source(new SONETFrame(new SPE(0)), 1490);
	}
	
	public static void main(String args[]){
		q2c go = new q2c();
		go.twoRings();
	}
}
