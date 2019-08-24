package plugins.fmp.multicafe;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import icy.gui.component.PopupPanel;
import icy.gui.util.GuiUtil;
import plugins.fmp.multicafeSequence.Capillaries;
import plugins.fmp.multicafeSequence.Experiment;
import plugins.fmp.multicafeSequence.SequenceCamData;
import plugins.fmp.multicafeSequence.SequenceKymos;
import plugins.fmp.multicafeSequence.SequenceKymosUtils;



public class MCCapillariesPane extends JPanel implements PropertyChangeListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 853047648249832145L;
	
	JTabbedPane 				tabsPane 		= new JTabbedPane();
	MCCapillariesTab_Build 		buildarrayTab 	= new MCCapillariesTab_Build();
	MCCapillariesTab_File 		fileTab 		= new MCCapillariesTab_File();
	MCCapillariesTab_Adjust 	adjustTab 		= new MCCapillariesTab_Adjust();
	MCCapillariesTab_Infos		infosTab		= new MCCapillariesTab_Infos();

	Capillaries capold = new Capillaries();
	private MultiCAFE parent0 = null;

	
	void init (JPanel mainPanel, String string, MultiCAFE parent0) {
		
		this.parent0 = parent0;
		PopupPanel capPopupPanel = new PopupPanel(string);
		JPanel capPanel = capPopupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		capPopupPanel.expand();
		mainPanel.add(GuiUtil.besidesPanel(capPopupPanel));
		
		GridLayout capLayout = new GridLayout(3, 1);
		
		buildarrayTab.init(capLayout, parent0);
		buildarrayTab.addPropertyChangeListener(this);
		tabsPane.addTab("Create", null, buildarrayTab, "Create lines defining capillaries");

		adjustTab.init(capLayout, parent0);
		adjustTab.addPropertyChangeListener(parent0);
		tabsPane.addTab("Adjust", null, adjustTab, "Adjust ROIS position to the capillaries");

		infosTab.init(capLayout, parent0);
		infosTab.addPropertyChangeListener(this);
		tabsPane.addTab("Infos", null, infosTab, "Define pixel conversion unit of images and capillaries content");

		fileTab.init(capLayout, parent0);
		fileTab.addPropertyChangeListener(this);
		tabsPane.addTab("Load/Save", null, fileTab, "Load/Save xml file with capillaries descriptors");

		tabsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		capPanel.add(GuiUtil.besidesPanel(tabsPane));
		
		tabsPane.addChangeListener(this );
		
		capPopupPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				parent0.mainFrame.revalidate();
				parent0.mainFrame.pack();
				parent0.mainFrame.repaint();
			}
		});
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getPropertyName().equals("CAP_ROIS_OPEN")) {
			fileTab.loadCapillaryTrack();
		  	setCapillariesInfosToDialogs();
		  	tabsPane.setSelectedIndex(2);
		  	firePropertyChange("CAPILLARIES_OPEN", false, true);
		}			  
		else if (event.getPropertyName().equals("CAP_ROIS_SAVE")) {
			Experiment exp = parent0.expList.getExperiment(parent0.currentIndex);
			fileTab.saveCapillaryTrack(exp);
			tabsPane.setSelectedIndex(2);
		}
		else if (event.getPropertyName().equals("CAPILLARIES_NEW")) {
			infosTab.visibleCheckBox.setSelected(true);
			firePropertyChange("CAPILLARIES_NEW", false, true);
			tabsPane.setSelectedIndex(2);
		}

	}
	
	boolean loadCapillaryTrack() {
		SequenceKymos seqKymos = parent0.expList.getSeqKymos(parent0.currentIndex);boolean flag = fileTab.loadCapillaryTrack();
		if (flag) {
			SwingUtilities.invokeLater(new Runnable() { public void run() {
				infosTab.setCapillariesInfosToDialog(seqKymos.capillaries);
				buildarrayTab.setCapillariesInfosToDialog(seqKymos.capillaries);
				parent0.sequencePane.infosTab.setCapillariesInfosToDialog(seqKymos.capillaries);
			}});
		}
		return flag;
	}
	
	private void setCapillariesInfosToDialogs() {
		SequenceCamData seqCamData = parent0.expList.getSeqCamData(parent0.currentIndex);
		SequenceKymos seqKymos = parent0.expList.getSeqKymos(parent0.currentIndex);
		SequenceKymosUtils.transferCamDataROIStoKymo(seqCamData, seqKymos);
		
		infosTab.setCapillariesInfosToDialog(seqKymos.capillaries);
		buildarrayTab.setCapillariesInfosToDialog(seqKymos.capillaries);
		parent0.sequencePane.infosTab.setCapillariesInfosToDialog(seqKymos.capillaries);
	}
	
	boolean saveCapillaryTrack(Experiment exp) {
		getCapillariesInfos(exp.seqKymos.capillaries);
		return fileTab.saveCapillaryTrack(exp);
	}
	
	void getCapillariesInfos(Capillaries cap) {
		infosTab.getCapillariesInfosFromDialog(cap);
		buildarrayTab.getCapillariesInfosFromDialog(cap);
		parent0.sequencePane.infosTab.getCapillariesInfosFromDialog(cap);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		JTabbedPane tabbedPane = (JTabbedPane) arg0.getSource();
        int selectedIndex = tabbedPane.getSelectedIndex();
        adjustTab.roisDisplayrefBar(selectedIndex == 1);
        infosTab.visibleCheckBox.setSelected(selectedIndex == 2);
	}
	


}
