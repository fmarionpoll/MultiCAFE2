package plugins.fmp.multicafe;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import icy.gui.util.GuiUtil;
import icy.util.StringUtil;
import plugins.fmp.multicafeSequence.Capillary;
import plugins.fmp.multicafeSequence.Experiment;
import plugins.fmp.multicafeSequence.SequenceKymos;
import plugins.fmp.multicafeTools.DetectLimits_Options;
import plugins.fmp.multicafeTools.DetectLimits_series;
import plugins.fmp.multicafeTools.ImageTransformTools.TransformOp;


public class MCLevels_DetectLimits extends JPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	JSpinner			startSpinner			= new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
	JSpinner			endSpinner				= new JSpinner(new SpinnerNumberModel(3, 1, 100000, 1));
	JComboBox<TransformOp> transformForLevelsComboBox = new JComboBox<TransformOp> (new TransformOp[] {
			TransformOp.R_RGB, TransformOp.G_RGB, TransformOp.B_RGB, 
			TransformOp.R2MINUS_GB, TransformOp.G2MINUS_RB, TransformOp.B2MINUS_RG, TransformOp.RGB,
			TransformOp.GBMINUS_2R, TransformOp.RBMINUS_2G, TransformOp.RGMINUS_2B, 
			TransformOp.H_HSB, TransformOp.S_HSB, TransformOp.B_HSB	});

	private static final long serialVersionUID = -6329863521455897561L;
	private JComboBox<String> 	directionComboBox= new JComboBox<String> (new String[] {" threshold >", " threshold <" });
	private JCheckBox	allImagesCheckBox 		= new JCheckBox ("all images", true);
	private JSpinner 	thresholdSpinner 		= new JSpinner(new SpinnerNumberModel(35, 1, 255, 1));
	private JButton		displayTransform1Button	= new JButton("Display");
	private JSpinner	spanTopSpinner			= new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
	private String 		detectString 			= "Detect";
	private JButton 	detectButton 			= new JButton(detectString);
	private JCheckBox	partCheckBox 			= new JCheckBox ("detect from", false);
	private JCheckBox	ALLCheckBox 			= new JCheckBox("ALL series", false);
	
	private MultiCAFE 	parent0 				= null;
	private DetectLimits_series thread 			= null;

	
	
	// -----------------------------------------------------
		
	void init(GridLayout capLayout, MultiCAFE parent0) {
		setLayout(capLayout);
		this.parent0 = parent0;
		add( GuiUtil.besidesPanel(detectButton, allImagesCheckBox, new JLabel(" "), ALLCheckBox));
		
		((JLabel) directionComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		add( GuiUtil.besidesPanel(directionComboBox, thresholdSpinner, transformForLevelsComboBox, displayTransform1Button ));
		
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		((FlowLayout)panel1.getLayout()).setVgap(0);	
		panel1.add(partCheckBox);
		panel1.add(startSpinner);
		panel1.add(new JLabel("to"));
		panel1.add(endSpinner);
		add( panel1);
		
		defineActionListeners();
	}
	
	private void defineActionListeners() {
		transformForLevelsComboBox.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = parent0.expList.getExperiment(parent0.currentExperimentIndex);
				if (exp != null && exp.seqCamData != null) {
					kymosDisplayFiltered1(exp);
					firePropertyChange("KYMO_DISPLAY_FILTERED1", false, true);
				}
			}});
		
		detectButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				if (detectButton.getText() .equals(detectString)) {
					series_detectLimitsStart();
				}
				else {
					series_detectLimitsStop();
				}
			}});	
		
		displayTransform1Button.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = parent0.expList.getExperiment(parent0.currentExperimentIndex);
				kymosDisplayFiltered1(exp);
				firePropertyChange("KYMO_DISPLAY_FILTERED1", false, true);
			}});
		
		ALLCheckBox.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) {
				Color color = Color.BLACK;
				if (ALLCheckBox.isSelected()) 
					color = Color.RED;
				ALLCheckBox.setForeground(color);
				detectButton.setForeground(color);
		}});

	}
	
	// -------------------------------------------------
	
	int getDetectLevelThreshold() {
		return (int) thresholdSpinner.getValue();
	}

	void setDetectLevelThreshold (int threshold) {
		thresholdSpinner.setValue(threshold);
	}
	
	int getSpanDiffTop() {
		return (int) spanTopSpinner.getValue() ;
	}
		
	void kymosDisplayFiltered1(Experiment exp) {
		SequenceKymos seqKymos = exp.seqKymos;
		if (seqKymos == null)
			return;
		TransformOp transform = (TransformOp) transformForLevelsComboBox.getSelectedItem();
		List<Capillary> capList = exp.capillaries.capillariesArrayList;
		for (int t=0; t < exp.seqKymos.seq.getSizeT(); t++) {
			getInfosFromDialog(capList.get(t));		
		}
		int zChannelDestination = 1;
		exp.kymosBuildFiltered(0, zChannelDestination, transform, getSpanDiffTop());
		seqKymos.seq.getFirstViewer().getCanvas().setPositionZ(zChannelDestination);
	}
	
	void setInfosToDialog(Capillary cap) {
		DetectLimits_Options options = cap.limitsOptions;
		transformForLevelsComboBox.setSelectedItem(options.transformForLevels);
		int index =options.directionUp ? 0:1;
		directionComboBox.setSelectedIndex(index);
		setDetectLevelThreshold(options.detectLevelThreshold);
		thresholdSpinner.setValue(options.detectLevelThreshold);
		allImagesCheckBox.setSelected(options.detectAllImages);
	}
	
	void getInfosFromDialog(Capillary cap) {
		DetectLimits_Options options = cap.limitsOptions;
		options.transformForLevels = (TransformOp) transformForLevelsComboBox.getSelectedItem();
		options.directionUp = (directionComboBox.getSelectedIndex() == 0) ;
		options.detectLevelThreshold = getDetectLevelThreshold();
		options.detectAllImages = allImagesCheckBox.isSelected();
	}
	
	private boolean initBuildParameters(Experiment exp) {
		if (thread == null)
			return false;
		
		DetectLimits_Options options= thread.options;
		options.expList = parent0.expList; 
		options.expList.index0 = parent0.currentExperimentIndex;
		options.expList.index1 = options.expList.index0;
		if (ALLCheckBox.isSelected()) {
			options.expList.index0 = 0;
			options.expList.index1 = parent0.expList.experimentList.size()-1;
		}
		options.transformForLevels 	= (TransformOp) transformForLevelsComboBox.getSelectedItem();
		options.directionUp 		= (directionComboBox.getSelectedIndex() == 0);
		options.detectLevelThreshold= (int) getDetectLevelThreshold();
		options.detectAllImages 	= allImagesCheckBox.isSelected();
		int first = parent0.paneKymos.tabDisplay.kymographNamesComboBox.getSelectedIndex();
		if (first <0)
			first = 0;
		options.firstImage = first;
		options.analyzePartOnly		= partCheckBox.isSelected();
		options.startPixel			= (int) startSpinner.getValue();
		options.endPixel			= (int) endSpinner.getValue();
		options.spanDiffTop			= getSpanDiffTop();
		options.parent0Rect 		= parent0.mainFrame.getBoundsInternal();
		
		return true;
	}
	
	void series_detectLimitsStart() {
		thread = new DetectLimits_series();
		
		Experiment exp = parent0.expList.getExperiment(parent0.currentExperimentIndex);
		parent0.paneSequence.tabClose.closeExp(exp);
		parent0.paneSequence.transferExperimentNamesToExpList(parent0.expList, true);
		parent0.paneSequence.tabIntervals.getAnalyzeFrameFromDialog(exp);
		parent0.currentExperimentIndex = parent0.paneSequence.expListComboBox.getSelectedIndex();
		initBuildParameters(exp);
		
		thread.addPropertyChangeListener(this);
		thread.execute();
		detectButton.setText("STOP");
	}

	private void series_detectLimitsStop() {	
		if (thread != null && !thread.stopFlag) {
			thread.stopFlag = true;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		 if (StringUtil.equals("thread_ended", evt.getPropertyName())) {
			Experiment exp = parent0.expList.getExperiment(parent0.paneSequence.expListComboBox.getSelectedIndex());
			parent0.paneSequence.openExperiment(exp);
			detectButton.setText(detectString);
		 }
	}
}
