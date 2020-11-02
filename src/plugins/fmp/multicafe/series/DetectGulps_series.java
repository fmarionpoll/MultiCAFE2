package plugins.fmp.multicafe.series;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import icy.gui.frame.progress.ProgressFrame;
import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import icy.type.collection.array.Array1DUtil;
import icy.type.geom.Polyline2D;
import plugins.fmp.multicafe.sequence.Capillary;
import plugins.fmp.multicafe.sequence.CapillaryLimits;
import plugins.fmp.multicafe.sequence.Experiment;

import plugins.fmp.multicafe.sequence.SequenceKymos;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;



public class DetectGulps_series extends BuildSeries  {
	
	void analyzeExperiment(Experiment exp) {
		exp.loadExperimentCapillariesData_ForSeries();
		if ( exp.loadKymographs()) {
			buildFilteredImage(exp);
			detectGulps(exp);
			exp.xmlSaveMCcapillaries();
		}
		exp.seqKymos.closeSequence();
	}

	private void buildFilteredImage(Experiment exp) {
		if (exp.seqKymos == null)
			return;
		int zChannelDestination = 2;
		exp.kymosBuildFiltered(0, zChannelDestination, options.transformForGulps, options.spanDiff);
	}
	
	public void detectGulps(Experiment exp) {			
		SequenceKymos seqKymos = exp.seqKymos;
		
		int jitter = 5;
		int firstkymo = 0;
		int lastkymo = seqKymos.seq.getSizeT() -1;
		if (!options.detectAllGulps) {
			firstkymo = options.firstkymo;
			lastkymo = firstkymo;
		}
		seqKymos.seq.beginUpdate();
		threadRunning = true;
		stopFlag = false;
		ProgressFrame progressBar = new ProgressFrame("Processing with subthreads started");
		
		int nframes = lastkymo - firstkymo +1;
	    final Processor processor = new Processor(SystemUtil.getNumberOfCPUs());
	    processor.setThreadName("detect_levels");
	    processor.setPriority(Processor.NORM_PRIORITY);
        ArrayList<Future<?>> futures = new ArrayList<Future<?>>(nframes);
		futures.clear();
		
		for (int frame = firstkymo; frame <= lastkymo; frame++) {
			final Capillary cap = exp.capillaries.capillariesArrayList.get(frame);
			cap.setGulpsOptions(options);
			
			final int t_from = frame;;
			futures.add(processor.submit(new Runnable () {
			@Override
			public void run() {
				if (options.buildDerivative) {
					seqKymos.removeRoisContainingString(t_from, "derivative");
					getDerivativeProfile(seqKymos.seq, t_from, cap, jitter);	
				}
				if (options.buildGulps) {
					cap.cleanGulps();
					seqKymos.removeRoisContainingString(t_from, "gulp");
					cap.getGulps(t_from);
					if (cap.gulpsRois.rois.size() > 0)
						seqKymos.seq.addROIs(cap.gulpsRois.rois, false);
				}
			}
			}));
		}
		waitAnalyzeExperimentCompletion(processor, futures, progressBar);
		seqKymos.seq.endUpdate();
		progressBar.close();
		processor.shutdown();
	}	

	private void getDerivativeProfile(Sequence seq, int indexkymo, Capillary cap, int jitter) {	
		int z = seq.getSizeZ() -1;
		IcyBufferedImage image = seq.getImage(indexkymo, z, 0);
		List<Point2D> listOfMaxPoints = new ArrayList<>();
		int[] kymoImageValues = Array1DUtil.arrayToIntArray(image.getDataXY(0), image.isSignedDataType());	// channel 0 - RED
		int xwidth = image.getSizeX();
		int yheight = image.getSizeY();
		int ix = 0;
		int iy = 0;
		Polyline2D 	polyline = cap.ptsTop.polylineLimit;
		if (polyline == null)
			return;
		for (ix = 1; ix < polyline.npoints; ix++) {
			// for each point of topLevelArray, define a bracket of rows to look at ("jitter" = 10)
			int low = (int) polyline.ypoints[ix]- jitter;
			int high = low + 2*jitter;
			if (low < 0) 
				low = 0;
			if (high >= yheight) 
				high = yheight-1;
			int max = kymoImageValues [ix + low*xwidth];
			for (iy = low+1; iy < high; iy++) {
				int val = kymoImageValues [ix  + iy*xwidth];
				if (max < val) 
					max = val;
			}
			listOfMaxPoints.add(new Point2D.Double((double) ix, (double) max));
		}
		ROI2DPolyLine roiDerivative = new ROI2DPolyLine ();
		roiDerivative.setName(cap.getLast2ofCapillaryName()+"_derivative");
		roiDerivative.setColor(Color.yellow);
		roiDerivative.setStroke(1);
		roiDerivative.setPoints(listOfMaxPoints);
		roiDerivative.setT(indexkymo);
		seq.addROI(roiDerivative, false);
		cap.ptsDerivative = new CapillaryLimits(roiDerivative.getName(), indexkymo, roiDerivative.getPolyline2D());
	}
	
}


