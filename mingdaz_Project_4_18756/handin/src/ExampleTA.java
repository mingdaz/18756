/**
 * @author Andrew Fox
 */

import DataTypes.Packet;
import NetworkElements.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class ExampleTA {
	// This object will be used to move time forward on all objects
	private ArrayList<Computer> allSourceConsumers = new ArrayList<Computer>();
	private ArrayList<Computer> allDestinationConsumers = new ArrayList<Computer>();
	private int time = 0;
	Switch s;

	/**
	 * Create a network and creates connections
	 * @since 1.0
	 */
	public void go(int numComputers,int averageL,boolean isinput){
		System.out.println("** SYSTEM SETUP **");

//		int numComputers=64;
//		int averageL = 20;
		int exptime = 60;

		// Create Source Computers
		for (int i=0;i<numComputers;i++){
			Computer c=new Computer(i);
			NIC nic = new NIC(c);
			allSourceConsumers.add(c);
		}

		// Create Destination Computers
		for (int i=0;i<numComputers;i++){
			Computer c=new Computer(i);
			NIC nic = new NIC(c);
			allDestinationConsumers.add(c);
		}

		// Create the Switch
		s = new Switch(numComputers);
		if(isinput)
			s.setInputQueue();
		else
			s.setOutputQueue();
		s.setSwitchBufferSize(6400);

		// connect the computers to the links
		int j=0;
		for(Computer c:allSourceConsumers){
			OtoOLink l=new OtoOLink(c.getNIC(),s.getInputNICs().get(j));
			j++;
		}
		j=0;
		for(Computer c:allDestinationConsumers){
			OtoOLink l=new OtoOLink(c.getNIC(),s.getOutputNICs().get(j));
			j++;
		}




		// Load packet form dat files. Ensure the path is correct.
		try {
			getTestcase(numComputers,averageL);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<exptime; i++)
			this.tock();

	}

	/**
	 * moves time forward in all of the networks objects, so that packets take some amount of time to
	 * travel from once place to another
	 */
	public void tock(){
		System.out.println("** TIME = " + time + " **");
		time++;
		
		// send packets from all input computers to the switch
		for(int i=0; i<this.allSourceConsumers.size(); i++)
			allSourceConsumers.get(i).sendFromBuffer();
		s.sendFromOutputs();		// send packets from the output of the switch to destination computer
		s.sendFromBuffer();			// send packets across the switch
		// clears the buffers of the destination routers
		for(int i=0; i<this.allDestinationConsumers.size(); i++)
			allDestinationConsumers.get(i).clearBuffer();
		update();		
	}
	
	// self define function update
	public void update(){
		for(Computer c:this.allSourceConsumers){
			NIC nic = c.getNIC();
			ArrayList<Packet> ps = nic.getBuffer();
			for(Packet p:ps){
				p.addDelay(1);
			}		
		}
		ArrayList<NIC> sinput = s.getInputNICs();
		for(NIC nic:sinput){
			ArrayList<Packet> ps = nic.getBuffer();
			for(Packet p:ps){
				p.addDelay(1);
			}		
		}
		ArrayList<NIC> soutput = s.getOutputNICs();
		for(NIC nic:soutput){
			ArrayList<Packet> ps = nic.getBuffer();
			for(Packet p:ps){
				p.addDelay(1);
			}		
		}
		for(Computer c:this.allDestinationConsumers){
			NIC nic = c.getNIC();
			ArrayList<Packet> ps = nic.getBuffer();
			for(Packet p:ps){
				p.addDelay(1);
			}		
		}
	}
	
	public void getStatic(){
		
		
	}
	
	public void generateTestcase(int numComputers,int repeat) throws IOException{
		String filename = String.format("testcase/case%dx%d.dat", numComputers , repeat);
		int[] multi = new int[numComputers*repeat];
		ObjectOutputStream myStream = new ObjectOutputStream(new FileOutputStream(filename));
		Random rnd = new Random();
		for(int i=0;i<numComputers;i++){
			for(int j=0;j<repeat;j++){
				int tmp = rnd.nextInt(numComputers);
				multi[i*repeat+j] = tmp;
			}
		}
		myStream.writeObject(multi);
        myStream.close();
	}
	
	public void getTestcase(int numComputers,int repeat) throws ClassNotFoundException, IOException{
		String filename = String.format("testcase/case%dx%d.dat", numComputers , repeat);
		FileInputStream File;
		try {
			File = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			this.generateTestcase(numComputers,repeat);
			File = new FileInputStream(filename);
		
		}
		ObjectInputStream multi = new ObjectInputStream(File);
        int[] array = (int[]) multi.readObject();
		for(int i=0;i<numComputers;i++){
			for(int j=0;j<repeat;j++){
				allSourceConsumers.get(i).sendPacket(array[i*repeat+j]);
			}
		}  
		multi.close();
	}
	
	public static void main(String args[]){
		
		ExampleTA go = new ExampleTA();
		go.go(2,20,false);		
		
//		int[] rep = {2,4,8,16,32,64};
		
//		for(int i:rep){
//			ExampleTA go = new ExampleTA();
//			go.go(i,100,true);
//			try {
//				System.in.read();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			go = new ExampleTA();
//			go.go(i,100,false);
//			try {
//				System.in.read();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		//generating testcase dat file;
//		int[] rep = {10,20,50,100};
//		for (int i=2;i<=64;i*=2){
//			for(int r:rep){
//				try {
//					go.generateTestcase(i,r);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//			}
//		}
	}
}
