
name             := "AnchorExtract"
version          := "1.0"
scalaVersion     := "2.10.5"
test in assembly := {}
mainClass in Compile := Some("org.searsia.AnchorExtract")

packAutoSettings

val sparkV  = "1.6.1"
val hadoopV = "2.7.1"
val jwatV   = "1.0.2"

resolvers += "nl.surfsara" at "http://searsia.org/repository/"

libraryDependencies ++= Seq(
  "org.apache.spark"  %% "spark-core"    % sparkV  % "provided",
  "org.apache.hadoop" %  "hadoop-client" % hadoopV % "provided",
  "org.jwat"          %  "jwat-warc"     % jwatV,
  "org.jsoup"         %  "jsoup"         % "1.10.2",
  "nl.surfsara"       %  "warcutils"     % "1.4"
)

// Not needed for AnchorExtract.scala
libraryDependencies ++= Seq(
  "org.glassfish.jersey.containers" % "jersey-container-grizzly2-http" % "2.23" % "provided",
  "commons-cli" % "commons-cli" % "1.3.1"    % "provided",
  "junit"       % "junit"       % "4.12"     % "provided",
  "org.json"    % "json"        % "20160212" % "provided"
)
