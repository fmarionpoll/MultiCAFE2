package plugins.fmp.multicafeTools;

import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import plugins.fmp.multicafeSequence.Experiment;
import plugins.fmp.multicafeSequence.ExperimentList;




public class DetectFlies1_series extends SwingWorker<Integer, Integer> {

	private Viewer 				viewerCamData 	= null;
	private OverlayThreshold 	ov 				= null;
	
	public boolean 				stopFlag 		= false;
	public boolean 				threadRunning 	= false;
	public boolean				buildBackground	= true;
	public boolean				detectFlies		= true;
	public DetectFlies_Options 	detect 			= new DetectFlies_Options();

	// -----------------------------------------------------
	@Override
	protected Integer doInBackground() throws Exception
    {
        threadRunning = true;
        int nbiterations = 0;
		ExperimentList expList = detect.expList;
		int nbexp = expList.index1 - expList.index0 +1;
		ProgressChrono progressBar = new ProgressChrono("Detect flies");
		progressBar.initChrono(nbexp);
		progressBar.setMessageFirstPart("Analyze series ");
		for (int index = expList.index0; index <= expList.index1 && !stopFlag; index++, nbiterations++) {
			if (stopFlag) 
				break;
			Experiment exp = expList.experimentList.get(index);
			System.out.println(exp.experimentFileName);
			progressBar.updatePosition(index-expList.index0+1);
			
			exp.loadExperimentCamData();
			exp.seqCamData.xmlReadDrosoTrackDefault();
			runDetectFlies(exp);
			if (!stopFlag)
				saveComputation(exp);
			exp.seqCamData.seq.close();
		}
		progressBar.close();
		threadRunning = false;
		return nbiterations;
    }

	@Override
	protected void done()
    {
		// this method is called when the background thread finishes execution 
        try 
        { 
            int statusMsg = get(); 
            System.out.println("iterations done: "+statusMsg);
            if (!threadRunning || stopFlag) {
            	firePropertyChange("thread_ended", null, null);
            }
            else {
            	firePropertyChange("thread_done", null, statusMsg);
            }
        }  
        catch (InterruptedException e)  
        { 
            e.printStackTrace(); 
        }  
        catch (ExecutionException e)  
        { 
            e.printStackTrace(); 
        } 
    }
	
	private void saveComputation(Experiment exp) {			
		Path dir = Paths.get(exp.seqCamData.getDirectory());
		dir = dir.resolve("results");
		String directory = dir.toAbsolutePath().toString();
		if (Files.notExists(dir))  {
			try {
				Files.createDirectory(dir);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Creating directory failed: "+ directory);
				return;
			}
		}
		exp.saveFlyPositions();
	}
	
	private void runDetectFlies(Experiment exp) {
		detect.seqCamData = exp.seqCamData;
		detect.initParametersForDetection();
		detect.initTempRectROIs(detect.seqCamData.seq);
		ProgressChrono progressBar = new ProgressChrono("Detecting flies...");
		progressBar.initChrono(detect.endFrame-detect.startFrame+1);
		try {
			SwingUtilities.invokeAndWait(new Runnable() { public void run() {
				viewerCamData = new Viewer(exp.seqCamData.seq, true);
				Rectangle rectv = viewerCamData.getBoundsInternal();
				rectv.setLocation(detect.parent0Rect.x+ detect.parent0Rect.width, detect.parent0Rect.y);
				viewerCamData.setBounds(rectv);
				ov = new OverlayThreshold(exp.seqCamData);
				detect.seqCamData.seq.addOverlay(ov);	
				ov.setThresholdSingle(detect.seqCamData.cages.detect.threshold);
				ov.painterChanged();
			}});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		} 
		
		detect.seqCamData.seq.beginUpdate();
		int it = 0;
		for (int t = detect.startFrame ; t <= detect.endFrame; t  += detect.analyzeStep, it++ ) {				
			if (stopFlag)
				break;
			progressBar.updatePositionAndTimeLeft(t);
			IcyBufferedImage workImage = detect.seqCamData.getImageAndSubtractReference(t, detect.transformop); 
			if (workImage == null)
				continue;
			detect.seqCamData.currentFrame = t;
			viewerCamData.setPositionT(t);
			viewerCamData.setTitle(detect.seqCamData.getDecoratedImageName(t));
			detect.findFlies (workImage, t, it);
		}
	
		detect.seqCamData.seq.endUpdate();
		detect.removeTempRectROIs(detect.seqCamData.seq);
		if (!stopFlag)
			detect.copyDetectedROIsToSequence(detect.seqCamData.seq);
		progressBar.close();
	}
	
	
}