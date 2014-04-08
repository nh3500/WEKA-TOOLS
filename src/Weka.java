import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;

import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instance;
import weka.core.Instances;

import weka.classifiers.Classifier;


/* This program is designed for put two testing data sets to two different classifiers (for music and speech separately).
 * 
 * And it will generate the final with filename "finaloutput_timestamp" that contain following structure:
 * 
 * 0,1,0,1,1
 * 0,1,0,1,3
 * 0,1,0,1,1
 * 0,1,0,1,5
 * 0,1,0,1,2
 * 0,1,0,1,7
 * ...
 * 
 * First column : real class of the instance in speech testing data. 	(0 or 1)
 * Second column: real class of the instance in music testing data.		(0 or 1)
 * Third column : predicted class of the instance in speech testing data.(0 or 1)
 * Fourth column: predicted class of the instance in music testing data.(0 or 1)
 * Fifth column : Final class in the table given by teacher.(1-9)
 * 
 * 
 *                                     predicted class
 * ----------------------------------------------------------------
 *                          music-only   music&speech   nomusic
 * --------------------------------------------------------------
 *      |  music-only    |    class 1      class 2       class 3
 * true |  music&speech  |    class 4      class 5       class 6
 * class|  nomusic       |    class 7      class 8       class 9
 * 
 * It also show the income in the console.
 * 
 * 
 * To use this program, please set the paths to your testing data(.arff file) and classifier(.model file) 
 * 
 */




public class Weka {

	private static String speech_test_data = "661-speech.arff"; // speech
																// testing file
	private static String speech_model = "SMO_speech.model";// classifier for
															// speech
	private static String music_test_data = "661-music.arff"; // music testing
																// file
	private static String music_model = "SMO_music.model"; // classifier for
															// music
	private static String output_data = "output.arff"; // structure of final
														// output

	private static DataSource source;
	private static Instances data;
	private static Classifier smo;
	private static Instances output;
	private static double predictLabel;

	public static void main(String[] args) throws Exception {

		// load final output file to store result
		source = new DataSource(output_data);
		output = source.getDataSet();

		/*
		 * Classify speech data
		 */

		System.out.println("Start to load speech data");
		// load speech testing dataset
		source = new DataSource(speech_test_data);
		data = source.getDataSet();
		// set class label
		if (data.classIndex() == -1)
			data.setClassIndex(data.numAttributes() - 1);

		// record real class of speech data into final output file
		for (int i = 0; i < data.numInstances(); i++) {
			double[] vals = new double[output.numAttributes()];
			vals[0] = data.instance(i).classValue();
			vals[1] = 0;
			vals[2] = 0;
			vals[3] = 0;
			vals[4] = 0;
			output.add(new Instance(1.0, vals));
		}

		System.out.println("Start to classify speech data");

		// call model file to load classifier
		smo = (Classifier) weka.core.SerializationHelper.read(speech_model);

		// classify instances in testing dataset and store into output file
		for (int i = 0; i < data.numInstances(); i++) {
			predictLabel = smo.classifyInstance(data.instance(i));
			output.instance(i).setValue(2, predictLabel);
			// System.out.println(predictLabel);
		}

		/*
		 * Classify music data
		 */

		System.out.println("Start to load music data");
		// load speech testing dataset
		source = new DataSource(music_test_data);
		data = source.getDataSet();
		// set class label
		if (data.classIndex() == -1)
			data.setClassIndex(data.numAttributes() - 1);

		// record real class of music data into final output file
		for (int i = 0; i < data.numInstances(); i++)
			output.instance(i).setValue(1, data.instance(i).classValue());

		System.out.println("Start to classify music data");
		// call model file to load classifier
		smo = (Classifier) weka.core.SerializationHelper.read(music_model);

		// classify instances in testing dataset and store into output file
		for (int i = 0; i < data.numInstances(); i++) {
			predictLabel = smo.classifyInstance(data.instance(i));
			output.instance(i).setValue(3, predictLabel);
		}

		/*
		 * decide final class
		 */

		System.out.println("Start to decide final calss and caculate income");

		double income = 0;

		for (int i = 0; i < output.numInstances(); i++) {
			// real class : music only
			if (output.instance(i).value(0) == 0
					&& output.instance(i).value(1) == 1) {
				if (output.instance(i).value(2) == 0
						&& output.instance(i).value(3) == 1) {
					predictLabel = 1;
					income = income + 1;
				} else if (output.instance(i).value(2) == 1
						&& output.instance(i).value(3) == 1) {
					predictLabel = 2;
					income = income + 0.75;
				} else if (output.instance(i).value(3) == 0)
					predictLabel = 3;
			}
			// real calss : speech & music
			else if (output.instance(i).value(0) == 1
					&& output.instance(i).value(1) == 1) {
				if (output.instance(i).value(2) == 0
						&& output.instance(i).value(3) == 1) {
					predictLabel = 4;
					income = income - 0.5;
				} else if (output.instance(i).value(2) == 1
						&& output.instance(i).value(3) == 1) {
					predictLabel = 5;
					income = income + 0.75;
				} else if (output.instance(i).value(3) == 0)
					predictLabel = 6;
			}
			// real class : no music
			else if (output.instance(i).value(1) == 0) {
				if (output.instance(i).value(2) == 0
						&& output.instance(i).value(3) == 1) {
					predictLabel = 7;
					income = income - 3;
				} else if (output.instance(i).value(2) == 1
						&& output.instance(i).value(3) == 1) {
					predictLabel = 8;
					income = income - 1;
				} else if (output.instance(i).value(3) == 0)
					predictLabel = 9;
			}

			output.instance(i).setValue(4, predictLabel);
		}

		System.out.println("income : " + income);

		/*
		 * write result to file
		 */

		System.out.println("Start to write result to file");
		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(
				now.getTime());


		String filename = "finaloutput_" + currentTimestamp.toString();
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
		// System.out.println(data.instance(10).value(10));

	}
}
