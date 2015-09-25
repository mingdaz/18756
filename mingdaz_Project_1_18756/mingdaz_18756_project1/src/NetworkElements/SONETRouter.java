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
		if(destinationFrequencies.values().contains(wavelength)){
			// check if  the frame is on the router drop frequency 
			if(dropFrequency.contains(wavelength)){
			// if so, check if the frame is also the router destination frequency 
				sink(frame,wavelength);
			}
			else{
				// if the frequency does not match any drop frequency then we forward it. 
				sendRingFrameOp(frame,wavelength,nic);	
			}
		}
		else{
			// do not forward this message
			;
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
	
	public void sendRingFrameOp(SONETFrame frame, int wavelength, OpticalNICTA nic){
		// Loop through the interfaces sending the frame on interfaces that are on the ring
		// except the one it was received on. Basically what UPSR does
		Boolean send = false;
		Boolean flag = true;
		ArrayList<Integer> hopcount = destinationNextHop.get(wavelength); 
		int i=0;
		int h;
		
		if(nic==null){
			Boolean send1 = false;
			Boolean flag1 = true;
			Boolean send2 = false;
			Boolean flag2 = false;
			i=0;
			for(OpticalNICTA NIC:NICs){
				h = hopcount.get(i);
				if(h<=1){
					if(NIC.getIsClockwise()==flag1 && ! NIC.getHasError()&&!NIC.equals(nic) ){
						NIC.sendFrame(frame.clone(), wavelength);
						send1 = true;
					}
				}
				i++;
			}
			i=0;
			for(OpticalNICTA NIC:NICs){
				h = hopcount.get(i);
				if(h<=1){
					if(NIC.getIsClockwise()==flag2 && ! NIC.getHasError()&&!NIC.equals(nic) ){
						NIC.sendFrame(frame.clone(), wavelength);
						send2 = true;
					}
				}
				i++;
			}
			
			if(!send1){
				i=0;		
				for(OpticalNICTA NIC:NICs){
					h = hopcount.get(i);
					if(h==2){
						if(NIC.getIsClockwise()==flag1 && ! NIC.getHasError()&&!NIC.equals(nic) ){
							NIC.sendFrame(frame.clone(), wavelength);
						}
					}
					i++;
				}
			}
			
			
			if(!send2){
				i=0;		
				for(OpticalNICTA NIC:NICs){
					h = hopcount.get(i);
					if(h==2){
						if(NIC.getIsClockwise()==flag2 && ! NIC.getHasError()&&!NIC.equals(nic) ){
							NIC.sendFrame(frame.clone(), wavelength);
						}
					}
					i++;
				}
			}
			
			
		}
		else{
			flag = nic.getIsClockwise();
			i=0;
			for(OpticalNICTA NIC:NICs){
				h = hopcount.get(i);
				if(h<=1){
					if(NIC.getIsClockwise()==flag && ! NIC.getHasError()&&!NIC.equals(nic) ){
						NIC.sendFrame(frame.clone(), wavelength);
						send = true;
					}
				}
				i++;
			}
			if(!send){
				i=0;		
				for(OpticalNICTA NIC:NICs){
					h = hopcount.get(i);
					if(h==2){
						if(NIC.getIsClockwise()==flag && ! NIC.getHasError()&&!NIC.equals(nic) ){
							NIC.sendFrame(frame.clone(), wavelength);
						}
					}
					i++;
				}
			}
		}
	}
}
