val scalatraVersion = "2.4.0"

lazy val root =
  project
    .in(file("."))
    .settings(
      name := "Digiroad2 TN-ITS",
      organization := "fi.liikennevirasto.digiroad2",
      version := "0.1.0-SNAPSHOT",
      mainClass in Compile := Some("JettyLauncher"),
      scalaVersion := "2.11.8",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % scalatraVersion,
        "org.scalatra" %% "scalatra-json" % scalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % scalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % scalatraVersion % "test",
        "org.json4s" %% "json4s-jackson" % "3.3.0",
        "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.2.15.v20160210" % "compile;container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
        "org.jsoup" % "jsoup" % "1.8.3",
        // Needed by map-1.4.2.jar
        "org.apache.logging.log4j" % "log4j-1.2-api" % "2.5" % "compile",
        "org.apache.logging.log4j" % "log4j-api" % "2.5" % "compile",
        "org.apache.logging.log4j" % "log4j-core" % "2.5" % "compile"

      )
    )
    .enablePlugins(JettyPlugin, JavaAppPackaging)
