package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.io.{FileInputStream, StringReader}
import javax.xml.XMLConstants
import javax.xml.parsers.{SAXParser, SAXParserFactory}
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import org.scalatest.FunSuite
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}
import scala.util.Try
import scala.xml.{Elem, SAXParseException}
import scala.xml.factory.XMLLoader
import scala.xml.parsing.{FactoryAdapter, NoBindingFactoryAdapter}


/*
*  XML Validator class
*  It is a little Java style because I haven't found the correspondent class in scala
*/
class XMLValidator(schema: Schema) extends  XMLLoader[Elem] {

  override def adapter: FactoryAdapter = new NoBindingFactoryAdapter() {
    override def error(e: SAXParseException) = {
      throw e
    }
  }

  override def parser: SAXParser = {
    val saxPF = SAXParserFactory.newInstance()
    saxPF.setNamespaceAware(true)
    saxPF.setSchema(schema)
    saxPF.newSAXParser()
  }
}


class RosatteXSDValidatorSpec extends FunSuite {

  test("Test the xml against Rosatte xsd file") {

    /* schemaLocation with the path to the schema we want to test */
    val W3C_XSD_TOP_ELEMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
      "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.ptvag.com/rosatte/dataexchange/rest\" elementFormDefault=\"qualified\">\n" +
      "<xs:include schemaLocation=\"./src/main/webapp/old-schemas/ROSATTE-rest.xsd\"/>\n" +
      "</xs:schema>"

    /* XML we want to test */
    val xmlToTest = new FileInputStream("./src/test/scala/fi/liikennevirasto/digiroad2/tnits/rosatte/RosatteTestXML.xml")

    /* Loading schema */
    val schema = {
      val factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
      factory.newSchema(new StreamSource( new StringReader(W3C_XSD_TOP_ELEMENT), "xsdTop"))
    }

    /* Instantiate the XML validator and validate the XML against the schema */
    val result = Try {
      new XMLValidator(schema).load(xmlToTest)
    }

    /* Close the file */
    xmlToTest.close()

    if (result.isFailure) {
      println(result)
    }

    result.isFailure should be (false)
    result.isSuccess should be (true)
  }

}
