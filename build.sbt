val scalatraVersion = "2.4.0"
val geoToolsVersion = "14.3"

lazy val root =
  project
    .in(file("."))
    .settings(
      name := "Digiroad2 TN-ITS",
      organization := "fi.liikennevirasto.digiroad2",
      version := "0.1.0-SNAPSHOT",
      mainClass in Compile := Some("JettyLauncher"),
      scalaVersion := "2.11.8",
      resolvers ++= Seq(
        Classpaths.typesafeReleases,
        "opengeo" at "http://repo.opengeo.org/",
        "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/",
        "GeoToolKit Repository" at "http://maven.geotoolkit.org"),
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % scalatraVersion,
        "org.scalatra" %% "scalatra-json" % scalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % scalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % scalatraVersion % "test",
        "org.json4s" %% "json4s-jackson" % "3.3.0",
        "ch.qos.logback" % "logback-classic" % "1.1.7" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.3.8.v20160314" % "compile;container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
        "org.jsoup" % "jsoup" % "1.8.3",
        "commons-net" % "commons-net" % "3.4",
        "org.geotools" % "gt-referencing" % geoToolsVersion,
        "org.geotools" % "gt-epsg-wkt" % geoToolsVersion,
        "javax.media" % "jai_core" % "1.1.3",
        // Needed by map-1.4.2.jar
        "org.apache.logging.log4j" % "log4j-1.2-api" % "2.5" % "compile",
        "org.apache.logging.log4j" % "log4j-api" % "2.5" % "compile",
        "org.apache.logging.log4j" % "log4j-core" % "2.5" % "compile"
      ),
      containerPort in Jetty := 8090
    )
    .enablePlugins(JettyPlugin, JavaAppPackaging)
