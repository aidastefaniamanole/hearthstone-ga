package GeneticAlgorithm;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class HearthstoneSpark {

    public static void readCards(String dirPath) {
        File dir = new File(dirPath);
        File[] firstLevelFiles = dir.listFiles();
        if (firstLevelFiles != null && firstLevelFiles.length > 0) {
            for (File aFile : firstLevelFiles) {
                if (aFile.isDirectory()) {
                    readCards(aFile.getAbsolutePath());
                } else {
                    // read the file
                    JSONParser jsonParser = new JSONParser();
                    try {
                        FileReader reader = new FileReader(aFile);
                        Object o = jsonParser.parse(reader);
                        JSONObject jsonObject = (JSONObject)o;
                        if ((boolean) jsonObject.get("collectible")) {
                            // save the card in hbase

                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        // default HBase configuration for connecting to localhost on default port
        Configuration conf =  HBaseConfiguration.create();
        // simple spark configuration where everything runs in process using 1 worker thread
        SparkConf sparkConf = new SparkConf().setAppName("Hearthstone-GA").setMaster("local[1]");
        // the Java Spark context provides Java-friendly interface for working with Spark RDDs
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        // The HBase context for Spark offers general purpose method for bulk read/write
        // hBaseContext = new

        // load cards in hbase so the filtering would be easier
        // readCards("resources/cards/");





    }
}
