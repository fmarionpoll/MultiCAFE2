package plugins.fmp.multicafe2.dlg.capillaries;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import icy.gui.frame.IcyFrame;
import plugins.fmp.multicafe2.MultiCAFE2;
import plugins.fmp.multicafe2.dlg.JComponents.CapillariesWithTimeTableModel;
import plugins.fmp.multicafe2.experiment.CapillariesWithTime;


public class EditCapillariesTable extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID 		= 1L;
	
	IcyFrame 					dialogFrame 		= null;
    private JTable 				tableView 			= new JTable();
    private String				explanation 		= "Move to image, edit capillaries position and save";

    private JButton				addItem				= new JButton("Add interval");
    private JButton				deleteItem			= new JButton("Delete interval");
    private JButton				saveCapillaries   	= new JButton("Save");
    private JButton				showFrameButton		= new JButton("Show capillaries outer frame");
    private JButton				fitCapillariesToFrameButton	= new JButton("Fit capillaries to frame");
    
	private MultiCAFE2 			parent0 			= null; 
	private CapillariesWithTimeTableModel capillariesWithTimeTablemodel = null;
	private List <CapillariesWithTime> 	capillariesArrayCopy = null;
	
	
	public void initialize (MultiCAFE2 parent0, List <CapillariesWithTime> capCopy) 
	{
		this.parent0 = parent0;
		capillariesArrayCopy = capCopy;
		
		capillariesWithTimeTablemodel = new CapillariesWithTimeTableModel(parent0.expListCombo);
	    tableView.setModel(capillariesWithTimeTablemodel);
	    tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    tableView.setPreferredScrollableViewportSize(new Dimension(300, 200));
	    tableView.setFillsViewportHeight(true);
	    TableColumnModel columnModel = tableView.getColumnModel();
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment( JLabel.CENTER );
	    for (int i=0; i<capillariesWithTimeTablemodel.getColumnCount(); i++) {
	    	TableColumn col = columnModel.getColumn(i);
	    	col.setCellRenderer( centerRenderer );
	    }
        JScrollPane scrollPane = new JScrollPane(tableView);
        
		JPanel topPanel = new JPanel(new GridLayout(4, 1));
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT); 
		JPanel panel0 = new JPanel (flowLayout);
		panel0.add(new JLabel(explanation));
		topPanel.add(panel0);
		
		JPanel panel1 = new JPanel (flowLayout);
		panel1.add(addItem);
		panel1.add(deleteItem);
		topPanel.add(panel1);
        
        JPanel panel2 = new JPanel (flowLayout);
        panel2.add(showFrameButton);
        panel2.add(fitCapillariesToFrameButton);
        panel2.add(saveCapillaries);
        topPanel.add(panel2);
        
        JPanel panel3 = new JPanel (flowLayout);
        panel3.add(saveCapillaries);
        topPanel.add(panel3);
        
        JPanel tablePanel = new JPanel();
		tablePanel.add(scrollPane);
        
		dialogFrame = new IcyFrame ("Edit capillaries position with time", true, true);	
		dialogFrame.add(topPanel, BorderLayout.NORTH);
		dialogFrame.add(tablePanel, BorderLayout.CENTER);
		
		dialogFrame.pack();
		dialogFrame.addToDesktopPane();
		dialogFrame.requestFocus();
		dialogFrame.center();
		dialogFrame.setVisible(true);
		defineActionListeners();
		
	}
	
	private void defineActionListeners() 
	{
		
//		getNfliesButton.addActionListener(new ActionListener () 
//		{ 
//			@Override public void actionPerformed( final ActionEvent e ) 
//			{ 
//				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
//				if (exp != null && exp.cages.cagesList.size() > 0) 
//				{
//					exp.cages.transferNFliesFromCagesToCapillaries(exp.capillaries.capillariesList);
//					capillariesWithTimeTablemodel.fireTableDataChanged();
//				}
//			}});
		
		
	}
	
	void close() 
	{
		dialogFrame.close();
	}
	

	

}
