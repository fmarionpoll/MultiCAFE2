package plugins.fmp.multicafeTools;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.image.IcyBufferedImage;
import icy.type.point.Point5D;
import plugins.fmp.multicafeSequence.SequenceCamData;
import icy.painter.Overlay;
import icy.painter.OverlayListener;
import icy.sequence.Sequence;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;


//our painter extends AbstractPainter as it provides painter facilities
public class OverlayTrapMouse extends Overlay
{
    private Point5D.Double Pt;
	private JButton		pickColorButton		= null;
	private JComboBox<Color> colorPickCombo = null;
	private String textPickAPixel = null;

    public OverlayTrapMouse()
    {
        super("Simple overlay");
    }
    
	public void setMouseTrapOverlay (SequenceCamData seqCamData, boolean bActive, JButton pickColorButton, JComboBox<Color> colorPickCombo) {
		if (bActive) {
			if (seqCamData.trapOverlay == null)
				seqCamData.trapOverlay = new OverlayTrapMouse (pickColorButton, colorPickCombo);
			if (!seqCamData.seq.contains(seqCamData.trapOverlay))
				seqCamData.seq.addOverlay(seqCamData.trapOverlay);
		}
		else {
			if (seqCamData.trapOverlay != null && seqCamData.seq.contains(seqCamData.trapOverlay))
				seqCamData.seq.removeOverlay(seqCamData.trapOverlay);
			seqCamData.trapOverlay = null;
		}
	}
	
    public OverlayTrapMouse(JButton pickColorButton, JComboBox<Color> colorPickCombo) {
    	super("overlay with Jbutton and JComboBox");
    	this.pickColorButton = pickColorButton;
    	this.colorPickCombo = colorPickCombo;
    	textPickAPixel = pickColorButton.getText();
    }
    
    public OverlayTrapMouse(OverlayListener listener) {
    	super("Simple overlay");
    	addOverlayListener(listener);
    }
    
    public void attachInterfaceElements( JButton pickColorButton, JComboBox<Color> colorPickCombo) {
    	this.pickColorButton = pickColorButton;
    	this.colorPickCombo = colorPickCombo;
    	textPickAPixel = pickColorButton.getText();
    }

    @Override
    public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        // check if we are dealing with a 2D canvas and we have a valid image position
        if ((canvas instanceof IcyCanvas2D) && (imagePoint != null))
            onMouseClicked(canvas.getSequence(), canvas.getPositionT(), imagePoint);
    }

    @Override
    public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
         // check if we are dealing with a 2D canvas and we have a valid image position
        if ((canvas instanceof IcyCanvas2D) && (imagePoint != null))
            onMouseMoved(canvas.getSequence(), canvas.getPositionT(), imagePoint);
    }
 
    private void onMouseClicked(Sequence sequence, int posT, Point5D.Double imagePoint)
    {
        Color c = getRGB(sequence, posT, imagePoint);
        if (c != null && pickColorButton != null) {
            pickColorButton.setBackground(c);
			boolean isnewcolor = true;
			int isel = 0;
			for (int i=0; i < colorPickCombo.getItemCount(); i++) {
				if (c.equals(colorPickCombo.getItemAt(i)) ) {
					isnewcolor = false;
					isel = i;
				}
			}
			
			if (isnewcolor) {
				colorPickCombo.addItem(c);
				isel = colorPickCombo.getItemCount()-1;
			}
			colorPickCombo.setSelectedIndex(isel);
			pickColorButton.setBackground(Color.LIGHT_GRAY);
			pickColorButton.setText(textPickAPixel);
        }     
    }


    private void onMouseMoved(Sequence sequence, int posT, Point5D.Double imagePoint)
    {
        Color c = getRGB(sequence, posT, imagePoint);
        if (c != null && pickColorButton != null) {
        	pickColorButton.setBackground(c);
			String cs = Integer.toString(c.getRed()) + ":"+ Integer.toString(c.getGreen()) +":" + Integer.toString(c.getBlue());
			pickColorButton.setText(cs);
        }
    }

    private Color getRGB(Sequence sequence, int posT, Point5D.Double imagePoint)
    {
        int x = (int) imagePoint.getX();
        int y = (int) imagePoint.getY();
        setPt(imagePoint);
        IcyBufferedImage image = sequence.getImage(posT, 0);
        boolean isInside = image.isInside(new Point(x, y));

        if (isInside)
        {
            int argb = image.getRGB(x, y);
            int r = (argb>>16) & 0xFF;
            int g = (argb>>8) & 0xFF;
            int b = (argb>>0) & 0xFF;
            return new Color(r, g, b);
        }
        return null;
    }

	/**
	 * @return the pt
	 */
	public Point5D.Double getPt() {
		return Pt;
	}

	/**
	 * @param pt the pt to set
	 */
	public void setPt(Point5D.Double pt) {
		Pt = pt;
	}

}
