package core;
import inout.OutputWriterMeka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.RAkEL;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class multiLabelClassification {
	public static void main(String[] args) throws Exception {
		
		Map<String,Integer> classIndices = new HashMap<String, Integer>();
		classIndices.put("Type", 9);
		classIndices.put("Group", 4);
		/*
		classIndices.put("Feature_Request", 1);
		*/
	
		BufferedReader reader;
		Instances instances,instancesFiltered;
		
		String algo = null;
		String[] labels = {"Type","Group"};//,"Feature_Request"};
		
		for (int i = 0; i < labels.length; i++) {
			reader = new BufferedReader(new FileReader("./data/"+labels[i]+".arff"));
			instances = new Instances(reader);
			Instances temp = new Instances(instances);			
			temp.deleteAttributeAt(0);
			instancesFiltered = stringToWord(temp);
						
			instancesFiltered.setClassIndex(classIndices.get(labels[i]));
			
			//for every algo create dir
			
			algo = "./output/BR";	
			File theDir = new File(algo);
			theDir.mkdir();
			
			BR multilabel = new BR();
			multilabel.setClassifier(new J48());		
			multilabel.buildClassifier(instancesFiltered); //hadnt changed this ..fool
			
					
			//System.out.println(Evaluation.cvModel(multilabel, instancesFiltered, 10, "0.5","8"));// threshold and verbosity respectively
			
			
			PrintWriter writer = new PrintWriter(algo+"/"+labels[i]+".txt", "UTF-8");
			writer.println(Evaluation.cvModel(multilabel, instancesFiltered, 10, "0.5","8"));//hadmt changed this ..fool...PCut1 
			writer.close();
		}
		outputXls(classIndices,labels,algo);
	}
		
	public static void outputXls(Map<String,Integer> classIndices,String[] labels
			,String algo) throws Exception {
	
		HSSFWorkbook workbook = new HSSFWorkbook();
		workbook = OutputWriterMeka.addHeaderToStats(workbook, "Sheet1");		
		Map<String, Integer> columnIndex = OutputWriterMeka.getColumnIndices(workbook);
		
		BufferedReader reader = new BufferedReader(
                new FileReader("./data/Type.arff")); /////////////////// 
		Instances instances = new Instances(reader);
		workbook = OutputWriterMeka.addSummaryDescription(workbook, instances,columnIndex);
		
		for (int i = 0; i < labels.length; i++) {
			reader = new BufferedReader(
	                new FileReader("./data/"+labels[i]+".arff"));
			instances = new Instances(reader);
			workbook = OutputWriterMeka.insertActual(workbook, instances, classIndices, labels[i], columnIndex);
			workbook = OutputWriterMeka.addConfidence(instances, workbook,algo+"/"+labels[i], columnIndex);
			workbook = OutputWriterMeka.insertPredicted(workbook, instances, classIndices, labels[i], columnIndex);
		}		
		OutputWriterMeka.saveWorkbook(workbook, algo);	
	}
	
	
	public static void saveARFF(Instances instancesFiltered,String name) throws Exception{
	 	 ArffSaver saver = new ArffSaver();
		 saver.setInstances(instancesFiltered);
		 saver.setFile(new File("../mekaDemo/data/"+name+".arff")); // save in mekaDemo package
		 saver.writeBatch();
	}		
	
	public static Instances stringToWord(Instances instances) throws Exception{
	     NGramTokenizer tokenizer = new NGramTokenizer(); 
		 tokenizer.setDelimiters("\\W");
		 StringToWordVector filter = new StringToWordVector();
		 filter.setInputFormat(instances);
		 SnowballStemmer stemmer = new SnowballStemmer();
		 filter.setStemmer(stemmer);
		 filter.setTokenizer(tokenizer);
		 filter.setLowerCaseTokens(true);
		 filter.setDoNotOperateOnPerClassBasis(true); 
		 return Filter.useFilter(instances, filter);
	}	
}
