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

	public static ArrayList<GeneticCard> readCards() {
		ArrayList<GeneticCard> cards = new ArrayList<>();

		try {
			Collection<ResourceInputStream> inputStreams = ResourceLoader
					.loadJsonInputStreams(CARDS_FOLDER, false);
			JSONParser jsonParser = new JSONParser();
			for (ResourceInputStream resource : inputStreams) {
				JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(resource.inputStream));

				if (!((boolean) jsonObject.get("collectible")) || jsonObject.get("type").equals("HERO")
						|| jsonObject.get("type").equals("HERO_POWER")) {
					continue;
				}

				cards.add(new GeneticCard(jsonObject, resource.fileName.split("\\.")[0]));
			}
		} catch (URISyntaxException | IOException | ParseException e) {
			logger.error("Read cards fail", e);
		}

		return cards;
	}

	public static void addCardsToHbase(Configuration conf) {
		ArrayList<GeneticCard> cards = readCards();

		// load cards in hbase so the filtering would be easier
		try {
			// establish a connection
			Connection connection = ConnectionFactory.createConnection(conf);
			// Table on which different commands have to be run.
			Table tableName = connection.getTable(TableName.valueOf("cards"));
			for (GeneticCard card : cards) {
				Put insHBase = new Put(Bytes.toBytes(card.getId()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("heroClass"), Bytes.toBytes(card.getHeroClass()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("baseManaCost"), Bytes.toBytes(card.getBaseManaCost().toString()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("cardType"), Bytes.toBytes(card.getCardType()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(card.getName()));
				tableName.put(insHBase);
			}
		} catch (IOException e) {
			logger.error("Add cards to DB fail", e);
		}
	}

	public static String catalog = "{" +
			"\"table\":{\"namespace\":\"default\", \"name\":\"cards\", \"tableCoder\":\"PrimitiveType\"}," +
			"\"rowkey\":\"key\"," +
			"\"columns\":{" +
			"\"rowkey\":{\"cf\":\"rowkey\", \"col\":\"key\", \"type\":\"string\"}," +
			"\"heroClass\":{\"cf\":\"info\", \"col\":\"heroClass\", \"type\":\"string\"}," +
			"\"baseManaCost\":{\"cf\":\"info\", \"col\":\"baseManaCost\", \"type\":\"string\"}," +
			"\"cardType\":{\"cf\":\"info\", \"col\":\"cardType\", \"type\":\"string\"}," +
			"\"name\":{\"cf\":\"info\", \"col\":\"name\", \"type\":\"string\"}" +
			"}}";

	public static void initPopulation(SQLContext sqlContext ) {
		Map<String, String> optionsMap = new HashMap<>();

//        String htc = HBaseTableCatalog.tableCatalog();
//
        optionsMap.put("catalog", catalog);
		Dataset dataset = sqlContext.read().options(optionsMap)
				.format("org.apache.spark.sql.execution.datasources.hbase").load();

		dataset.show();
	}

	public static void main(String[] args) {
		System.setProperty("hadoop.home.dir", "/home/nightcro/hadoop-3.1.2");
		// simple spark configuration where everything runs in process using 1 worker thread
		SparkConf sparkConf = new SparkConf().setAppName("Hearthstone-GA").setMaster("local[1]");
		SparkContext sc = new SparkContext(sparkConf);
		// default HBase configuration for connecting to localhost on default port
		Configuration conf = HBaseConfiguration.create();
		// the entry point interface for the Spark SQL processing module
		SQLContext sqlContext = new SQLContext(sc);

		addCardsToHbase(conf);
		initPopulation(sqlContext);
	}
}
