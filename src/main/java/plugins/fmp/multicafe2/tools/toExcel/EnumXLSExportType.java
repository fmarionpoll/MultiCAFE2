package plugins.fmp.multicafe2.tools.toExcel;

public enum EnumXLSExportType 
{
	TOPRAW ("topraw", "volume (ul)"),
	TOPLEVEL ("toplevel", "volume (ul)"),
	BOTTOMLEVEL ("bottomlevel", "volume (ul)"), 
	DERIVEDVALUES ("derivative", "volume (ul)"), 
	
	TOPLEVEL_LR ("toplevel_L+R", "volume (ul)"), 
	TOPLEVELDELTA ("topdelta", "volume (ul)"),
	TOPLEVELDELTA_LR ("topdelta_L+R", "volume (ul)"),
	TOPLEVEL_RATIO ("toplevel_ratio", "volume (ul)"),
	
	SUMGULPS ("sumGulps", "volume (ul)"), 
	SUMGULPS_LR ("sumGulps_L+R", "volume (ul)"), 
	NBGULPS ("nbGulps", "volume (ul)"),
	AMPLITUDEGULPS ("amplitudeGulps", "volume (ul)"),
	TTOGULP("tToGulp", "minutes"),
	TTOGULP_LR("tToGulp_LR", "minutes"),
	
	XYIMAGE ("xy-image", "pixels"), 
	XYTOPCAGE ("xy-topcage", "pixels"), 
	XYTIPCAPS ("xy-tipcaps", "pixels"), 
	DISTANCE ("distance", "pixels"), 
	ISALIVE ("_alive", "yes/no"), 
	SLEEP ("sleep", "yes, no");
	
	private String label;
	private String unit;
	
	EnumXLSExportType (String label, String unit) 
	{ 
		this.label = label;
		this.unit = unit;
	}
	
	public String toString() 
	{ 
		return label;
	}
	
	public String toUnit() 
	{
		return unit;
	}
	
	public static EnumXLSExportType findByText(String abbr)
	{
	    for(EnumXLSExportType v : values()) 
	    { 
	    	if( v.toString().equals(abbr)) 
	    		return v;   
    	}
	    return null;
	}
}
