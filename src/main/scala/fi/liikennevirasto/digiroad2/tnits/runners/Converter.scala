package fi.liikennevirasto.digiroad2.tnits.runners

import java.net.URLEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util

import dispatch.Http
import fi.liikennevirasto.digiroad2.tnits.aineistot.RemoteDatasets
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.oth.OTHClient
import fi.liikennevirasto.digiroad2.tnits.rosatte.{RosatteConverter, features}
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.{AuthSchemes, RequestConfig}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.ConnectionConfig
import org.apache.http.impl.client.{BasicCredentialsProvider, ProxyAuthenticationStrategy}
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.scalatra.util.RicherString
import sun.net.www.protocol.http.BasicAuthentication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

case class AssetType(apiEndPoint: String, featureType: String, valueType: String, unit: String)

object Converter {
  def main(args: Array[String]) {
    try {
      convert()
    } finally {
      Http.shutdown()
    }
  }

  case class OTHException(cause: Throwable) extends RuntimeException(cause)

  def convert(): Unit = {
    val start = Instant.now.minus(1, ChronoUnit.HOURS)
    val end = Instant.now.minus(1, ChronoUnit.MINUTES)

    val assetTypes = Seq(
      AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph"),
      AssetType("axle_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg"),
      AssetType("length_limits", "RestrictionForVehicles", "MaximumLength", "kg"),
      AssetType("width_limits", "RestrictionForVehicles", "MaximumWidth", "kg"),
      AssetType("height_limits", "RestrictionForVehicles", "MaximumHeight", "kg"),
      AssetType("total_weight_limits", "RestrictionForVehicles", "MaximumLadenWeight", "kg"))

    println(fetchAllChangesWithApacheHttpAsyncClient(start, end, assetTypes))

    val assets = fetchAllChanges(start, end, assetTypes)

    val allRosatteFeatures =
      assetTypes.zip(assets).flatMap { case (assetType, changes) =>
        RosatteConverter.convert(changes, assetType.featureType, assetType.valueType, assetType.unit)
      }

    val dataSet = RosatteConverter.generateDataSet(allRosatteFeatures, start, end)
    println(dataSet.updates)
    val filename = s"${URLEncoder.encode(dataSet.id, "UTF-8")}.xml"
    RemoteDatasets.put(filename, dataSet.updates)
  }

  def fetchAllChanges(start: Instant, end: Instant, assetTypes: Seq[AssetType]): Seq[Seq[features.Asset]] = {
    try {
      val responses = Future.sequence(assetTypes.map(asset => OTHClient.fetchChanges(asset.apiEndPoint, start, end)))
      Await.result(responses, 30.seconds)
    } catch {
      case err: Throwable =>
        throw OTHException(err)
    }
  }

  def fetchAllChangesWithApacheHttpAsyncClient(start: Instant, end: Instant, assetTypes: Seq[AssetType]): Seq[String] = {
    val credentials = new BasicCredentialsProvider
    if (config.optionalProxy.isDefined) {
      val proxy = config.optionalProxy.get
      credentials.setCredentials(
        new AuthScope(proxy.host, proxy.port),
        new UsernamePasswordCredentials(proxy.username, proxy.password))
    }
    credentials.setCredentials(
      new AuthScope("testioag.liikennevirasto.fi", 443), // TODO: Use config
      new UsernamePasswordCredentials(config.logins.oth.username, config.logins.oth.password))

    val client = HttpAsyncClients.custom()
      .setDefaultCredentialsProvider(credentials)
      .build()

    try {
      client.start()

      val responses =
        Future
          .sequence(Seq(assetTypes.head).map { asset =>
            OTHClient.fetchChangesWithApacheHttpAsyncClient(client, asset.apiEndPoint, start, end) })
          .map { responses =>
            responses.map { response =>
              Source.fromInputStream(response.getEntity.getContent).mkString
            }
          }
      Await.result(responses, 30.seconds)
    } catch {
      case err: Throwable =>
        throw OTHException(err)
    } finally {
      client.close()
    }
  }
}
