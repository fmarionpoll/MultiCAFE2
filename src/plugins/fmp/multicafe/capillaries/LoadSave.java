package plugins.fmp.multicafe.capillaries;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.util.FontUtil;
import icy.gui.util.GuiUtil;
import plugins.fmp.multicafe.MultiCAFE;
import plugins.fmp.multicafeSequence.Experiment;
import plugins.fmp.multicafeSequence.SequenceKymos;
import plugins.fmp.multicafeSequence.SequenceKymosUtils;


public class LoadSave extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4019075448319252245L;
	
	private JButton		openButtonCapillaries	= new JButton("Load...");
	private JButton		saveButtonCapillaries	= new JButton("Save...");
	private MultiCAFE 	parent0 				= null;
	
	void init(GridLayout capLayout, MultiCAFE parent0) {
		setLayout(capLayout);
		
		JLabel loadsaveText = new JLabel ("-> Capillaries (xml) ", SwingConstants.RIGHT);
		loadsaveText.setFont(FontUtil.setStyle(loadsaveText.getFont(), Font.ITALIC));
		add(GuiUtil.besidesPanel( new JLabel (" "), loadsaveText, openButtonCapillaries, saveButtonCapillaries));
			
		this.parent0 = parent0;
		defineActionListeners();
	}
	
	private void defineActionListeners() {	
		openButtonCapillaries.addActionListener(new ActionListener () { @Override public void actionPerformed( final ActionEvent e ) { 
			Experiment exp = parent0.expList.getCurrentExperiment();
			if (exp != null) {
				loadCapillaries_File(exp);
				firePropertyChange("CAP_ROIS_OPEN", false, true);
			}
		}}); 
		
		saveButtonCapillaries.addActionListener(new ActionListener () { @Override public void actionPerformed( final ActionEvent e ) { 
			Experiment exp = parent0.expList.getCurrentExperiment();
			if (exp != null) {
				saveCapillaries(exp);
				firePropertyChange("CAP_ROIS_SAVE", false, true);
			}
		}});	
	}
	
	boolean loadCapillaries_File(Experiment exp) {	
		SequenceKymos seqKymos = exp.seqKymos;
		if (seqKymos == null) {
			exp.seqKymos = new SequenceKymos();
			seqKymos = exp.seqKymos;
		}
		boolean flag = false;
		flag = exp.xmlLoadMCcapillaries_Only();
		if (flag &= exp.xmlLoadMCCapillaries_Measures()) {
			SequenceKymosUtils.transferKymoCapillariesToCamData (exp);
			return true;
		}
		return flag;
	}
	
	public boolean saveCapillaries(Experiment exp) {
		parent0.paneCapillaries.getCapillariesInfos(exp);  // get data into desc
		parent0.paneSequence.getExperimentInfosFromDialog(exp);
		exp.capillaries.transferDescriptionToCapillaries();
	
		exp.xmlSaveExperiment ();
		exp.updateCapillariesFromCamData();
		return exp.xmlSaveMCcapillaries();
	}

}