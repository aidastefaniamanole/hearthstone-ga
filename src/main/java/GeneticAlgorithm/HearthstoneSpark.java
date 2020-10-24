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
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SQLContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;

import static net.demilich.metastone.game.cards.CardCatalogue.CARDS_FOLDER;

public class HearthstoneSpark {
	private static final Logger logger = LoggerFactory.getLogger(HearthstoneSpark.class);
	private static final Integer noPopulations = 2;
	private static final Integer populationSize = 20;

	public static String catalog = "{" +
			"\"table\":{\"namespace\":\"default\", \"name\":\"cards\", \"tableCoder\":\"PrimitiveType\"}," +
			"\"rowkey\":\"key\"," +
			"\"columns\":{" +
			"\"rowkey\":{\"cf\":\"rowkey\", \"col\":\"key\", \"type\":\"string\"}," +
			"\"heroClass\":{\"cf\":\"info\", \"col\":\"heroClass\", \"type\":\"string\"}," +
			"\"baseManaCost\":{\"cf\":\"info\", \"col\":\"baseManaCost\", \"type\":\"long\"}," +
			"\"cardType\":{\"cf\":\"info\", \"col\":\"cardType\", \"type\":\"string\"}," +
			"\"name\":{\"cf\":\"info\", \"col\":\"name\", \"type\":\"string\"}," +
			"\"rarity\":{\"cf\":\"info\", \"col\":\"rarity\", \"type\":\"string\"}" +
			"}}";

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
				Put insHBase = new Put(Bytes.toBytes(card.getRowkey()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("heroClass"), Bytes.toBytes(card.getHeroClass()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("baseManaCost"), Bytes.toBytes(card.getBaseManaCost()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("cardType"), Bytes.toBytes(card.getCardType()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(card.getName()));
				insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("rarity"), Bytes.toBytes(card.getRarity()));
				tableName.put(insHBase);
			}
		} catch (IOException e) {
			logger.error("Add cards to DB fail", e);
		}
	}

	public static GeneticDeck generateDeck(String heroClass, List<GeneticCard> cards) {
		Random rand = new Random();
		GeneticDeck deck = new GeneticDeck();
		deck.setHeroClass(heroClass);

		for (int i = 0; i < GeneticDeck.deckSize; i++) {
			Integer index = rand.nextInt(cards.size());
			GeneticCard card = cards.get(index);
			while (!deck.canAddCardToDeck(card)) {
				index = rand.nextInt(cards.size());
				card = cards.get(index);
			}
			deck.cards.add(card);
		}

		return deck;
	}

	public static ArrayList<Population> initPopulations(SQLContext sqlContext, String heroClass) {
		Map<String, String> optionsMap = new HashMap<>();

		optionsMap.put("catalog", catalog);
		Dataset dataset = sqlContext.read().options(optionsMap)
				.format("org.apache.spark.sql.execution.datasources.hbase").load();

		Dataset datasetFiltered = dataset.filter(dataset.col("heroClass")
				.equalTo(heroClass).or(dataset.col("heroClass").equalTo("ANY")));

		// datasetFiltered.show(10);

		Dataset<GeneticCard> ds = datasetFiltered.as(Encoders.bean(GeneticCard.class));
		List<GeneticCard> cardList = ds.collectAsList();

		// generate noPopulations * populationSize randomly build decks with the filtered cards
		ArrayList<Population> populations = new ArrayList<>();
		for (int i = 0; i < noPopulations; i++) {
			Population population = new Population(populationSize);
			for (int j = 0; j < populationSize; j++) {
				population.addOrganism(generateDeck(heroClass, cardList));
			}
			populations.add(population);
		}

		return populations;
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

		String heroClass = "";
		// read client config
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(args[0]));
			heroClass = (String) jsonObject.get("heroClass");
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

		logger.info("Generate decks for hero", heroClass);
		ArrayList<Population> populations = initPopulations(sqlContext, heroClass);

		// TODO MapReduce
	}
}
