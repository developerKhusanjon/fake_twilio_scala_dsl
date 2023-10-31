ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "fake_twilio",
      libraryDependencies ++= {
      val akkaVersion = "2.8.0"
      val akkaHttpVersion = "10.5.0"
      val slickVersion = "3.4.1"
      val flywayVersion = "9.16.0"
      Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,

        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
        "com.typesafe.akka" %% "akka-stream-testkit" % "2.8.0" % Test,
        "org.scalatest" % "scalatest_2.12" % "3.2.15" % Test,

        "com.typesafe.slick" %% "slick" % slickVersion,
        "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,

        "org.flywaydb" % "flyway-core" % flywayVersion
      )
    }
  )
