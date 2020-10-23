package GeneticAlgorithm;

import console.MetaStoneSim;
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
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SQLContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static net.demilich.metastone.game.cards.CardCatalogue.CARDS_FOLDER;

public class HearthstoneSpark {
	private static final Logger logger = LoggerFactory.getLogger(HearthstoneSpark.class);

	public static ArrayList<Card> readCards() {
		ArrayList<Card> cards = new ArrayList<>();

		try {
			Collection<ResourceInputStream> inputStreams = ResourceLoader
					.loadJsonInputStreams(CARDS_FOLDER, false);
			JSONParser jsonParser = new JSONParser();
			for (ResourceInputStream resource : inputStreams) {
				JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(resource.inputStream));
				if ((boolean) jsonObject.get("collectible")) {
					cards.add(new Card(jsonObject, resource.fileName.split(".")[0]));
				}
			}
		} catch (URISyntaxException | IOException | ParseException e) {
			logger.error("Read cards fail", e);
		}

		return cards;
	}

	public static void addCardsToHbase(Configuration conf) {
		ArrayList<Card> cards = readCards();

		// load cards in hbase so the filtering would be easier
		try {
			// establish a connection
			Connection connection = ConnectionFactory.createConnection(conf);
			// Table on which different commands have to be run.
			Table tableName = connection.getTable(TableName.valueOf("cards"));
			for (Card card : cards) {
				Put insHBase = new Put(Bytes.toBytes(card.name));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("heroClass"), Bytes.toBytes(card.heroClass));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("baseManaCost"), Bytes.toBytes(card.baseManaCost.toString()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("cardType"), Bytes.toBytes(card.cardType));
                insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("fileName"), Bytes.toBytes(card.fileName));
				tableName.put(insHBase);
			}
		} catch (IOException e) {
			logger.error("Add cards to DB fail", e);
		}
	}

    public static String catalog = "{\n" +
            "\t\"table\":{\"namespace\":\"default\", \"name\":\"cards\", \"tableCoder\":\"PrimitiveType\"},\n" +
            "    \"rowkey\":\"key\",\n" +
            "    \"columns\":{\n" +
            "\t    \"rowkey\":{\"cf\":\"rowkey\", \"col\":\"key\", \"type\":\"string\"},\n" +
            "\t    \"heroClass\":{\"cf\":\"info\", \"col\":\"heroClass\", \"type\":\"string\"}\n" +
            "\t    \"baseManaCost\":{\"cf\":\"info\", \"col\":\"baseManaCost\", \"type\":\"string\"}\n" +
            "\t    \"cardType\":{\"cf\":\"info\", \"col\":\"cardType\", \"type\":\"string\"}\n" +
            "    }\n" +
            "}";

	public static void initPopulation(SQLContext sqlContext ) {
        Map<String, String> optionsMap = new HashMap<>();

//        String htc = HBaseTableCatalog.tableCatalog();
//
//        optionsMap.put(htc, catalog);
        Dataset dataset = sqlContext.read().options(optionsMap)
                .format("org.apache.spark.sql.execution.datasources.hbase").load();
    }

	public static void main(String[] args) {
		// simple spark configuration where everything runs in process using 1 worker thread
		SparkConf sparkConf = new SparkConf().setAppName("Hearthstone-GA").setMaster("local[1]");
		SparkContext sc = new SparkContext(sparkConf);
		// default HBase configuration for connecting to localhost on default port
		Configuration conf = HBaseConfiguration.create();
		// the entry point interface for the Spark SQL processing module
		SQLContext sqlContext = new SQLContext(sc);

		addCardsToHbase(conf);
	}
}
