package plugins.fmp.multicafe.dlg.experiment;


import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.gui.viewer.Viewer;
import icy.roi.ROI;

import plugins.fmp.multicafe.MultiCAFE;
import plugins.fmp.multicafe.experiment.Experiment;



public class Display  extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8137492850312895195L;
	public 	JCheckBox 	viewCapillariesCheckBox = new JCheckBox("capillaries", true);
	public JCheckBox 	viewCagesCheckbox 		= new JCheckBox("cages", true);
			JCheckBox 	viewFlyCheckbox 		= new JCheckBox("flies position", false);

	private MultiCAFE parent0 = null;

	
	void init(GridLayout capLayout, MultiCAFE parent0) 
	{	
		setLayout(capLayout);
		this.parent0 = parent0;
		
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		layout.setVgap(0);
		JPanel panel1 = new JPanel (layout);
		panel1.add(new JLabel(" ROIs: "));
		panel1.add(viewCapillariesCheckBox);
		panel1.add(viewCagesCheckbox);
		panel1.add(viewFlyCheckbox);
		add(panel1);

		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		viewCapillariesCheckBox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				displayROIsCategory(viewCapillariesCheckBox.isSelected(), "line");
			}});
		
		viewCagesCheckbox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
			displayROIsCategory(viewCagesCheckbox.isSelected(), "cage");
			}});
		
		viewFlyCheckbox.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				displayROIsCategory(viewFlyCheckbox.isSelected(), "det");
			}});
	}
	
	public void displayROIsCategory(boolean isVisible, String pattern) 
	{
		Experiment exp =(Experiment)  parent0.expList.getSelectedItem();
		if (exp == null)
			return;
		Viewer v = exp.seqCamData.seq.getFirstViewer();
		IcyCanvas canvas = v.getCanvas();
		List<Layer> layers = canvas.getLayers(false);
		if (layers == null)
			return;
		for (Layer layer: layers) 
		{
			ROI roi = layer.getAttachedROI();
			if (roi == null)
				continue;
			String cs = roi.getName();
			if (cs.contains(pattern))  
				layer.setVisible(isVisible);
		}
	}

}