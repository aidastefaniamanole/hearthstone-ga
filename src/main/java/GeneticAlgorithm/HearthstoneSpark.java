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
import org.apache.hadoop.hbase.util.Pair;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SQLContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;

import static net.demilich.metastone.game.cards.CardCatalogue.CARDS_FOLDER;

public class HearthstoneSpark {
	private static final Logger logger = LoggerFactory.getLogger(HearthstoneSpark.class);
	private static final Integer noPopulations = 2;
	private static final Integer populationSize = 20; //20
	private static final Integer noGenerations = 10; //10

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
		GeneticDeck deck = new GeneticDeck(heroClass);

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
		SparkConf sparkConf = new SparkConf().setAppName("Hearthstone-GA").setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
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

		logger.info("Generate decks for hero {}", heroClass);
		List<Population> populations = initPopulations(sqlContext, heroClass);
		logger.info("Initial population complete");



		populations.forEach(x -> {
			x.getMembers().forEach(y -> {
				System.out.println("Win rate " + y.getFitness());
				System.out.println("Cards\n" + y.getCards().toString());
			});
		});

		logger.info("Commence parallelization");
		// run the GA in parallel
		JavaRDD<Population> populationsRDD = sc.parallelize(populations);
		populationsRDD = populationsRDD.map(x -> {
			Evaluator.calculateFitness(x.getMembers());
			Population var = x;
			for (int i = 0; i < noGenerations; i++) {
				logger.info("Evolve generation {}", i);
				var = var.evolve();
			}
			return var;
		});

		List<List<Pair<Double, GeneticDeck>>> lists = populationsRDD.map(x -> {
			List<Pair<Double, GeneticDeck>> pairs = new ArrayList<>();
			for (GeneticDeck deck : x.getMembers()) {
				pairs.add(new Pair<>(deck.getFitness(), deck));
			}

			return pairs;
		}).collect();

		// create a list of tuples (fitness, deck) and distribute it
		List<Pair<Double, GeneticDeck>> tuplesList = new ArrayList<>();
		lists.forEach(tuplesList::addAll);
		tuplesList.sort(Comparator.comparing(Pair::getFirst));
		Collections.reverse(tuplesList);
		logger.info("Generate parallelize pairs");

		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Result:");
		tuplesList.stream().limit(10).forEach(x -> {
			System.out.println("Win rate " + x.getFirst());
			System.out.println("Cards\n" + x.getSecond().toString());
		});
	}
}


