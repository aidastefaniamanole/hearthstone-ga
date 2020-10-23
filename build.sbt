name := "spark_hearthstone"

version := "1.0"

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

resolvers += "com.hortonworks" at "https://repo.hortonworks.com/content/repositories/releases/"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % "2.2.1",
  "org.apache.spark" % "spark-sql_2.11" % "2.2.1",
  "org.apache.hbase" % "hbase-client" % "1.2.1",
  "org.apache.hbase" % "hbase-common" % "1.2.1",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "com.google.code.gson" % "gson" % "2.8.6",
  "org.jsoup" % "jsoup" % "1.10.2",
  "commons-cli" % "commons-cli" % "1.4",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "com.hortonworks" % "shc-core" % "1.1.1-2.1-s_2.11" exclude("slf4j-log4j12", "log4j")
)

