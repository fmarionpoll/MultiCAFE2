package plugins.fmp.multicafe;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.gui.util.GuiUtil;
import icy.gui.viewer.Viewer;
import icy.roi.ROI;
import plugins.fmp.multicafeSequence.Capillaries;
import plugins.fmp.multicafeSequence.SequenceCamData;



public class MCCapillariesTab_Infos extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4950182090521600937L;

	JCheckBox					visibleCheckBox				= new JCheckBox("ROIs visible", true);
	private JSpinner 			capillaryVolumeTextField	= new JSpinner(new SpinnerNumberModel(5., 0., 100., 1.));
	private JSpinner 			capillaryPixelsTextField	= new JSpinner(new SpinnerNumberModel(5, 0, 1000, 1));
	private JComboBox<String> 	stimulusRJCombo				= new JComboBox<String>();
	private JComboBox<String> 	concentrationRJCombo 		= new JComboBox<String>();
	private JComboBox<String> 	stimulusLJCombo				= new JComboBox<String>();
	private JComboBox<String> 	concentrationLJCombo 		= new JComboBox<String>();
	
	private MultiCAFE parent0;
	
	void init(GridLayout capLayout, MultiCAFE parent0) {
		setLayout(capLayout);
		
		add( GuiUtil.besidesPanel(
				visibleCheckBox,
				new JLabel("volume (�l) ", SwingConstants.RIGHT), 
				capillaryVolumeTextField,  
				new JLabel("length (pixels) ", SwingConstants.RIGHT), 
				capillaryPixelsTextField));
		
		add( GuiUtil.besidesPanel(
				createComboPanel("stim(L) ", stimulusLJCombo),  
				createComboPanel("  conc(L) ", concentrationLJCombo)));
		
		add( GuiUtil.besidesPanel(
				createComboPanel("stim(R) ", stimulusRJCombo),  
				createComboPanel("  conc(R) ", concentrationRJCombo)));
		
		stimulusRJCombo.setEditable(true);
		concentrationRJCombo.setEditable(true);
		stimulusLJCombo.setEditable(true);
		concentrationLJCombo.setEditable(true);	
		
		this.parent0 = parent0;
		defineActionListeners();
	}
			
	private void defineActionListeners() {
		visibleCheckBox.addActionListener(new ActionListener () { @Override public void actionPerformed( final ActionEvent e ) { 
			roisDisplayLine(visibleCheckBox.isSelected());
		} } );
	}
			
	private JPanel createComboPanel(String text, JComboBox<String> combo) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(text, SwingConstants.RIGHT), BorderLayout.WEST); 
		panel.add(combo, BorderLayout.CENTER);
		return panel;
	}

	
	private void roisDisplayLine(boolean isVisible) {
		SequenceCamData seqCamData = parent0.expList.getSeqCamData(parent0.currentIndex);
		Viewer v = seqCamData.seq.getFirstViewer();
		IcyCanvas canvas = v.getCanvas();
		List<Layer> layers = canvas.getLayers(false);
		if (layers == null)
			return;
		for (Layer layer: layers) {
			ROI roi = layer.getAttachedROI();
			if (roi == null)
				continue;
			String cs = roi.getName();
			if (cs.contains("line"))  
				layer.setVisible(isVisible);
		}
	}
		
	// set/ get
	
	void setCapillariesInfosToDialog(Capillaries cap) {
		capillaryVolumeTextField.setValue( cap.desc.volume);
		capillaryPixelsTextField.setValue( cap.desc.pixels);
		addItem(stimulusRJCombo, cap.desc.stimulusR);
		addItem(concentrationRJCombo, cap.desc.concentrationR);
		addItem(stimulusLJCombo, cap.desc.stimulusL);
		addItem(concentrationLJCombo, cap.desc.concentrationL);
	}

	
	private double getCapillaryVolume() {
		return (double) capillaryVolumeTextField.getValue();
	}
	
	private int getCapillaryPixelLength() {
		return (int) capillaryPixelsTextField.getValue(); 
	}
	
	void getCapillariesInfosFromDialog(Capillaries cap) {
		cap.desc.volume = getCapillaryVolume();
		cap.desc.pixels = getCapillaryPixelLength();
		cap.desc.stimulusR = (String) stimulusRJCombo.getSelectedItem();
		cap.desc.concentrationR = (String) concentrationRJCombo.getSelectedItem();
		cap.desc.stimulusL = (String) stimulusLJCombo.getSelectedItem();
		cap.desc.concentrationL = (String) concentrationLJCombo.getSelectedItem();
	}
	
	private void addItem(JComboBox<String> combo, String text) {
		if (text == null)
			return;
		combo.setSelectedItem(text);
		if (combo.getSelectedIndex() < 0) {
			boolean found = false;
			for (int i=0; i < combo.getItemCount(); i++) {
				int comparison = text.compareTo(combo.getItemAt(i));
				if (comparison > 0)
					continue;
				if (comparison < 0) {
					found = true;
					combo.insertItemAt(text, i);
					break;
				}
			}
			if (!found)
				combo.addItem(text);
			combo.setSelectedItem(text);
		}
	}
	
	void updateCombos() {
		addItem(stimulusRJCombo, (String) stimulusRJCombo.getSelectedItem());
		addItem(concentrationRJCombo, (String) concentrationRJCombo.getSelectedItem());
		
		addItem(stimulusLJCombo, (String) stimulusLJCombo.getSelectedItem());
		addItem(concentrationLJCombo, (String) concentrationLJCombo.getSelectedItem());
	}
						
}
