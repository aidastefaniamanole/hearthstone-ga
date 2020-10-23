package GeneticAlgorithm;

import net.demilich.metastone.utils.ResourceInputStream;
import net.demilich.metastone.utils.ResourceLoader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import static net.demilich.metastone.game.cards.CardCatalogue.CARDS_FOLDER;

public class HearthstoneSpark {

    public static ArrayList<Card> readCards() {
        ArrayList<Card> cards = new ArrayList<>();

        try {
            Collection<ResourceInputStream> inputStreams = ResourceLoader
                    .loadJsonInputStreams(CARDS_FOLDER, false);
            JSONParser jsonParser = new JSONParser();
            for (ResourceInputStream resource : inputStreams) {
                JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(resource.inputStream));
                if ((boolean) jsonObject.get("collectible")) {
                    cards.add(new Card(jsonObject));
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cards;
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
        ArrayList<Card> cards = readCards();





    }
}
