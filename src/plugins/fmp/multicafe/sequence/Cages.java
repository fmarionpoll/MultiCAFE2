package plugins.fmp.multicafe.sequence;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.util.XMLUtil;

import plugins.fmp.multicafe.tools.Comparators;
import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DShape;


public class Cages {
	
	public List<Cage>	cageList			= new ArrayList<Cage>();
	public boolean		saveDetectedROIs	= false;
	// ---------- not saved to xml:
	public long			detectFirst_Ms		= 0;
	public long			detectLast_Ms		= 0;
	public long			detectBin_Ms		= 60000;
	public int			detect_threshold	= 0;
	public int			detect_nframes		= 0;
	// ----------------------------

	private final String ID_CAGES 			= "Cages";
	private final String ID_NCAGES 			= "n_cages";
	private final String ID_DROSOTRACK 		= "drosoTrack";
	private final String ID_NBITEMS 		= "nb_items";
	private final String ID_CAGELIMITS 		= "Cage_Limits";
	private final String ID_FLYDETECTED 	= "Fly_Detected";
	
	

	public void clearAllMeasures() {
		for (Cage cage: cageList) {
			cage.clearMeasures();
		}
	}
	
	public void removeCages() {
		cageList.clear();
	}
	
	public void mergeLists(Cages cagesm) {
		for (Cage cagem : cagesm.cageList ) {
			if (!isPresent(cagem))
				cageList.add(cagem);
		}
	}
	
	boolean isPresent(Cage cagenew) {
		boolean flag = false;
		for (Cage cage: cageList) {
			if (cage.cageRoi.getName().contentEquals(cagenew.cageRoi.getName())) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	// -------------
	
	public boolean xmlWriteCagesToFile(String name, String directory) {
		String csFile = Dialog.saveFileAs(name, directory, "xml");
		if (csFile == null)
			return false;
		csFile.toLowerCase();
		if (!csFile.contains(".xml")) 
			csFile += ".xml";
		return xmlWriteCagesToFileNoQuestion(csFile);
	}
		
	public boolean xmlWriteCagesToFileNoQuestion(String tempname) {
		if (tempname == null) 
			return false;
		final Document doc = XMLUtil.createDocument(true);
		if (doc == null)
			return false;
		
		Node node = XMLUtil.addElement(XMLUtil.getRootElement(doc), ID_DROSOTRACK);
		if (node == null)
			return false;

		int index = 0;
		Element xmlVal = XMLUtil.addElement(node, ID_CAGES);
		int ncages = cageList.size();
		XMLUtil.setAttributeIntValue(xmlVal, ID_NCAGES, ncages);
		for (Cage cage: cageList) {
			cage.saveDetectedROIs = saveDetectedROIs;
			cage.xmlSaveCage(xmlVal, index);
			index++;
		}
	
		return XMLUtil.saveDocument(doc, tempname);
	}
		
	public boolean xmlReadCagesFromFile(SequenceCamData seq) {
		String [] filedummy = null;
		String filename = seq.getSequenceFileName();
		File file = new File(filename);
		String directory = file.getParentFile().getAbsolutePath();
		filedummy = Dialog.selectFiles(directory, "xml");
		boolean wasOk = false;
		if (filedummy != null) {
			for (int i= 0; i< filedummy.length; i++) {
				String csFile = filedummy[i];
				wasOk &= xmlReadCagesFromFileNoQuestion(csFile, seq);
			}
		}
		return wasOk;
	}
	
	public boolean xmlReadCagesFromFileNoQuestion(String tempname, SequenceCamData seq) {
		if (tempname == null) 
			return false;
		final Document doc = XMLUtil.loadDocument(tempname);
		if (doc == null)
			return false;
		boolean flag = xmlLoadCages(doc); 
		if (flag) {
			fromCagesToROIs(seq);
			fromDetectedFliesToROIs(seq);
		}
		else {
			System.out.println("failed to load cages from file");
			return false;
		}
		return true;
	}
	
	private boolean xmlLoadCages (Document doc) {
		Node node = XMLUtil.getElement(XMLUtil.getRootElement(doc), ID_DROSOTRACK);
		if (node == null)
			return false;
		cageList.clear();
		
		Element xmlVal = XMLUtil.getElement(node, ID_CAGES);
		if (xmlVal != null) {
			int ncages = XMLUtil.getAttributeIntValue(xmlVal, ID_NCAGES, 0);
			for (int index = 0; index < ncages; index++) {
				Cage cage = new Cage();
				cage.xmlLoadCage(xmlVal, index);
				cageList.add(cage);
			}
		} else {
			List<ROI2D> cageLimitROIList = new ArrayList<ROI2D>();
			if (xmlLoadCagesLimits_v0(node, cageLimitROIList)) {
				List<XYTaSeries> flyPositionsList = new ArrayList<XYTaSeries>();
				xmlLoadFlyPositions_v0(node, flyPositionsList);
				transferDataToCages_v0(cageLimitROIList, flyPositionsList);
			}
			else
				return false;
		}
		return true;
	}
	
	// --------------
	
	public void copy (Cages cag) {
		
//		detect.copyParameters(cag.detect);
		
		cageList.clear();
		for (Cage ccag: cag.cageList) {
			Cage cagi = new Cage();
			cagi.copy(ccag);
			cageList.add(cagi);
		}
	}
	
	private void transferDataToCages_v0(List<ROI2D> cageLimitROIList, List<XYTaSeries> flyPositionsList) {
		cageList.clear();
		Collections.sort(cageLimitROIList, new Comparators.ROI2DNameComparator());
		int ncages = cageLimitROIList.size();
		for (int index=0; index< ncages; index++) {
			Cage cage = new Cage();
			cage.cageRoi = cageLimitROIList.get(index);
			cage.flyPositions = flyPositionsList.get(index);
			cageList.add(cage);
		}
	}

	private boolean xmlLoadCagesLimits_v0(Node node, List<ROI2D> cageLimitROIList) {
		if (node == null)
			return false;
		Element xmlVal = XMLUtil.getElement(node, ID_CAGELIMITS);
		if (xmlVal == null) 
			return false;
		
		cageLimitROIList.clear();
		int nb_items =  XMLUtil.getAttributeIntValue(xmlVal, ID_NBITEMS, 0);
		for (int i=0; i< nb_items; i++) {
			ROI2DPolygon roi = (ROI2DPolygon) ROI.create("plugins.kernel.roi.roi2d.ROI2DPolygon");
			Element subnode = XMLUtil.getElement(xmlVal, "cage"+i);
			roi.loadFromXML(subnode);
			cageLimitROIList.add((ROI2D) roi);
		}
		return true;
	}
	
	private boolean xmlLoadFlyPositions_v0(Node node, List<XYTaSeries> flyPositionsList) {
		if (node == null)
			return false;
		Element xmlVal = XMLUtil.getElement(node, ID_FLYDETECTED);
		if (xmlVal == null) 
			return false;
		
		flyPositionsList.clear();
		int nb_items =  XMLUtil.getAttributeIntValue(xmlVal, ID_NBITEMS, 0);
		int ielement = 0;
		for (int i=0; i< nb_items; i++) {
			Element subnode = XMLUtil.getElement(xmlVal, "cage"+ielement);
			XYTaSeries pos = new XYTaSeries();
			pos.loadFromXML(subnode);
			flyPositionsList.add(pos);
			ielement++;
		}
		return true;
	}
	
	public void fromCagesToROIs(SequenceCamData seqCamData) {
		List <ROI2D> cageLimitROIList = getRoisWithCageName(seqCamData);
		seqCamData.seq.removeROIs(cageLimitROIList, false);
		for (Cage cage: cageList) {
			cageLimitROIList.add(cage.cageRoi);
		}
		seqCamData.seq.addROIs(cageLimitROIList, true);
	}
	
	public void getCagesFromROIs(SequenceCamData seqCamData) {
		List <ROI2D> roiList = getRoisWithCageName(seqCamData);
		Collections.sort(roiList, new Comparators.ROI2DNameComparator());
		addMissingCages(roiList);
		removeOrphanCages(roiList);
		Collections.sort(cageList, new Comparators.CageNameComparator());
	}
	
	public void setFirstAndLastCageToZeroFly() {
		for (Cage cage: cageList) {
			if (cage.cageRoi.getName().contains("000") || cage.cageRoi.getName().contains("009"))
				cage.cageNFlies = 0;
		}
	}
	
	private void addMissingCages(List<ROI2D> roiList) {
		for (ROI2D roi:roiList) {
			boolean found = false;
			if (roi.getName() == null)
				break;
			for (Cage cage: cageList) {
				if (cage.cageRoi == null)
					break;
				if (roi.getName().equals(cage.cageRoi.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				Cage cage = new Cage();
				cage.cageRoi = roi;
				cageList.add(cage);
			}
		}
	}
	
	private void removeOrphanCages(List<ROI2D> roiList) {
		// remove cages with names not in the list
		Iterator<Cage> iterator = cageList.iterator();
		while (iterator.hasNext()) {
			Cage cage = iterator.next();
			boolean found = false;
			if (cage.cageRoi != null) {
				String cageRoiName = cage.cageRoi.getName();
				for (ROI2D roi: roiList) {
					if (roi.getName().equals(cageRoiName)) {
						found = true;
						break;
					}
				}
			}
			if (!found ) {
				iterator.remove();
			}
		}
	}
	
	private List <ROI2D> getRoisWithCageName(SequenceCamData seqCamData) {
		List<ROI2D> roiList = seqCamData.seq.getROI2Ds();
		List<ROI2D> cageList = new ArrayList<ROI2D>();
		for ( ROI2D roi : roiList ) {
			String csName = roi.getName();
			if ((roi instanceof ROI2DPolygon) || (roi instanceof ROI2DArea)) {
//				if (( csName.contains( "cage") 
				if ((csName.length() > 4 && csName.substring( 0 , 4 ).contains("cage")
					|| csName.contains("Polygon2D")) ) {
					cageList.add(roi);
				}
			}
		}
		return cageList;
	}
	
	public void fromDetectedFliesToROIs(SequenceCamData seqCamData) {
		ArrayList<ROI2D> seqlist = seqCamData.seq.getROI2Ds();
		for (ROI2D roi: seqlist) {
			if (!(roi instanceof ROI2DShape))
				continue;
			if (!roi.getName().contains("det"))
				continue;
			seqCamData.seq.removeROI(roi);
		}
		
		List<ROI> detectedFliesList = new ArrayList<ROI>();
		for (Cage cage: cageList) {
			detectedFliesList.addAll(cage.detectedROIsList);
		}
		seqCamData.seq.addROIs(detectedFliesList, true);
	}
	
	public int removeAllRoiCagesFromSequence(SequenceCamData seqCamData) {
		String cageRoot = "cage";
		int iRoot = -1;
		for (ROI roi: seqCamData.seq.getROIs()) {
			if (roi.getName().contains(cageRoot)) {
				String left = roi.getName().substring(4);
				int item = Integer.valueOf(left);
				iRoot = Math.max(iRoot, item);
			}
		}
		iRoot++;
		return iRoot;
	}
	
	public void transferNFliesFromCapillariesToCages(List<Capillary> capList) {
		for (Cage cage: cageList ) {
			int cagenb = cage.getCageNumberInteger();
			for (Capillary cap: capList) {
				if (cap.capCageID != cagenb)
					continue;
				cage.cageNFlies = cap.capNFlies;
			}
		}
	}
		
	public void transferNFliesFromCagesToCapillaries(List<Capillary> capList) {
		for (Cage cage: cageList ) {
			int cagenb = cage.getCageNumberInteger();
			for (Capillary cap: capList) {
				if (cap.capCageID != cagenb)
					continue;
				cap.capNFlies = cage.cageNFlies;
			}
		}
	}
	
	public void setCageNbFromName(List<Capillary> capList) {
		for (Capillary cap: capList) {
			int cagenb = cap.getCageIndexFromRoiName();
			cap.capCageID = cagenb;
		}
	}
	
	public void displayDetectedFliesAsRois(Sequence seq, boolean isVisible) {
		for (Cage cage: cageList ) {
			if (cage.detectedROIsList.size() >0) {
				
			} else {
				if (isVisible) {
					cage.transferPositionsToRois();
					seq.addROIs(cage.detectedROIsList, false);
				}
			}
		}
	}
	
	public Cage getCageFromNumber (int number) {
		Cage cageFound = null;
		for (Cage cage: cageList) {
			if (number == cage.getCageNumberInteger()) {
				cageFound = cage;
				break;
			}
		}
		return cageFound;
	}
}
