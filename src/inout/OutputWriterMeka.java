package inout;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import weka.core.Instances;


public class OutputWriterMeka {
	
	public static HSSFWorkbook addHeaderToStats(HSSFWorkbook workbook ,String sheetName){		
		Sheet sheet = workbook.createSheet(sheetName);
		int rowNum = 0;
		Row row = sheet.createRow(rowNum);
		Cell cell = row.createCell(0);
		cell.setCellValue("BugID");
		cell = row.createCell(1);
		cell.setCellValue("Summary");
		cell = row.createCell(2);
		cell.setCellValue("Description");
		cell = row.createCell(3);
		cell.setCellValue("Group_Actual");
		cell = row.createCell(4);
		cell.setCellValue("Group_Predicted");
		cell = row.createCell(5);
		cell.setCellValue("Group_Confidence");
		cell = row.createCell(6);
		cell.setCellValue("Type_Actual");
		cell = row.createCell(7);
		cell.setCellValue("Type_Predicted");
		cell = row.createCell(8);
		cell.setCellValue("Type_Confidence");
		cell = row.createCell(9);
		cell.setCellValue("Feature_Request_Actual");
		cell = row.createCell(10);
		cell.setCellValue("Feature_Request_Predicted");	
		cell = row.createCell(11);
		cell.setCellValue("Feature_Request_Confidence");	
		return workbook;
	}
	
	
	public static HSSFWorkbook addSummaryDescription(HSSFWorkbook workbook,Instances instances,
			Map<String, Integer> columnIndex) {
		Sheet sheet = workbook.getSheetAt(0);
		int rowNum = 1;
		Cell cell;
		Row row;
		String summary,description,summaryDescription;
		for (int i = 0; i < instances.numInstances(); i++) {
			row = sheet.createRow(rowNum);
			
			//add bugID
			cell = row.createCell(columnIndex.get("BugID"));
			cell.setCellValue(instances.instance(i).stringValue(0));
	
			summaryDescription = instances.instance(i).stringValue(instances.numAttributes()-1);
			description = summaryDescription.substring(0,summaryDescription.indexOf("_summary_start_"));
			summary = summaryDescription.substring(summaryDescription.indexOf("_summary_start_") + new String("_summary_start_").length()
									    ,summaryDescription.indexOf("_summary_end_"));		
			
			//add summary
			cell = row.createCell(columnIndex.get("Summary"));
			cell.setCellValue(summary);
			
			//add description
			cell = row.createCell(columnIndex.get("Description"));
			cell.setCellValue(description);			
			rowNum++;
		}
		return workbook;	
	}
	
	
	public static HSSFWorkbook insertActual(HSSFWorkbook workbook,Instances instances,
				Map<String,Integer> classIndices,String scheme,Map<String, Integer> columnIndex) {
		
		Sheet sheet = workbook.getSheetAt(0);		
		String labels="";
		int START_INDEX = 1;
		Cell cell;
		int rowNum = 1;
		Row row;
		for (int i = 0; i < instances.numInstances(); i++) {
			row = sheet.getRow(rowNum);
			for (int j = START_INDEX ; j < classIndices.get(scheme) + START_INDEX; j++) {
				if(instances.instance(i).stringValue(j).equals("1")){
					if(labels.equals("")){
						labels = instances.attribute(j).name().substring(1);
					}
					else{
						labels = labels +","+ instances.attribute(j).name().substring(1);
					}					
				}
			}
			cell = row.createCell(columnIndex.get(scheme+"_Actual"));
			cell.setCellValue(labels);
			rowNum++;
			labels ="";
		}
		return workbook;
	}
	
	
	public static HSSFWorkbook addConfidence(Instances instances,HSSFWorkbook workbook,
			String scheme,Map<String, Integer> columnIndex) throws Exception {	
		
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(scheme+".txt"));	
		Sheet sheet = workbook.getSheetAt(0);
		int rowNum = 1;
		Row row;Cell cell;
				
		int START_INDEX = 3,END_INDEX = instances.numInstances() ;
		while(START_INDEX > 0){
			reader.readLine();
			START_INDEX--;
		}		
		String line = reader.readLine();		
		while(line != null && END_INDEX > 0){
			row = sheet.getRow(rowNum);
			cell = row.createCell(columnIndex.get(scheme.substring(scheme.lastIndexOf("/")+1)+"_Confidence"));
			cell.setCellValue(line.substring(line.lastIndexOf("[")));
			END_INDEX --;
			line=reader.readLine();
			rowNum++;
		}		
		reader.close();
		return workbook;
	}
	
	
	public static void saveWorkbook (HSSFWorkbook workbook,String algo) {
		try {
			String textExtractType;
			textExtractType =  algo+"/"+algo.substring(algo.lastIndexOf("/"))+".xls";
			FileOutputStream defectsTypeXLS = new FileOutputStream(textExtractType);
			workbook.write(defectsTypeXLS);
			defectsTypeXLS.flush();
			defectsTypeXLS.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	
	public static Map<String, Integer> getColumnIndices (HSSFWorkbook workbook) {
		Map<String, Integer> columnIndex = new HashMap<>();
		Sheet sheet = workbook.getSheetAt(0);
		Row row = sheet.getRow(0);
		for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
			columnIndex.put(row.getCell(i).toString(), i);
		}	
		return columnIndex;
	}
	
	
	public static HSSFWorkbook insertPredicted(HSSFWorkbook workbook,Instances instances,
			Map<String, Integer> classIndices,String scheme,Map<String, Integer> columnIndex ) {
		
		Sheet sheet = workbook.getSheetAt(0);
		Row row;
		String[] confidence;
		
		Map<String,Double> confidencePerLabel = new HashMap<>();
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {			
			 row = sheet.getRow(i);
			 confidence = row.getCell(columnIndex.get(scheme+"_Confidence")).toString().split(" ");
			 for (int j = 1; j < confidence.length-1 ; j++) { // "[" and "]" included in the output
				confidencePerLabel.put(instances.attribute(j).name().substring(1),
						Double.parseDouble(confidence[j]));			
			}			 			 
			 //write to xls
			 String predictions="";
			 for(Map.Entry<String, Double> iEntry:confidencePerLabel.entrySet()){		 
				 if(iEntry.getValue() >= getThreshold()){
					 predictions = predictions + " " + iEntry.getKey();					 
				 }
			 }			 
			 Cell cell = row.createCell(columnIndex.get(scheme+"_Predicted"));
			 cell.setCellValue(predictions);
		}		
		return workbook;
	}
	
	public static double getThreshold() {
		return 0.5;
	}
	
	public static void main(String[] args) throws Exception {		
		
		HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream("./output/BR/BR.xls"));
		
		//insertPredicted(workbook);
		
		
		/*HSSFWorkbook workbook = new HSSFWorkbook();
//		workbook = addHeaderToStats(workbook, "Sheet1", "DS");		
		Map<String, Integer> columnIndex = getColumnIndices(workbook);
		Map<String,Integer> classIndices = new HashMap<String, Integer>();
		classIndices.put("Type", 9);
		classIndices.put("Group", 4);
		//classIndices.put("featureRequest", 1);
		
		
		BufferedReader reader = new BufferedReader(
                new FileReader("./data/da.arff"));
		Instances instances = new Instances(reader);
		addSummaryDescription(workbook, instances,columnIndex);
		
		String[] labels = {"Type","Actual","Feature_Request"};
		for (int i = 0; i < labels.length; i++) {
			reader = new BufferedReader(
	                new FileReader("./data/"+labels[i]+".arff"));
			instances = new Instances(reader);
			insertActual(workbook, instances, classIndices, labels[i], columnIndex);
			addConfidence(instances, workbook,labels[i], columnIndex);
		}*/
	
	}

}
