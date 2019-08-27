package plugins.fmp.multicafe;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.util.FontUtil;
import icy.gui.util.GuiUtil;
import plugins.fmp.multicafeSequence.Experiment;
import plugins.fmp.multicafeSequence.SequenceKymos;



public class MCKymosAnalyzeTab_File  extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3973928400949966679L;

	private JButton		loadMeasuresButton		= new JButton("Load");
	private JButton		saveMeasuresButton		= new JButton("Save");
	private MultiCAFE 	parent0 				= null;
	
	void init(GridLayout capLayout, MultiCAFE parent0) {
		setLayout(capLayout);
		this.parent0 = parent0;
	
		JLabel loadsaveText3 = new JLabel ("-> File (xml) ", SwingConstants.RIGHT); 
		loadsaveText3.setFont(FontUtil.setStyle(loadsaveText3.getFont(), Font.ITALIC));
		add(GuiUtil.besidesPanel(new JLabel (" "), loadsaveText3,  loadMeasuresButton, saveMeasuresButton));

		defineActionListeners();
	}
	
	private void defineActionListeners() {
		loadMeasuresButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = parent0.expList.getExperiment(parent0.currentIndex);
				if (loadKymosMeasures(exp)) {
					transferMeasuresToROIs(exp);
					firePropertyChange("MEASURES_OPEN", false, true);
				}
			}}); 
		
		saveMeasuresButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = parent0.expList.getExperiment(parent0.currentIndex);
				saveKymosMeasures(exp);
				firePropertyChange("MEASURES_SAVE", false, true);
			}});	
	}

	boolean loadKymosMeasures(Experiment exp) {
		String directory = exp.seqCamData.getDirectory();
		boolean flag = true;
		if (exp.seqKymos != null ) {
			exp.seqKymos.xmlLoadCapillaryTrack(directory);
		}
		return flag;
	}
	
	boolean transferMeasuresToROIs(Experiment exp) {
		SequenceKymos seqKymos = exp.seqKymos;
		boolean flag = true;
		if (seqKymos != null && seqKymos.seq != null) {
			seqKymos.seq.removeAllROI();
			seqKymos.transferMeasuresToKymosRois();
		}
		return flag;
	}
	
	boolean transferCapillariesToROIs(Experiment exp) {
		SequenceKymos seqKymos = exp.seqKymos;
		boolean flag = true;
		if (seqKymos != null && seqKymos.seq != null) {
			seqKymos.transferMeasuresToKymosRois();
		}
		return flag;
	}
	
	void saveKymosMeasures(Experiment exp) {
		SequenceKymos seqKymos = exp.seqKymos;
		if (seqKymos != null) {
			seqKymos.getAnalysisParametersFromCamData(exp.seqCamData);
			seqKymos.roisSaveEdits();
			String name = exp.seqCamData.getDirectory()+ File.separator + "capillarytrack.xml";
			seqKymos.xmlSaveCapillaryTrack(name);
		}
	}
}