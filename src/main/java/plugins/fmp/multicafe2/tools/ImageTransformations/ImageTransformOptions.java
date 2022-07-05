package plugins.fmp.multicafe2.tools.ImageTransformations;

import java.awt.Color;
import java.util.ArrayList;

import icy.image.IcyBufferedImage;
import plugins.fmp.multicafe2.experiment.SequenceCamData;


public class ImageTransformOptions 
{
	public EnumImageTransformations transformOption; 
	public IcyBufferedImage referenceImage = null;
	public IcyBufferedImage secondImage = null;
	public SequenceCamData seqCamData = null;
	public int npixels_changed = 0;
	
	public int xfirst;
	public int xlast;
	public int yfirst;
	public int ylast;
	public int channel0;
	public int channel1;
	public int channel2;
	public int w0 = 1;
	public int w1 = 1;
	public int w2 = 1;
	public int spanDiff = 3;
	public int simplethreshold = 255;
		
	protected int colorthreshold = 0;
	protected int colordistanceType = 0;
	protected boolean ifGreater = true;
	
	protected final byte byteFALSE = 0;
	protected final byte byteTRUE = (byte) 0xFF;
	protected ArrayList<Color> colorarray = null;
	
	
	public void setSingleThreshold (int simplethreshold, boolean ifGreater) 
	{
		this.simplethreshold = simplethreshold;
		this.ifGreater = ifGreater;
	}
	
	public void setColorArrayThreshold (int colordistanceType, int colorthreshold, ArrayList<Color> colorarray) 
	{
		transformOption = EnumImageTransformations.THRESHOLD_COLORS;
		this.colordistanceType = colordistanceType;
		this.colorthreshold = colorthreshold;
		this.colorarray = colorarray;
	}
}
