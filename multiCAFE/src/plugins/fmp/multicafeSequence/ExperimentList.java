package plugins.fmp.multicafeSequence;


import java.util.ArrayList;

import icy.gui.frame.progress.ProgressFrame;

public class ExperimentList {
	
	public ArrayList<Experiment> experimentList = null;
	
	public ExperimentList () {
		experimentList = new ArrayList<Experiment> ();
	}
	
	public Experiment getStartAndEndFromAllExperiments() {
		
		ProgressFrame progress = new ProgressFrame("Get time start and time end across all experiments");
		progress.setLength(experimentList.size());

		Experiment expglobal = new Experiment();
		Experiment exp0 = experimentList.get(0);
		expglobal.fileTimeImageFirst = exp0.fileTimeImageFirst;
		expglobal.fileTimeImageLast = exp0.fileTimeImageLast;
		
		for (Experiment exp: experimentList) 
		{
			if (expglobal.fileTimeImageFirst.compareTo(exp.fileTimeImageFirst) > 0) 
				expglobal.fileTimeImageFirst = exp.fileTimeImageFirst;
			if (expglobal.fileTimeImageLast .compareTo(exp.fileTimeImageLast) <0)
				expglobal.fileTimeImageLast = exp.fileTimeImageLast;
			if (expglobal.number_of_frames < exp.vSequence.getSizeT())
				expglobal.number_of_frames = exp.vSequence.getSizeT();
			if (exp.vSequence.analysisEnd > exp.vSequence.getSizeT()-1)
				exp.vSequence.analysisEnd = exp.vSequence.getSizeT()-1;
			progress.incPosition();
		}
		
		expglobal.fileTimeImageFirstMinutes = expglobal.fileTimeImageFirst.toMillis()/60000;
		expglobal.fileTimeImageLastMinutes = expglobal.fileTimeImageLast.toMillis()/60000;
		progress.close();
		return expglobal;
	}
	
	public boolean readInfosFromAllExperiments() {

		ProgressFrame progress = new ProgressFrame("Load experiment(s) parameters");
		progress.setLength(experimentList.size());
		
		boolean flag = true;
		for (Experiment exp: experimentList) 
		{
			boolean ok = exp.openSequenceAndMeasures();
			flag &= ok;
			progress.incPosition();
		}
		progress.close();
		return flag;
	}
	
	public boolean chainExperiments() {
		
		boolean flagOK = true;
		for (Experiment exp: experimentList) {
			for (Experiment expi: experimentList) {
				if (expi == exp)
					continue;
				
				if (expi.boxID .equals(exp.boxID)) {
					// if before, insert eventually
					if (expi.fileTimeImageLastMinutes < exp.fileTimeImageFirstMinutes) {
						if (exp.expPrevious == null)
							exp.expPrevious = expi;
						else if (expi.fileTimeImageLastMinutes > exp.expPrevious.fileTimeImageLastMinutes ) {
							exp.expPrevious.expNext = expi;
							exp.expPrevious = expi;
						}
						continue;
					}
					// if after, insert eventually
					if (expi.fileTimeImageFirstMinutes > exp.fileTimeImageLastMinutes) {
						if (exp.expNext == null)
							exp.expNext = expi;
						else if (expi.fileTimeImageFirstMinutes < exp.expNext.fileTimeImageFirstMinutes ) {
							exp.expNext.expPrevious = expi;
							exp.expNext = expi;
						}
						continue;
					}
					// it should never arrive here
					flagOK = false;
				}
			}
		}
		return flagOK;
	}
}
