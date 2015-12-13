package DataTypes;

public class SONETFrame extends SONETFrameTA{
	/**
	 * Create a new SONET frame to be sent on the network
	 * @param	spe the spe inside the frame
	 */
	public SONETFrame(SPE spe){
		super(spe);
	}
	
	public SONETFrame(SPE spe,String flag){
		super(spe);
		this.setOAMFlags(flag);
	}

	/**
	 * Increases the delay that this frame has encountered during it's travel
	 * @param	delay the additional delay to be added
	 */
	public void addDelay(int delay){
		this.delay += delay;
	}
	
	public SONETFrame clone(){
		SONETFrame newframe = new SONETFrame(this.getSPE().clone());
		newframe.delay = 0;
		newframe.setOAMFlags(this.getOAMFlags());
        return newframe;
	}
}
