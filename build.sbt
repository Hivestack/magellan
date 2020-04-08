name := "magellan"

version := "2.0.0.0"

organization := "harsha2010"

scalaVersion := "2.12.11"

crossScalaVersions := Seq("2.12.11")

sparkVersion := "2.4.4"

// As of Scala 2.12.* this option is broken when combined with implicits. Disabled for now
//scalacOptions += "-optimize"

val testSparkVersion = settingKey[String]("The version of Spark to test against.")

testSparkVersion := sys.props.get("spark.testVersion").getOrElse(sparkVersion.value)

val testHadoopVersion = settingKey[String]("The version of Hadoop to test against.")

testHadoopVersion := sys.props.getOrElse("hadoop.testVersion", "3.2.1")

sparkComponents := Seq("core", "sql")

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.10.2"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.2"

dependencyOverrides +=  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.2"

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.6",
  "com.google.guava" % "guava" % "28.2-jre",
  "org.slf4j" % "slf4j-api" % "1.7.30" % "provided",
  "com.lihaoyi" %% "fastparse" % "2.2.4" % "provided",
  "org.scalatest" %% "scalatest" % "3.1.1" % "test",
  "com.vividsolutions" % "jts" % "1.13" % "test",
  "com.esri.geometry" % "esri-geometry-api" % "1.2.1"
)

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-client" % testHadoopVersion.value % "test",
  "org.apache.spark" %% "spark-core" % testSparkVersion.value % "test" exclude("org.apache.hadoop", "hadoop-client"),
  "org.apache.spark" %% "spark-sql" % testSparkVersion.value % "test" exclude("org.apache.hadoop", "hadoop-client")
)

// This is necessary because of how we explicitly specify Spark dependencies
// for tests rather than using the sbt-spark-package plugin to provide them.
spIgnoreProvided := true

publishMavenStyle := true

spAppendScalaVersion := true

spIncludeMaven := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/harsha2010/magellan</url>
  <licenses>
    <license>
      <name>Apache License, Verision 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:harsha2010/magellan.git</url>
    <connection>scm:git:git@github.com:harsha2010/magellan.git</connection>
  </scm>
  <developers>
    <developer>
      <id>harsha2010</id>
      <name>Ram Sriharsha</name>
      <url>www.linkedin.com/in/harsha340</url>
    </developer>
  </developers>)

spName := "harsha2010/magellan"

parallelExecution in Test := false

licenses += "Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0")

