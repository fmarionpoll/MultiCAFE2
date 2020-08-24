package plugins.fmp.multicafeTools.Detect;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import icy.gui.frame.progress.ProgressFrame;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.roi.BooleanMask2D;
import icy.sequence.Sequence;
import icy.type.collection.array.Array1DUtil;
import plugins.fmp.multicafeSequence.Cage;
import plugins.fmp.multicafeSequence.Experiment;
import plugins.fmp.multicafeSequence.ExperimentList;
import plugins.fmp.multicafeSequence.SequenceCamData;
import plugins.fmp.multicafeTools.ProgressChrono;
import plugins.kernel.roi.roi2d.ROI2DArea;



public class DetectFlies2_series extends SwingWorker<Integer, Integer> {

	private List<Boolean> initialflyRemovedList = new ArrayList<Boolean>();
	private Viewer viewerCamData;
	private Viewer vPositive = null;
	private Viewer vBackgroundImage = null;;

	public boolean stopFlag = false;
	public boolean threadRunning = false;

	public Sequence seqNegative = new Sequence();
	public Sequence seqPositive = new Sequence();
	public boolean viewInternalImages = false;
	public DetectFlies_Options	options	= null;
	
	private DetectFlies_Find 	detect 	= new DetectFlies_Find();
	

	// -----------------------------------------
	
	@Override
	protected Integer doInBackground() throws Exception {
		if (options == null)
			return 0;
		System.out.println("start detect flies thread (v2)");
        threadRunning = true;
		int nbiterations = 0;
		ExperimentList expList = options.expList;
		ProgressFrame progress = new ProgressFrame("Detect flies");
		options.btrackWhite = true;
		
		for (int index = expList.index0; index <= expList.index1; index++, nbiterations++) {
			if (stopFlag) 
				break;
			Experiment exp = expList.getExperiment(index);
			System.out.println((index+1)+": " +exp.getExperimentFileName());
			progress.setMessage("Processing file: " + (index+1) + "//" + (expList.index1+1));
			
			exp.resultsSubPath = options.resultsSubPath;
			exp.getResultsDirectory(); 
			
			exp.xmlLoadExperiment();
			exp.seqCamData.loadSequence(exp.getExperimentFileName()) ;
			exp.xmlReadDrosoTrack(null);
			exp.setCagesFrameStep(options.df_stepFrame);
			if (options.isFrameFixed) {
				exp.setCagesFrameStart ( options.df_startFrame);
				exp.setCagesFrameEnd ( options.df_endFrame);
				if (exp.getCagesFrameEnd() > (exp.getSeqCamSizeT() - 1))
					exp.setCagesFrameEnd( exp.getSeqCamSizeT() - 1);
			} else {
				exp.setCagesFrameStart (0);
				exp.setCagesFrameEnd (exp.seqCamData.seq.getSizeT() - 1);
			}
			
			if (exp.cages.cageList.size() < 1 ) {
				System.out.println("! skipped experiment with no cage: " + exp.getExperimentFileName());
			} else {
				System.out.println((index+1) + " - "+ exp.getExperimentFileName() + " " + exp.resultsSubPath);
				runDetectFlies(exp);
			}

			exp.seqCamData.closeSequence();
		}
		progress.close();
		threadRunning = false;
		return nbiterations;
	}
	
	@Override
	protected void done() {
		int statusMsg = 0;
		try {
			statusMsg = get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		} 
//		System.out.println("iterations done: "+statusMsg);
		if (!threadRunning || stopFlag) {
			firePropertyChange("thread_ended", null, statusMsg);
		} else {
			firePropertyChange("thread_done", null, statusMsg);
		}
    }

	private void runDetectFlies(Experiment exp) {
		if (seqNegative == null)
			seqNegative = new Sequence();
		if (seqPositive == null)
			seqPositive = new Sequence();
		
		detect.initParametersForDetection(exp, options);
		options.threshold = options.thresholdDiff;
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					viewerCamData = new Viewer(exp.seqCamData.seq, true);
					Rectangle rectv = viewerCamData.getBoundsInternal();
					rectv.setLocation(options.parent0Rect.x+ options.parent0Rect.width, options.parent0Rect.y);
					viewerCamData.setBounds(rectv);
				}});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		
		boolean flag = options.forceBuildBackground;
		flag |= (!exp.loadReferenceImage());
		flag |= (exp.seqCamData.refImage == null);
		if (flag) {
			System.out.println(" buildbackground");
			buildBackgroundImage(exp);
			exp.saveReferenceImage();
		}
		
		if (options.detectFlies) {
			exp.cleanPreviousFliesDetections();
			findFlies(exp);
			exp.xmlSaveFlyPositionsForAllCages();
		}
		closeViewersAndSequences (exp);
	}
	
	private void closeViewersAndSequences (Experiment exp) {
		if (seqNegative != null) {
			seqNegative.close();
			seqNegative = null;
		}
		if (seqPositive != null) {
			if (vPositive != null)
				vPositive.close();
			seqPositive.close();
			seqPositive = null;
		}
		if (exp.seqBackgroundImage != null) {
			exp.seqBackgroundImage.close();
			exp.seqBackgroundImage = null;
		}
	}

	private void findFlies(Experiment exp) {
		ProgressChrono progressBar = new ProgressChrono("Detecting flies...");
		progressBar.initChrono(exp.getCagesFrameEnd()-exp.getCagesFrameStart()+1);

		exp.seqBackgroundImage.close();
		if (vPositive != null) {
			vPositive.close();
			vPositive = null;
		}
		if (vBackgroundImage != null) {
			vBackgroundImage.close();
			vBackgroundImage = null;
		}
		detect.initTempRectROIs(exp, seqNegative);

		try {
			viewerCamData = exp.seqCamData.seq.getFirstViewer();
			exp.seqCamData.seq.beginUpdate();
			if (viewInternalImages)
				displayDetectViewer(exp);

			// ----------------- loop over all images of the stack
			int it = 0;
			for (int t = exp.getCagesFrameStart(); t <= exp.getCagesFrameEnd(); t += exp.getCagesFrameStep(), it++) {
				if (stopFlag)
					break;
				progressBar.updatePosition(t);
				IcyBufferedImage workImage = exp.seqCamData.getImage(t, 0);
				if (workImage == null)
					continue;
				IcyBufferedImage currentImage = IcyBufferedImageUtil.getCopy(workImage);
				exp.seqCamData.currentFrame = t;
				seqNegative.beginUpdate();
				IcyBufferedImage negativeImage = exp.seqCamData.subtractImages(exp.seqCamData.refImage, currentImage);
				detect.findFlies(negativeImage, t, it);
				seqNegative.setImage(0, 0, negativeImage);
				seqNegative.endUpdate();
			}
		} finally {
			exp.seqCamData.seq.endUpdate();
			seqNegative.close();
			detect.copyDetectedROIsToSequence(exp);
			detect.copyDetectedROIsToCages(exp);
		}
		progressBar.close();
	}

	private void patchRectToReferenceImage(SequenceCamData seqCamData, IcyBufferedImage currentImage, Rectangle rect) {
		int cmax = currentImage.getSizeC();
		for (int c = 0; c < cmax; c++) {
			int[] intCurrentImage = Array1DUtil.arrayToIntArray(currentImage.getDataXY(c),
					currentImage.isSignedDataType());
			int[] intRefImage = Array1DUtil.arrayToIntArray(seqCamData.refImage.getDataXY(c),
					seqCamData.refImage.isSignedDataType());
			int xwidth = currentImage.getSizeX();
			for (int x = 0; x < rect.width; x++) {
				for (int y = 0; y < rect.height; y++) {
					int xi = rect.x + x;
					int yi = rect.y + y;
					int coord = xi + yi * xwidth;
					intRefImage[coord] = intCurrentImage[coord];
				}
			}
			Object destArray = seqCamData.refImage.getDataXY(c);
			Array1DUtil.intArrayToSafeArray(intRefImage, destArray, seqCamData.refImage.isSignedDataType(),
					seqCamData.refImage.isSignedDataType());
			seqCamData.refImage.setDataXY(c, destArray);
		}
		seqCamData.refImage.dataChanged();
	}

	private void displayDetectViewer(Experiment exp) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Viewer vNegative = new Viewer(seqNegative, false);
					seqNegative.setName("detectionImage");
					seqNegative.setImage(0, 0, exp.seqCamData.refImage);
					Point pt = viewerCamData.getLocation();
					if (vNegative != null) {
						vNegative.setLocation(pt);
						vNegative.setVisible(true);
					}
				}});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void displayRefViewers(Experiment exp) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if (exp.seqBackgroundImage == null)
						exp.seqBackgroundImage = new Sequence();
					if (vBackgroundImage == null)
						vBackgroundImage = new Viewer(exp.seqBackgroundImage, false);
					exp.seqBackgroundImage.setName("referenceImage");
					exp.seqBackgroundImage.setImage(0, 0,IcyBufferedImageUtil.getSubImage(exp.seqCamData.refImage, detect.rectangleAllCages));
					
					if (seqPositive == null)
						seqPositive = new Sequence();
					if (vPositive == null)
						vPositive = new Viewer(seqPositive, false);
					seqPositive.setName("positiveImage");
					seqPositive.setImage(0, 0, IcyBufferedImageUtil.getSubImage(exp.seqCamData.refImage, detect.rectangleAllCages));
			
					viewerCamData = exp.seqCamData.seq.getFirstViewer();
					Point pt = viewerCamData.getLocation();
					int height = viewerCamData.getHeight();
					pt.y += height;
			
					if (vPositive != null) {
						vPositive.setVisible(true);
						vPositive.setLocation(pt);
					}
					if (vBackgroundImage != null) {
						vBackgroundImage.setVisible(true);
						vBackgroundImage.setLocation(pt);
					}
				}});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void buildBackgroundImage(Experiment exp) {
		ProgressFrame progress = new ProgressFrame("Build background image...");
		int nfliesRemoved = 0; //
		detect.initParametersForDetection(exp, options);
		exp.seqCamData.refImage = IcyBufferedImageUtil.getCopy(exp.seqCamData.getImage(options.df_startFrame, 0));
		initialflyRemovedList.clear();
		int ndetectcages = exp.cages.cageList.size();
		for (int i = 0; i < ndetectcages; i++)
			initialflyRemovedList.add(false);

		viewerCamData = exp.seqCamData.seq.getFirstViewer();
		displayRefViewers(exp);
		int limit = 50 * exp.getCagesFrameStep();
		if (limit > exp.getSeqCamSizeT())
			limit = exp.getSeqCamSizeT();
		
		for (int t = exp.getCagesFrameStart() + 1; t <= limit && !stopFlag; t += exp.getCagesFrameStep()) {
			IcyBufferedImage currentImage = exp.seqCamData.getImage(t, 0);
			exp.seqCamData.currentFrame = t;
			viewerCamData.setPositionT(t);
			viewerCamData.setTitle(exp.seqCamData.getDecoratedImageName(t));

			IcyBufferedImage positiveImage = exp.seqCamData.subtractImages(currentImage, exp.seqCamData.refImage);
			seqPositive.setImage(0, 0, IcyBufferedImageUtil.getSubImage(positiveImage, detect.rectangleAllCages));
			ROI2DArea roiAll = detect.binarizeImage(positiveImage, options.thresholdBckgnd);
			 
			for (int icage = 0; icage <= ndetectcages - 1; icage++) {
				Cage cage = exp.cages.cageList.get(icage);
				if (cage.cageNFlies != 1)
					continue;
				BooleanMask2D bestMask = detect.findLargestBlob(roiAll, icage);
				if (bestMask != null) {
					ROI2DArea flyROI = new ROI2DArea(bestMask);
					if (!initialflyRemovedList.get(icage)) {
						Rectangle rect = flyROI.getBounds();
						patchRectToReferenceImage(exp.seqCamData, currentImage, rect);
						initialflyRemovedList.set(icage, true);
						nfliesRemoved++;
						if (exp.seqBackgroundImage != null)
							exp.seqBackgroundImage.setImage(0, 0, IcyBufferedImageUtil.getSubImage(exp.seqCamData.refImage,
									detect.rectangleAllCages));
						progress.setMessage("Build background image: n flies removed =" + nfliesRemoved);
					}
				}
			}
			if (nfliesRemoved == ndetectcages)
				break;
		}
		progress.close();
	}


}