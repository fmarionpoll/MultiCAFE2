package plugins.fmp.multicafe;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import icy.gui.util.GuiUtil;
import plugins.fmp.multicafeTools.EnumArrayListType;
import plugins.fmp.multicafeTools.XYMultiChart;

public class MCKymosTab_Graphs extends JPanel implements ActionListener  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7079184380174992501L;
	private XYMultiChart topandbottomChart 		= null;
	private XYMultiChart deltaChart 			= null;
	private XYMultiChart derivativeChart 		= null;
	private XYMultiChart sumgulpsChart 			= null;
	private MultiCAFE 	parent0 				= null;
	
	private JCheckBox 	limitsCheckbox 			= new JCheckBox("top/bottom", true);
	private JCheckBox 	derivativeCheckbox 		= new JCheckBox("derivative", false);
	private JCheckBox 	consumptionCheckbox 	= new JCheckBox("consumption", false);
	private JCheckBox 	deltaCheckbox 			= new JCheckBox("delta (v(t) - v(t-1))", false);
	
	private JButton displayResultsButton 		= new JButton("Display results");
	
	
	void init(GridLayout capLayout, MultiCAFE parent0) {	
		setLayout(capLayout);
		this.parent0 = parent0;
		add(GuiUtil.besidesPanel(limitsCheckbox, derivativeCheckbox, consumptionCheckbox, deltaCheckbox));
		add(GuiUtil.besidesPanel(displayResultsButton, new JLabel(" "))); 
		defineActionListeners();
	}
	
	private void defineActionListeners() {
		displayResultsButton.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if ( o == displayResultsButton)  {
			displayResultsButton.setEnabled(false);
			parent0.roisSaveEdits();
			xyDisplayGraphs();
			displayResultsButton.setEnabled(true);
		}
	}
	
	void xyDisplayGraphs() {

		int kmax = parent0.vSequence.capillaries.grouping;
		final Rectangle rectv = parent0.vSequence.getFirstViewer().getBounds();
		Point ptRelative = new Point(0,rectv.height);
		final int deltay = 230;

		if (limitsCheckbox.isSelected()) {
			topandbottomChart = xyDisplayGraphsItem("top + bottom levels", 
					EnumArrayListType.topAndBottom, 
					topandbottomChart, rectv, ptRelative, kmax);
			ptRelative.y += deltay;
		}
		if (deltaCheckbox.isSelected()) {
			deltaChart = xyDisplayGraphsItem("top delta t -(t-1)", 
					EnumArrayListType.topLevelDelta, 
					deltaChart, rectv, ptRelative, kmax);
			ptRelative.y += deltay;
		}
		if (derivativeCheckbox.isSelected()) {
			derivativeChart = xyDisplayGraphsItem("Derivative", 
					EnumArrayListType.derivedValues, 
					derivativeChart, rectv, ptRelative, kmax);
			ptRelative.y += deltay; 
		}
		if (consumptionCheckbox.isSelected()) {
			sumgulpsChart = xyDisplayGraphsItem("Cumulated gulps", 
					EnumArrayListType.cumSum, 
					sumgulpsChart, rectv, ptRelative, kmax);
			ptRelative.y += deltay; 
		}
	}

	private XYMultiChart xyDisplayGraphsItem(String title, EnumArrayListType option, XYMultiChart iChart, Rectangle rectv, Point ptRelative, int kmax) {
		
		if (iChart != null && iChart.mainChartPanel.isValid()) {
			iChart.fetchNewData(parent0.kymographArrayList, option, kmax, (int) parent0.vSequence.analysisStart);

		}
		else {
			iChart = new XYMultiChart();
			iChart.createPanel(title);
			iChart.setLocationRelativeToRectangle(rectv, ptRelative);
			iChart.displayData(parent0.kymographArrayList, option, kmax, (int) parent0.vSequence.analysisStart);
		}
		iChart.mainChartFrame.toFront();
		return iChart;
	}
	
	void closeAll() {
		if (topandbottomChart != null) 
			topandbottomChart.mainChartFrame.dispose();
		if (derivativeChart != null) 
			derivativeChart.mainChartFrame.close();
		if (sumgulpsChart != null) 
			sumgulpsChart.mainChartFrame.close();
		if (deltaChart != null) 
			deltaChart.mainChartFrame.close();

		topandbottomChart  = null;
		derivativeChart = null;
		sumgulpsChart  = null;
		deltaChart = null;
	}
}

