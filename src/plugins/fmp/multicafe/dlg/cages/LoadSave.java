package plugins.fmp.multicafe.dlg.cages;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.util.FontUtil;
import icy.gui.util.GuiUtil;
import plugins.fmp.multicafe.MultiCAFE;
import plugins.fmp.multicafe.sequence.Experiment;



public class LoadSave extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5257698990389571518L;
	private JButton		openCagesButton			= new JButton("Load...");
	private JButton		saveCagesButton			= new JButton("Save...");
	public JCheckBox 	saveRoisCheckBox 		= new JCheckBox("save ROIs", false);
	private MultiCAFE parent0;
	
	void init(GridLayout capLayout, MultiCAFE parent0) {
		setLayout(capLayout);
		this.parent0 = parent0;

		JLabel 	loadsaveText = new JLabel ("-> File (xml) ", SwingConstants.RIGHT);
		loadsaveText.setFont(FontUtil.setStyle(loadsaveText.getFont(), Font.ITALIC));
		
		FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(loadsaveText);
		panel1.add(openCagesButton);
		panel1.add(saveCagesButton);
		panel1.validate();
		add( GuiUtil.besidesPanel( panel1));
		
//		JLabel emptyText1	= new JLabel (" ");
//		add(GuiUtil.besidesPanel( emptyText1, loadsaveText1, openCagesButton, saveCagesButton));
		add(GuiUtil.besidesPanel( new JLabel (" ")));
		add(GuiUtil.besidesPanel( saveRoisCheckBox));
		
		defineActionListeners();
	}
	
	private void defineActionListeners() {
		openCagesButton.addActionListener(new ActionListener () {
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = parent0.expList.getCurrentExperiment();
				if (exp != null)
					exp.xmlReadDrosoTrack(null);
				firePropertyChange("LOAD_DATA", false, true);
				parent0.paneCages.tabsPane.setSelectedIndex(3);
			}});
		
		saveCagesButton.addActionListener(new ActionListener () {
			@Override public void actionPerformed( final ActionEvent e ) { 
				Experiment exp = parent0.expList.getCurrentExperiment();
				saveCagesAndMeasures(exp);
				parent0.paneCages.tabsPane.setSelectedIndex(3);
			}});
	}

	boolean loadCages(String csFileName) {	
		boolean flag = false;
		Experiment exp = parent0.expList.getCurrentExperiment();
		if (exp == null)
			return false;
		flag = exp.xmlReadDrosoTrack(csFileName);
		return flag;
	}
	
	public void saveCagesAndMeasures(Experiment exp) {
		if (exp != null) {
			exp.storeAnalysisParametersToCages();
			exp.cages.getCagesFromROIs(exp.seqCamData);
			exp.xmlWriteDrosoTrackDefault(saveRoisCheckBox.isSelected());
		}
	}
	
}