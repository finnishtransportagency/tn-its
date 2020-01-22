package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.io.{BufferedOutputStream, OutputStream, OutputStreamWriter}

import fi.liikennevirasto.digiroad2.tnits.geojson.{Feature, FeaturePoint}
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.{IncomingMassTransitStopProperties}
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType

/** Generates Vallu XML to be sended. */
object BusStopValluConverter {

  /** Converts list of Bus stops to Vallu XML and writes it to the provided stream. */
  def convertDataSet(featureMembers: Seq[(AssetType, Seq[Feature[AssetProperties]])], output: OutputStream): Unit = {
    generateXML(featureMembers, output)
  }

  private def generateXML(featureMembers: Seq[(AssetType, Seq[Feature[AssetProperties]])], output: OutputStream): Unit = {
    val writer = new OutputStreamWriter(new BufferedOutputStream(output), "UTF-8")
    writer.write(s"""<?xml version="1.0" encoding="UTF-8"?>""")
    writer.write(s"""\n<Stops xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n""")

    featureMembers.foreach { case (_, busStops) =>
      busStops.foreach { busStop =>
        val bs = busStop.asInstanceOf[FeaturePoint[IncomingMassTransitStopProperties]]
        val bsProperties = bs.properties
        val bsXCoordinate = bs.geometry.coordinates.head.toInt
        val bsYCoordinate = bs.geometry.coordinates.last.toInt

        val xmlBody = createBusStopXMLBody(bs.id, bsXCoordinate, bsYCoordinate, bsProperties)
        writer.write(xmlBody.toString)
      }
      writer.write(s"""\n</Stops>""")
      writer.flush()
    }
  }

  private def createBusStopXMLBody(id: Long, xCoordinate: Int, yCoordinate: Int, properties: IncomingMassTransitStopProperties): Any = {
    <Stop>
      <StopId>{id}</StopId>
      <AdminStopId>{properties.adminStopId}</AdminStopId>
      <StopCode>{properties.stopCode}</StopCode>
      { if (properties.name_fi.isDefined || properties.name_sv.isDefined)
          <Names>
            {if (properties.name_fi.isDefined) <Name lang="fi"> {properties.name_fi.head} </Name> }
            {if (properties.name_sv.isDefined) <Name lang="sv"> {properties.name_sv.head} </Name> }
          </Names>
      }
      <Coordinate>
        <xCoordinate>{xCoordinate}</xCoordinate>
        <yCoordinate>{yCoordinate}</yCoordinate>
      </Coordinate>
      <Bearing>{properties.bearing}</Bearing>
      {if (properties.bearingDescription.isDefined) <BearingDescription>{ properties.bearingDescription.get }</BearingDescription>}
      {if (properties.direction.isDefined) <Direction>{properties.direction.get}</Direction>}
      <StopAttribute>{
        properties.stopAttribute.map { busStopType =>
          <StopType>
            {busStopType}
          </StopType>
          }
      }</StopAttribute>
      <Equipment>{properties.equipment}</Equipment>
      <Reachability>{properties.reachability}</Reachability>
      <SpecialNeeds>{properties.specialNeeds}</SpecialNeeds>
      <ModifiedTimestamp>{properties.modifiedTimestamp}</ModifiedTimestamp>
      { if(properties.validFrom.equals("true"))
          <ValidFrom xsi:nil="true" />
        else
          <ValidFrom>{properties.validFrom}</ValidFrom>
      }
      { if(properties.validTo.equals("true"))
          <ValidTo xsi:nil="true" />
        else
          <ValidTo>{properties.validTo}</ValidTo>
      }
      <AdministratorCode>{properties.administratorCode}</AdministratorCode>
      <MunicipalityCode>{properties.municipalityCode}</MunicipalityCode>
      <MunicipalityName>{properties.municipalityName}</MunicipalityName>
      <Comments>{properties.comments}</Comments>
      <PlatformCode>{properties.platformCode}</PlatformCode>
      <ConnectedToTerminal>{properties.connectedToTerminal}</ConnectedToTerminal>
      <ContactEmails>
        <Contact>{properties.contactEmails}</Contact>
      </ContactEmails>
      <ZoneId>{properties.zoneId}</ZoneId>
    </Stop>
  }
}
