import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.earldouglas.xwp.JettyPlugin
import com.earldouglas.xwp.JettyPlugin.autoImport._
import com.earldouglas.xwp.ContainerPlugin.autoImport._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import com.typesafe.sbt.packager.archetypes.{JavaAppPackaging, JavaServerAppPackaging}

object Digiroad2TnitsBuild extends Build {
  val Organization = "fi.liikennevirasto.digiroad2"
  val Name = "Digiroad2 TN-ITS"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.7"
  val ScalatraVersion = "2.4.0"

  lazy val project = Project (
    "digiroad2-tn-its",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      mainClass in Compile := Some("JettyLauncher"),
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.json4s" %% "json4s-jackson" % "3.3.0",
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.2.15.v20160210" % "compile;container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
        "org.jsoup" % "jsoup" % "1.8.3",
        // Needed by map-1.4.2.jar
        "org.apache.logging.log4j" % "log4j-1.2-api" % "2.5" % "compile",
        "org.apache.logging.log4j" % "log4j-api" % "2.5" % "compile",
        "org.apache.logging.log4j" % "log4j-core" % "2.5" % "compile"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      },
      containerPort in Jetty := 8090
    )
  ).enablePlugins(JettyPlugin, JavaAppPackaging)
}
