package plugins.fmp.multicafe.dlg.levels;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import icy.util.StringUtil;
import plugins.fmp.multicafe.MultiCAFE;
import plugins.fmp.multicafe.sequence.Capillary;
import plugins.fmp.multicafe.sequence.Experiment;
import plugins.fmp.multicafe.sequence.SequenceKymos;
import plugins.fmp.multicafe.series.AdjustMeasuresDimensions_series;
import plugins.fmp.multicafe.series.BuildSeries_Options;
import plugins.fmp.multicafe.series.CurvesClipSameLengthWithinCage_series;
import plugins.fmp.multicafe.series.CurvesRestoreLength_series;


public class Adjust extends JPanel  implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2580935598417087197L;
	private MultiCAFE 			parent0;
	private JCheckBox			allSeriesCheckBox = new JCheckBox("ALL series", false);
	
	private String				adjustString  	= new String("Adjust dimensions");
	private String				clipString 		= new String("Clip curves per cage");
	private String				restoreString	= new String("Restore curves");
	
	private JButton 			adjustButton 	= new JButton(adjustString);
	private JButton 			restoreButton 	= new JButton(restoreString);
	private JButton 			clipButton 		= new JButton(clipString);
	private String				stopString		= new String("STOP ");
	
	private AdjustMeasuresDimensions_series threadAdjust = null;
	private CurvesRestoreLength_series threadRestore = null;
	private CurvesClipSameLengthWithinCage_series threadClip = null;
	
	
	
	void init(GridLayout capLayout, MultiCAFE parent0) {
		setLayout(capLayout);	
		this.parent0 = parent0;
		
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		layout.setVgap(0);
		
		JPanel panel0 = new JPanel(layout);
		panel0.add(adjustButton);
		add(panel0);

		JPanel panel1 = new JPanel(layout);
		panel1.add(restoreButton);
		panel1.add(clipButton);
		add(panel1);
		
		JPanel panel2 = new JPanel(layout);
		panel2.add(allSeriesCheckBox);
		add(panel2);
		
		defineListeners();
	}
	
	private void defineListeners() {
		adjustButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
				if (adjustButton.getText() .equals(adjustString))
					series_adjustDimensionsStart();
				else 
					series_adjustDimensionsStop();
			}});
		
		restoreButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
				if (restoreButton.getText() .equals(restoreString))
					series_restoreStart();
				else 
					series_restoreStop();
			}});
		
		clipButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
				if (restoreButton.getText() .equals(restoreString))
					series_clipStart();
				else 
					series_clipStop();
			}});
			
		allSeriesCheckBox.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
				Color color = Color.BLACK;
				if (allSeriesCheckBox.isSelected()) 
					color = Color.RED;
				allSeriesCheckBox.setForeground(color);
				adjustButton.setForeground(color);
				clipButton.setForeground(color);
				restoreButton.setForeground(color);
		}});
	}

	void restoreCroppedPoints(Experiment exp) {
		SequenceKymos seqKymos = exp.seqKymos;
		int t = seqKymos.currentFrame;
		Capillary cap = exp.capillaries.capillariesArrayList.get(t);
		cap.restoreCroppedMeasures();
		
		seqKymos.updateROIFromCapillaryMeasure(cap, cap.ptsTop);
		seqKymos.updateROIFromCapillaryMeasure(cap, cap.ptsBottom);
		seqKymos.updateROIFromCapillaryMeasure(cap, cap.ptsDerivative);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		 if (StringUtil.equals("thread_ended", evt.getPropertyName())) {
			Experiment exp = parent0.expList.getExperiment(parent0.paneSequence.expListComboBox.getSelectedIndex());
			parent0.paneSequence.openExperiment(exp);
			
			if (adjustButton.getText() .contains (stopString))
				adjustButton.setText(adjustString);
			else if (restoreButton.getText().contains(stopString))
				restoreButton.setText(restoreString);
			else if (clipButton.getText() .contains(stopString))
				clipButton.setText(clipString);
		 }	 
	}
	
	private void series_adjustDimensionsStop() {	
		if (threadAdjust != null && !threadAdjust.stopFlag) {
			threadAdjust.stopFlag = true;
		}
	}
	
	private void series_restoreStop() {
		if (threadRestore != null && !threadRestore.stopFlag) {
			threadRestore.stopFlag = true;
		}
	}
	
	private void series_clipStop() {
		if (threadClip != null && !threadClip.stopFlag) {
			threadClip.stopFlag = true;
		}
	}
	
	private boolean initBuildParameters(BuildSeries_Options options) {
		int index  = parent0.paneSequence.expListComboBox.getSelectedIndex();
		Experiment exp = parent0.expList.getExperiment(index);
		if (exp == null)
			return false;
		
		parent0.paneSequence.transferExperimentNamesToExpList(parent0.expList, true);
		
		parent0.expList.currentExperimentIndex = index;
		parent0.paneSequence.tabClose.closeExp(exp);
		options.expList = parent0.expList; 
		options.expList.index0 = parent0.expList.currentExperimentIndex;
		if (allSeriesCheckBox.isSelected())
			options.expList.index1 = parent0.expList.getSize()-1;
		else
			options.expList.index1 = options.expList.index0; 
		
		options.isFrameFixed 	= parent0.paneSequence.tabAnalyze.getIsFixedFrame();
		options.startMs 		= parent0.paneSequence.tabAnalyze.getStartMs();
		options.endMs 			= parent0.paneSequence.tabAnalyze.getEndMs();
		options.binMs			= parent0.paneSequence.tabAnalyze.getBinMs();
				
		options.parent0Rect 	= parent0.mainFrame.getBoundsInternal();
		options.resultsSubPath = (String) parent0.paneKymos.tabDisplay.availableResultsCombo.getSelectedItem() ;
		return true;
	}
	
	private void series_adjustDimensionsStart() {
		threadAdjust = new AdjustMeasuresDimensions_series();
		BuildSeries_Options options= threadAdjust.options;
		if (initBuildParameters (options)) {
			threadAdjust.addPropertyChangeListener(this);
			threadAdjust.execute();
			adjustButton.setText(stopString + adjustString);
		}
	}
	
	private void series_restoreStart() {
		threadRestore = new CurvesRestoreLength_series();
		BuildSeries_Options options= threadRestore.options;
		if (initBuildParameters (options)) {
			threadRestore.addPropertyChangeListener(this);
			threadRestore.execute();
			restoreButton.setText(stopString + restoreString);
		}
	}
	
	private void series_clipStart() {
		threadClip = new CurvesClipSameLengthWithinCage_series();
		BuildSeries_Options options= threadClip.options;
		if (initBuildParameters (options)) {
			threadClip.addPropertyChangeListener(this);
			threadClip.execute();
			clipButton.setText(stopString + clipString);
		}
	}
	
}
