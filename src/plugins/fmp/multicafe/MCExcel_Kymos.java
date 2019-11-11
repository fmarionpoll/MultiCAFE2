package plugins.fmp.multicafe;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import icy.gui.util.GuiUtil;


public class MCExcel_Kymos extends JPanel  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1290058998782225526L;

	JButton 	exportToXLSButton 	= new JButton("save XLS");
	JCheckBox 	topLevelCheckBox 	= new JCheckBox("top", true);
	JCheckBox 	topLevelDeltaCheckBox 	= new JCheckBox("delta top", true);


	
	JCheckBox 	bottomLevelCheckBox = new JCheckBox("bottom", false);
	JCheckBox 	consumptionCheckBox = new JCheckBox("gulps", false);
	JCheckBox 	sumCheckBox 		= new JCheckBox("L+R", true);
	JCheckBox 	derivativeCheckBox  = new JCheckBox("derivative", false);
	JCheckBox	t0CheckBox			= new JCheckBox("t-t0", true);
	JCheckBox	onlyaliveCheckBox   = new JCheckBox("dead=empty");	
	
	void init(GridLayout capLayout) {	
		setLayout(capLayout);
		add(GuiUtil.besidesPanel( topLevelCheckBox, topLevelDeltaCheckBox, consumptionCheckBox, bottomLevelCheckBox));
		add(GuiUtil.besidesPanel( t0CheckBox, sumCheckBox, new JLabel(" "), new JLabel(" "))); 
		add(GuiUtil.besidesPanel( onlyaliveCheckBox, new JLabel(" "), new JLabel(" "), exportToXLSButton)); 
		defineActionListeners();
	}
	
	private void defineActionListeners() {
		exportToXLSButton.addActionListener (new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				firePropertyChange("EXPORT_KYMOSDATA", false, true);
			}});
	}

}
