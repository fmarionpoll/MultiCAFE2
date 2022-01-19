package plugins.fmp.multicafe2.tools.ImageTransformations;


public enum EnumImageTransformations {
	R_RGB			("R(RGB)", 					new LinearCombination(1, 0, 0)),
    G_RGB			("G(RGB)", 					new LinearCombination(0, 1, 0)),
    B_RGB			("B(RGB)", 					new LinearCombination(0, 0, 1)),
    R2MINUS_GB 		("2R-(G+B)", 				new LinearCombination(2, -1, -1)), 
	G2MINUS_RB		("2G-(R+B)", 				new LinearCombination(-1, 2, -1)), 
	B2MINUS_RG		("2B-(R+G)", 				new LinearCombination(-1, -1, 2)), 
	GBMINUS_2R 		("(G+B)-2R", 				new LinearCombination(-2, 1, 1)),  
	RBMINUS_2G		("(R+B)-2G", 				new LinearCombination(1, -2, 1)),  
	RGMINUS_2B		("(R+G)-2B", 				new LinearCombination(1, 1, -2)), 
	RGB_DIFFS		("Sum(diffRGB)", 			new SumDiff()),
	RGB 			("(R+G+B)/3", 				new LinearCombination(1/3, 1/3, 1/3)),
	H_HSB 			("H(HSB)", 					new RGBtoHSB(0)), 
	S_HSB 			("S(HSB)", 					new RGBtoHSB(1)), 
	B_HSB			("B(HSB)", 					new RGBtoHSB(2)),  
	XDIFFN			("XDiffn", 					new XDiffn(3)), 
	YDIFFN			("YDiffn", 					new YDiffn(5)), 
	YDIFFN2			("YDiffn_1D", 				new YDiffn1D(4)), 
	XYDIFFN			( "XYDiffn", 				new XYDiffn(5)), 
	SUBTRACT_T0		("subtract t[start]", 		new SubtractReferenceImage()), 
	SUBTRACT_TM1	("subtract t[i-step]", 		new SubtractReferenceImage()), 
	SUBTRACT_REF	("subtract ref", 			new SubtractReferenceImage()),
	NORM_BRMINUSG	("F. Rebaudo", 				new LinearCombinationNormed(-1, 2, -1)),
	RGB_TO_H1H2H3	("H1H2H3", 					new H1H2H3()), 
	SUBTRACT_1RSTCOL("[t-t0]", 					new SubtractColumn(0)), 
	L1DIST_TO_1RSTCOL("L1[t-t0]", 				new L1DistanceToColumn(0)),
	COLORDISTANCE_L1_Y("color dist L1", 		new YDifferenceL(0, 0, 4, 0, false)), 
	COLORDISTANCE_L2_Y("color dist L2", 		new YDifferenceL(0, 0, 5, 0, true)),
	DERICHE			("edge detection", 			new Deriche(1., true)), 
	DERICHE_COLOR	("Deriche's edges", 		new Deriche(1., false)),
	MINUSHORIZAVG	("remove Hz traces", 		new RemoveHorizontalAverage()),
	THRESHOLD_SINGLE("threshold 1 value",		new ThresholdSingleValue()),
	THRESHOLD_COLORS("threshold colors array",	new ThresholdColors()),
	ZIGZAG			("remove spikes",			new None()),
	NONE			("none",					new None());

	private ImageTransformInterface klass;
    private String label;
	
    EnumImageTransformations(String label, ImageTransformInterface klass ) 
	{ 
		this.label = label; 
		this.klass = klass;
	}
    
	public String toString() 
	{ 
		return label; 
	}
	
	public ImageTransformInterface getFunction() 
	{ 
		return klass; 
	}
	
	public static EnumImageTransformations findByText(String abbr)
	{
	    for(EnumImageTransformations v : values())
	    { 
	    	if ( v.toString().equals(abbr)) 
	    		return v;  
	    }
	    return null;
	}

}