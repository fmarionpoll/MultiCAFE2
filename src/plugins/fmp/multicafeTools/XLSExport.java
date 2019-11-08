package plugins.fmp.multicafeTools;

import java.awt.Point;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import plugins.fmp.multicafeSequence.Capillary;
import plugins.fmp.multicafeSequence.Experiment;
import plugins.fmp.multicafeSequence.SequenceCamData;

public class XLSExport {

	protected XLSExportOptions 	options 	= null;
	protected Experiment 		expAll 		= null;
	int							nintervals	= 0;

	public long getnearest(long value, int step) {
		long diff0 = (value /step)*step;
		long diff1 = diff0 + step;
		if ((value - diff0 ) < (diff1 - value))
			value = diff0;
		else
			value = diff1;
		return value;
	}
		
	protected Point writeExperimentDescriptors(Experiment exp, String charSeries, XSSFSheet sheet, Point pt, boolean transpose) {	
		int row = pt.y;
		int col0 = pt.x;
		XLSUtils.setValue(sheet, pt, transpose, "..");
		pt.x++;
		XLSUtils.setValue(sheet, pt, transpose, "..");
		pt.x++;
		int colseries = pt.x;
		
		Path path = Paths.get(exp.seqCamData.getFileName());
		String boxID = exp.seqKymos.capillaries.desc.boxID;
		String experiment = exp.seqKymos.capillaries.desc.experiment;
		String comment = exp.seqKymos.capillaries.desc.comment;
		String stimulusL = exp.seqKymos.capillaries.desc.stimulusL;
		String stimulusR = exp.seqKymos.capillaries.desc.stimulusR;
		String concentrationL = exp.seqKymos.capillaries.desc.concentrationL;
		String concentrationR = exp.seqKymos.capillaries.desc.concentrationR;
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		String date = df.format(exp.getFileTimeImageFirst(false).toMillis());	
		String name0 = path.toString();
		String name1 = getSubName(path, 2);
		String cam = "-"; 
		if (name1.length() >= 5) cam = name1.substring(0, 5); 
		String name11 = getSubName(path, 3); 
		String name111 = getSubName(path, 4); 
		String sheetName = sheet.getSheetName();
		
		List<Capillary> capList = exp.seqKymos.capillaries.capillariesArrayList;
		
		for (int t=0; t< capList.size(); t++) { 
			Capillary cap = capList.get(t);
			String	name = cap.capillaryRoi.getName();
			int col = getColFromKymoFileName(name);
			if (col >= 0) 
				pt.x = colseries + col;
			pt.y = row;
			
			// path
			XLSUtils.setValue(sheet, pt, transpose, name0);
			pt.y++;
			
			// date
			XLSUtils.setValue(sheet, pt, transpose, date);
			pt.y++;
			
			XLSUtils.setValue(sheet, pt, transpose, boxID);
			pt.y++;
			XLSUtils.setValue(sheet, pt, transpose, experiment);
			pt.y++;
			XLSUtils.setValue(sheet, pt, transpose, comment);
			pt.y++;

			// stimulus, conc
			String letter = name.substring(name.length() - 1);
			if (letter .equals("L")) 	XLSUtils.setValue(sheet, pt, transpose, stimulusL);
			else						XLSUtils.setValue(sheet, pt, transpose, stimulusR);
			pt.y++;
			if (letter .equals("L")) 	XLSUtils.setValue(sheet, pt, transpose, concentrationL);
			else 						XLSUtils.setValue(sheet, pt, transpose, concentrationR);
			pt.y++;
			
			// cam
			XLSUtils.setValue(sheet, pt, transpose, cam);
			pt.y++;
			// cap
			XLSUtils.setValue(sheet, pt, transpose, letter);
			pt.y++;
			// cage
			int i = getCageFromCapillaryName(name);
			XLSUtils.setValue(sheet, pt, transpose, i);
			pt.y++;
			// cageID
			XLSUtils.setValue(sheet, pt, transpose, charSeries+i);
			pt.y++;
			// nflies
			int j = 1;
			if (i < 1 || i > 8)
				j = 0;
			XLSUtils.setValue(sheet, pt, transpose, j);
			pt.y++;
			// dum1
			XLSUtils.setValue(sheet, pt, transpose, name1);
			pt.y++;
			// dum2
			XLSUtils.setValue(sheet, pt, transpose, name11);
			pt.y++;
			// dum3
			XLSUtils.setValue(sheet, pt, transpose, name111);
			pt.y++;
			// dum4
			XLSUtils.setValue(sheet, pt, transpose, sheetName);
			pt.y++;
			// rois
//			XLSUtils.setValue(sheet, pt, transpose, exp.seqKymos.getFileName(t));
			pt.y++;
		}
		pt.x = col0;
		return pt;
	}
	
	public int outputFieldHeaders(XSSFSheet sheet, boolean transpose) {		
		Point pt = new Point(0,0);
		XLSUtils.setValue(sheet, pt, transpose, "path");
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.DATE.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.BOXID.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.EXPMT.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.COMMENT.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.STIM.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.CONC.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.CAM.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.CAP.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.CAGE.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.CAGEID.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.NFLIES.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.DUM1.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.DUM2.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.DUM3.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.DUM4.toString());
		pt.y++;
		XLSUtils.setValue(sheet, pt, transpose, EnumXLSExperimentDescriptors.ROIS.toString());
		pt.y++;
		return pt.y;
	}
	
	protected int getCageFromCapillaryName(String name) {
		if (!name .contains("line"))
			return -1;
	
		String num = name.substring(4, 5);
		int numFromName = Integer.parseInt(num);
		return numFromName;
	}
	
	protected String getSubName(Path path, int subnameIndex) {
		String name = "-";
		if (path.getNameCount() >= subnameIndex)
			name = path.getName(path.getNameCount() -subnameIndex).toString();
		return name;
	}
	
	protected String getShortenedName(SequenceCamData seq, int t) {
		return seq.getFileNameNoPath(t);
	}

	protected void xlsCreatePivotTable(XSSFWorkbook workBook, String workBookName, String fromWorkbook, DataConsolidateFunction function) {
		XSSFSheet pivotSheet = workBook.createSheet(workBookName);
        XSSFSheet sourceSheet = workBook.getSheet(fromWorkbook);

        int lastRowNum = sourceSheet.getLastRowNum();
        int lastColumnNum = sourceSheet.getRow(0).getLastCellNum();
        CellAddress lastcell = new CellAddress (lastRowNum, lastColumnNum-1);
        String address = "A1:"+lastcell.toString();
        AreaReference source = new AreaReference(address, SpreadsheetVersion.EXCEL2007);
        CellReference position = new CellReference(0, 0);
        XSSFPivotTable pivotTable = pivotSheet.createPivotTable(source, position, sourceSheet);

        boolean flag = false;	// ugly trick: switch mode when flag = true, ie when column "roi" has been found
        for (int i = 0; i< lastColumnNum; i++) {
        	XSSFCell cell = XLSUtils.getCell(sourceSheet, 0, i);
        	String text = cell.getStringCellValue();
        	if( !flag) {
        		flag = text.contains("roi");  // ugly trick here
        		if (text.contains(EnumXLSExperimentDescriptors.CAP.toString()))
        			pivotTable.addRowLabel(i);
        		if (text.contains(EnumXLSExperimentDescriptors.NFLIES.toString()))
        			pivotTable.addRowLabel(i);
        		continue;
        	}
        	pivotTable.addColumnLabel(function, i, text);
        }
	}

	protected void xlsCreatePivotTables(XSSFWorkbook workBook, String fromWorkbook) {
		xlsCreatePivotTable(workBook, "pivot_avg", fromWorkbook, DataConsolidateFunction.AVERAGE);
		xlsCreatePivotTable(workBook, "pivot_std", fromWorkbook, DataConsolidateFunction.STD_DEV);
		xlsCreatePivotTable(workBook, "pivot_n", fromWorkbook, DataConsolidateFunction.COUNT);
	}
	
	public int getColFromKymoFileName(String name) {
		if (!name .contains("line"))
			return -1;

		String num = name.substring(4, 5);
		int numFromName = Integer.parseInt(num);
		if( name.length() > 5) {
			String side = name.substring(5, 6);
			if (side != null) {
				if (side .equals("R")) {
					numFromName = numFromName* 2;
					numFromName += 1;
				}
				else if (side .equals("L"))
					numFromName = numFromName* 2;
			}
		}
		return numFromName;
	}
	
}
