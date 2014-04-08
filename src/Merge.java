import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Merge {

	private static String speech_data = "661-speech.arff"; // speech file
	private static String music_data = "661-music.arff"; // music file
	private static String output_data = "Merge_output.arff"; // structure of final output
	private static DataSource source;
	private static Instances data1;
	private static Instances data2;
	private static Instances output;
	
	public static void main(String[] args) throws Exception {

		// load final output file to store result
		source = new DataSource(output_data);
		output = source.getDataSet();
		
		// load speech testing dataset
		source = new DataSource(music_data);
		data1 = source.getDataSet();
		
		source = new DataSource(speech_data);
		data2 = source.getDataSet();

		
		//class of music : 706 class of music : 755
		// record real class of speech data into final output file
		for (int i = 0; i < data1.numInstances(); i++) {
			
			double[] vals = new double[output.numAttributes()];
			//System.out.println(output.numAttributes());
			
			for(int j=0;j<data1.numAttributes();j++){
					//System.out.println(j);
					vals[j] = data1.instance(i).value(j);
			}
			
			for(int j=0;j<data2.numAttributes();j++){
					//System.out.println(j+706);
					vals[j+706] = data2.instance(i).value(j);
			}
			
			output.add(new Instance(1.0, vals));
		}
		
		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(
				now.getTime());

		String filename = "mergeoutput_" + currentTimestamp.toString();
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		for (int i = 0; i < output.numInstances(); i++) {
			for (int j = 0; j < output.numAttributes(); j++) {
				String tmp = String.valueOf((int) output.instance(i).value(j));
				writer.write(tmp);
				if (j != output.numAttributes() - 1)
					writer.write(",");
			}
			writer.newLine();
		}
		writer.flush();
		writer.close();

		System.out.println("Finished");
		
		
		//for(int i=1;i<=49;i++)
		//	System.out.println("@ATTRIBUTE f000"+(i+706)+" REAL");
		
		
	}

}
