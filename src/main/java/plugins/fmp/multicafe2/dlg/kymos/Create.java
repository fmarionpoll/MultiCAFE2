package plugins.fmp.multicafe2.dlg.kymos;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import icy.util.StringUtil;
import plugins.fmp.multicafe2.MultiCAFE2;
import plugins.fmp.multicafe2.experiment.Experiment;
import plugins.fmp.multicafe2.series.BuildKymographs;
import plugins.fmp.multicafe2.series.BuildSeriesOptions;
import plugins.fmp.multicafe2.tools.EnumStatusComputation;



public class Create extends JPanel implements PropertyChangeListener 
{ 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1771360416354320887L;
	private String 			detectString 			= "Start";
			JButton 		startComputationButton 	= new JButton("Start");
			JSpinner		diskRadiusSpinner 		= new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
			JCheckBox 		doRegistrationCheckBox 	= new JCheckBox("registration", false);
			JCheckBox		allSeriesCheckBox 		= new JCheckBox("ALL series (current to last)", false);
			JLabel			startFrameLabel			= new JLabel ("starting at frame");
			JSpinner		startFrameSpinner 		= new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
			
	EnumStatusComputation 	sComputation 			= EnumStatusComputation.START_COMPUTATION; 
	private MultiCAFE2 		parent0					= null;
	private BuildKymographs threadBuildKymo 		= null;

	// -----------------------------------------------------
	
	void init(GridLayout capLayout, MultiCAFE2 parent0) 
	{
		setLayout(capLayout);	
		this.parent0 = parent0;
		
		FlowLayout layoutLeft = new FlowLayout(FlowLayout.LEFT);
		
		JPanel panel0 = new JPanel(layoutLeft);
		((FlowLayout)panel0.getLayout()).setVgap(1);
		panel0.add(startComputationButton);
		panel0.add(allSeriesCheckBox);
		panel0.add(new JLabel("area around ROIs", SwingConstants.RIGHT));
		panel0.add(diskRadiusSpinner);  
		add(panel0);
		
		JPanel panel2 = new JPanel(layoutLeft);
		panel2.add(doRegistrationCheckBox);
		panel2.add(startFrameLabel);
		panel2.add(startFrameSpinner);
		add(panel2);
		
		startFrameLabel.setVisible(false);
		startFrameSpinner.setVisible(false);
		
		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		startComputationButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				if (startComputationButton.getText() .equals(detectString))
					startComputation();
				else
					stopComputation();
		}});

		allSeriesCheckBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				Color color = Color.BLACK;
				if (allSeriesCheckBox.isSelected()) 
					color = Color.RED;
				allSeriesCheckBox.setForeground(color);
				startComputationButton.setForeground(color);
		}});
		
		doRegistrationCheckBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				boolean flag = doRegistrationCheckBox.isSelected();
				startFrameLabel.setVisible(flag);
				startFrameSpinner.setVisible(flag);
				if (flag)
					allSeriesCheckBox.setSelected(false);
		}});
	}
		
	private BuildSeriesOptions initBuildParameters() 
	{
		BuildSeriesOptions options  = new BuildSeriesOptions();
		options.expList = parent0.expListCombo; 
		options.expList.index0 = parent0.expListCombo.getSelectedIndex();
		if (allSeriesCheckBox.isSelected())
			options.expList.index1 = parent0.expListCombo.getItemCount()-1;
		else
			options.expList.index1 = options.expList.index0; 
		
		options.isFrameFixed 	= parent0.paneExcel.tabOptions.getIsFixedFrame();
		options.t_firstMs 		= parent0.paneExcel.tabOptions.getStartMs();
		options.t_lastMs 		= parent0.paneExcel.tabOptions.getEndMs();
		options.t_binMs			= parent0.paneExcel.tabOptions.getBinMs();
				
		options.diskRadius 		= (int) diskRadiusSpinner.getValue();
		options.doRegistration 	= doRegistrationCheckBox.isSelected();
		options.referenceFrame  = (int) startFrameSpinner.getValue();
		options.doCreateBinDir 	= true;
		options.parent0Rect 	= parent0.mainFrame.getBoundsInternal();
		options.binSubDirectory = Experiment.BIN+options.t_binMs/1000 ;
		return options;
	}
		
	private void startComputation() 
	{
		sComputation = EnumStatusComputation.STOP_COMPUTATION;
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp != null)
			parent0.paneCapillaries.tabFile.saveCapillaries_file(exp);
		
		threadBuildKymo = new BuildKymographs();	
		threadBuildKymo.options = initBuildParameters();
		
		threadBuildKymo.addPropertyChangeListener(this);
		threadBuildKymo.execute();
		startComputationButton.setText("STOP");
	}
	
	private void stopComputation() 
	{	
		if (threadBuildKymo != null && !threadBuildKymo.stopFlag) {
			threadBuildKymo.stopFlag = true;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{
		 if (StringUtil.equals("thread_ended", evt.getPropertyName())) {
			startComputationButton.setText(detectString);
		 }
	}
	

}
