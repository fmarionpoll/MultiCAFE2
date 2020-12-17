package plugins.fmp.multicafe.workinprogress_gpu;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import icy.gui.component.PopupPanel;
import icy.gui.util.GuiUtil;
import plugins.fmp.multicafe.MultiCAFE;




public class MCSpots_ extends JPanel implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2230724185086264742L;
	private JTabbedPane 	tabsPane 		= new JTabbedPane();
	MCSpots_1 				buildRef 		= new MCSpots_1();


	
	void init (JPanel mainPanel, String string, MultiCAFE parent0) {
		
		PopupPanel capPopupPanel = new PopupPanel(string);
		JPanel capPanel = capPopupPanel.getMainPanel();
		capPanel.setLayout(new BorderLayout());
		capPopupPanel.collapse();
		mainPanel.add(GuiUtil.besidesPanel(capPopupPanel));

		GridLayout capLayout = new GridLayout(3, 1);
		
		buildRef.init(capLayout, parent0);
		buildRef.addPropertyChangeListener(this);
		tabsPane.addTab("Limits", null, buildRef, "Subtract first column");
		
		capPanel.add(GuiUtil.besidesPanel(tabsPane));
		tabsPane.setSelectedIndex(0);
		
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
	public void propertyChange(PropertyChangeEvent arg0) {

	}


}