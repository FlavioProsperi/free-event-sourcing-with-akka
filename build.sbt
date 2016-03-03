name := "free-event-sourcing-with-akka"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")

resolvers += "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"

libraryDependencies ++= {
  val scalazVersion = "7.2.0"
  val akkaVersion = "2.4.2"
  Seq(
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.2.7",
    "org.scalatest" %% "scalatest" % "3.0.0-M15" % "test")
}
