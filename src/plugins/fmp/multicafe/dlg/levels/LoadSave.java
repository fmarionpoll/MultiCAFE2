package plugins.fmp.multicafe.dlg.levels;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import icy.gui.util.FontUtil;
import icy.gui.util.GuiUtil;
import plugins.fmp.multicafe.MultiCAFE;
import plugins.fmp.multicafe.sequence.Experiment;




public class LoadSave  extends JPanel {
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
	
		JLabel loadsaveText = new JLabel ("-> File (xml) ", SwingConstants.RIGHT); 
		loadsaveText.setFont(FontUtil.setStyle(loadsaveText.getFont(), Font.ITALIC));
		
		FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(loadsaveText);
		panel1.add(loadMeasuresButton);
		panel1.add(saveMeasuresButton);
		panel1.validate();
		add( GuiUtil.besidesPanel( panel1));
		
		defineActionListeners();
	}
	
	private void defineActionListeners() {
		loadMeasuresButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = parent0.expList.getCurrentExperiment();
				if (exp != null && loadCapillaries_Measures(exp)) {
					exp.transferCapillariesToROIs();
					firePropertyChange("MEASURES_OPEN", false, true);
				}
			}}); 
		
		saveMeasuresButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = parent0.expList.getCurrentExperiment();
				if (exp != null) {
					exp.saveExperimentMeasures(exp.getResultsDirectory());
					firePropertyChange("MEASURES_SAVE", false, true);
				}
			}});	
	}

	public boolean loadCapillaries_Measures(Experiment exp) {
		boolean flag = true;
		if (exp.seqKymos != null ) {
			boolean readOK = exp.xmlLoadMCCapillaries_Measures();
			if (readOK) {
				SwingUtilities.invokeLater(new Runnable() { public void run() {
					parent0.paneSequence.tabInfosSeq.setExperimentsInfosToDialog(exp);
					parent0.paneSequence.tabIntervals.setAnalyzeFrameToDialog(exp);
					parent0.paneKymos.tabCreate.setBuildKymosParametersToDialog(exp);
				}});
			}
		}
		return flag;
	}
	
	
}
