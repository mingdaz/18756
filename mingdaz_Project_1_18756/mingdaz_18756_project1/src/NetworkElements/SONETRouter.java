package NetworkElements;

import DataTypes.*;

import java.util.*;

public class SONETRouter extends SONETRouterTA{
	/**
	 * Construct a new SONET router with a given address
	 * @param	address the address of the new SONET router
	 */
	public SONETRouter(String address){
		super(address);
	}
	
	/**
	 * This method processes a frame when it is received from any location (including being created on this router
	 * from the source method). It either drops the frame from the line, or forwards it around the ring
	 * @param	frame the SONET frame to be processed
	 * @param	wavelength the wavelength the frame was received on
	 * @param	nic the NIC the frame was received on
	 */
	public void receiveFrame(SONETFrame frame, int wavelength, OpticalNICTA nic){
		if(dropFrequency.contains(wavelength)){
			// check if  the frame is on the router drop frequency 
			if(destinationFrequencies.values().contains(wavelength)){
				// if so, check if the frame is also the router destination frequency 
				sink(frame,wavelength);
			}
			else{
				// take it off the line
				// don't know what to do now;
				;
			}
		}
		else{
			// if the frequency does not match any drop frequency then we forward it. 
			sendRingFrame(frame,wavelength,nic);
		}
	}
	
	/**
	 * Sends a frame out onto the ring that this SONET router is joined to
	 * @param	frame the frame to be sent
	 * @param	wavelength the wavelength to send the frame on
	 * @param	nic the wavelength this frame originally came from (as we don't want to send it back to the sender)
	 */
	public void sendRingFrame(SONETFrame frame, int wavelength, OpticalNICTA nic){
		// Loop through the interfaces sending the frame on interfaces that are on the ring
		// except the one it was received on. Basically what UPSR does
		for(OpticalNICTA NIC:NICs)
			if(NIC.getIsOnRing() && !NIC.equals(nic))
				NIC.sendFrame(frame.clone(), wavelength);
	}
}
