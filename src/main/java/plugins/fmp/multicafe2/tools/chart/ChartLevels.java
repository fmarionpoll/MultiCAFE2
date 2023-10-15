package plugins.fmp.multicafe2.tools.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;

import plugins.fmp.multicafe2.MultiCAFE2;
import plugins.fmp.multicafe2.experiment.Experiment;
import plugins.fmp.multicafe2.tools.toExcel.EnumXLSExportType;
import plugins.fmp.multicafe2.tools.toExcel.XLSExport;
import plugins.fmp.multicafe2.tools.toExcel.XLSExportOptions;
import plugins.fmp.multicafe2.tools.toExcel.XLSResults;
import plugins.fmp.multicafe2.tools.toExcel.XLSResultsArray;

public class ChartLevels extends IcyFrame
{
	public JPanel 	mainChartPanel 	= null;
	public IcyFrame mainChartFrame 	= null;
	private MultiCAFE2 	parent0 	= null;
	
	private Point pt = new Point (0,0);
	private boolean flagMaxMinSet = false;
	private double globalYMax = 0;
	private double globalYMin = 0;
	private double globalXMax = 0;

	private double ymax = 0;
	private double ymin = 0;
	private double xmax = 0;
	private List<JFreeChart> xyChartList  = new ArrayList <JFreeChart>();
	private String title;

	//----------------------------------------
	
	public void createChartPanel(MultiCAFE2 parent, String cstitle) 
	{
		title = cstitle;
		parent0 = parent;
		
		mainChartPanel = new JPanel(); 
		mainChartPanel.setLayout( new BoxLayout( mainChartPanel, BoxLayout.LINE_AXIS ) );
		
		mainChartFrame = GuiUtil.generateTitleFrame(title, new JPanel(), new Dimension(300, 70), true, true, true, true);	    
		mainChartFrame.add(mainChartPanel);
	}

	public void setLocationRelativeToRectangle(Rectangle rectv, Point deltapt) 
	{
		pt = new Point(rectv.x + deltapt.x, rectv.y + deltapt.y);
	}
	
	public void displayData(Experiment exp, EnumXLSExportType option, boolean subtractEvaporation) 
	{
		xyChartList.clear();
		
		Dimension dim = this.getSize();
		System.out.println(dim);
		
		ymax = 0;
		ymin = 0;
		flagMaxMinSet = false;
		List<XYSeriesCollection> xyDataSetList = getDataArrays(exp, option, subtractEvaporation);;
		
		int icage = 0;
		
		// make a common vertical axis for all the sub-plots  
        final NumberAxis valueAxis = new NumberAxis("liquid level (µl)");
        valueAxis.setAutoRangeIncludesZero(false);  
        valueAxis.setInverted(true);
        final CombinedRangeXYPlot parent = new CombinedRangeXYPlot(valueAxis);
        
        Paint[] color = ChartColor.createDefaultPaintArray();

		for (XYSeriesCollection xySeriesCollection : xyDataSetList) 
		{
			NumberAxis domainAxis = new NumberAxis("Cage " + icage);
			XYLineAndShapeRenderer subPlotRenderer = new XYLineAndShapeRenderer(true, false);
			final XYPlot subplot = new XYPlot(xySeriesCollection, domainAxis, null, subPlotRenderer);
			int icolor = 0;
			for (int i=0; i< xySeriesCollection.getSeriesCount(); i++, icolor++ )
			{
				if (icolor > color.length)
					icolor = 0;
				subPlotRenderer.setSeriesPaint(i, color[icolor]);
			}
			
			subplot.setBackgroundPaint(Color.LIGHT_GRAY);
			subplot.setDomainGridlinePaint(Color.WHITE);
			subplot.setRangeGridlinePaint(Color.WHITE);
			parent.add(subplot);
						
			icage++;
		}
		
        JFreeChart chart = new JFreeChart("Capillary levels", null, parent, true);

        final ChartPanel panel = new ChartPanel(chart, true, true, true, false, true);
        panel.addChartMouseListener(new ChartMouseListener() {
		    public void chartMouseClicked(ChartMouseEvent e) {selectKymoImage(getSelectedCurve(e)); }
		    public void chartMouseMoved(ChartMouseEvent e) {}
		});
    	
		mainChartPanel.add(panel);
		mainChartFrame.pack();
		mainChartFrame.setLocation(pt);
		mainChartFrame.addToDesktopPane ();
		mainChartFrame.setVisible(true);
	}
	
	private int getSelectedCurve(ChartMouseEvent e) 
	{
		final MouseEvent trigger = e.getTrigger();
        if (trigger.getButton() != MouseEvent.BUTTON1)
        	return -1;
        		
		JFreeChart chart = e.getChart();
//        int isel = Integer.valueOf(chart.getID());
		ChartEntity chartEntity = e.getEntity();
		// TODO: use handleclick? jfreechart CombinedRangeXYPlot handleClick
		int isel= -1;
//		if (chartEntity != null && chartEntity instanceof XYItemEntity) {
//            XYItemEntity ent = (XYItemEntity) chartEntity;
//            isel += ent.getSeriesIndex();
//		}
		
		
		System.out.println( " entity" + chartEntity);
		return isel;
	}

	private void selectKymoImage(int isel)
	{
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
        Viewer v = exp.seqKymos.seq.getFirstViewer();
        v.setPositionT(isel);
	}

	private List<XYSeriesCollection> getDataArrays(Experiment exp, EnumXLSExportType exportType, boolean subtractEvaporation) 
	{
		XLSResultsArray resultsArray1 = getDataAsResultsArray(exp, exportType, subtractEvaporation);
		XLSResultsArray resultsArray2 = null;
		if (exportType == EnumXLSExportType.TOPLEVEL) 
			resultsArray2 = getDataAsResultsArray(exp, EnumXLSExportType.BOTTOMLEVEL, subtractEvaporation);
		
		XYSeriesCollection xyDataset = null;
		int oldcage = -1;
		
		List<XYSeriesCollection> xyList = new ArrayList<XYSeriesCollection>();
		for (int iRow = 0; iRow < resultsArray1.size(); iRow++ ) 
		{
			XLSResults rowXLSResults1 = resultsArray1.getRow(iRow);
			if (oldcage != rowXLSResults1.cageID ) 
			{
				xyDataset = new XYSeriesCollection();
				oldcage = rowXLSResults1.cageID; 
				xyList.add(xyDataset);
			} 	
			
			XYSeries seriesXY = getXYSeries(rowXLSResults1, rowXLSResults1.name.substring(4));
			if (resultsArray2 != null) 
				appendDataToXYSeries(seriesXY, resultsArray2.getRow(iRow));
			
			xyDataset.addSeries(seriesXY );
			updateGlobalMaxMin();
		}
		return xyList;
	}
	
	private XLSResultsArray getDataAsResultsArray(Experiment exp, EnumXLSExportType exportType, boolean subtractEvaporation)
	{
		XLSExportOptions options = new XLSExportOptions();
		options.buildExcelStepMs = 60000;
		options.t0 = true;
		options.subtractEvaporation = subtractEvaporation;
		
		XLSExport xlsExport = new XLSExport();
		return xlsExport.getCapDataFromOneExperiment(exp, exportType, options);
	}

	private void updateGlobalMaxMin() 
	{
		if (!flagMaxMinSet) 
		{
			globalYMax = ymax;
			globalYMin = ymin;
			globalXMax = xmax;
			flagMaxMinSet = true;
		}
		else 
		{
			if (globalYMax < ymax) globalYMax = ymax;
			if (globalYMin >= ymin) globalYMin = ymin;
			if (globalXMax < xmax) globalXMax = xmax;
		}
	}
	
	private XYSeries getXYSeries(XLSResults results, String name) 
	{
		XYSeries seriesXY = new XYSeries(name, false);
		if (results.valuesOut != null && results.valuesOut.length > 0) 
		{
			xmax = results.valuesOut.length;
			ymax = results.valuesOut[0];
			ymin = ymax;
			addPointsAndUpdateExtrema(seriesXY, results, 0);	
		}
		return seriesXY;
	}
	
	private void appendDataToXYSeries(XYSeries seriesXY, XLSResults results ) 
	{	
		if (results.valuesOut != null && results.valuesOut.length > 0) 
		{
			seriesXY.add(Double.NaN, Double.NaN);
			addPointsAndUpdateExtrema(seriesXY, results, 0);	
		}
	}
	
	private void addPointsAndUpdateExtrema(XYSeries seriesXY, XLSResults results, int startFrame) 
	{
		int x = 0;
		int npoints = results.valuesOut.length;
		for (int j = 0; j < npoints; j++) 
		{
			double y = results.valuesOut[j];
			seriesXY.add( x+startFrame , y );
			if (ymax < y) ymax = y;
			if (ymin > y) ymin = y;
			x++;
		}
	}

	
}
