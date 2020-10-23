package GeneticAlgorithm;

import net.demilich.metastone.utils.ResourceInputStream;
import net.demilich.metastone.utils.ResourceLoader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
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
        // simple spark configuration where everything runs in process using 1 worker thread
        SparkConf sparkConf = new SparkConf().setAppName("Hearthstone-GA").setMaster("local[1]");
        SparkContext sc = new SparkContext(sparkConf);
        // JavaSparkContext sc = new JavaSparkContext(sparkConf);
        // default HBase configuration for connecting to localhost on default port
        Configuration conf =  HBaseConfiguration.create();
        // the entry point interface for the Spark SQL processing module
        SQLContext sqlContext = new SQLContext(sc);

        ArrayList<Card> cards = readCards();

        // load cards in hbase so the filtering would be easier
        try {
            // establish a connection
            Connection connection = ConnectionFactory.createConnection(conf);
            // Table on which different commands have to be run.
            Table tableName = connection.getTable(TableName.valueOf("cards"));
            for(Card card : cards) {
                Put insHBase = new Put(Bytes.toBytes(card.name));
                insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("heroClass"), Bytes.toBytes(card.heroClass));
                insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("baseManaCost"), Bytes.toBytes(card.baseManaCost.toString()));
                insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("cardType"), Bytes.toBytes(card.cardType));
                tableName.put(insHBase);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
