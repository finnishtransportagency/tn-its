package fi.liikennevirasto.digiroad2.tnits.openlr.examples;

import openlr.LocationReference;
import openlr.OpenLRProcessingException;
import openlr.binary.ByteArray;
import openlr.encoder.LocationReferenceHolder;
import openlr.encoder.OpenLREncoder;
import openlr.encoder.OpenLREncoderParameter;
import openlr.location.Location;
import openlr.location.LocationFactory;
import openlr.map.Line;
import openlr.map.MapDatabase;
import openlr.map.loader.MapLoadParameter;
import openlr.map.loader.OpenLRMapLoaderException;
import openlr.map.sqlite.loader.DBFileNameParameter;
import openlr.map.sqlite.loader.SQLiteMapLoader;
import openlr.properties.OpenLRPropertiesReader;
import org.apache.commons.configuration.Configuration;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class EncoderExample {
    /* needs to be adjusted */
    private static final String PATH_TO_DB = "tomtom-openlr-testdata-utrecht/tomtom_utrecht_2008_04.db3";

    /* needs to be adjusted */
    // private static final String PATH_TO_LOG4J_PROPERTIES = "...";

    private static final InputStream ENCODER_PROPERTIES = Thread
        .currentThread().getContextClassLoader()
        .getResourceAsStream("OpenLR-Encoder-Properties.xml");

    public static void main(String[] args) throws UnsupportedEncodingException {

        // setup logging, optional
        // PropertyConfigurator.configure(PATH_TO_LOG4J_PROPERTIES);

        // instantiate map database
        MapDatabase mdb = null;
        SQLiteMapLoader mapLoader = new SQLiteMapLoader();
        List<MapLoadParameter> params = new ArrayList<MapLoadParameter>();
        DBFileNameParameter dbFile = new DBFileNameParameter();
        dbFile.setValue(PATH_TO_DB);
        params.add(dbFile);

        try {
            mdb = mapLoader.load(params);
        } catch (OpenLRMapLoaderException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // load location path with n lines
        List<Line> path = new ArrayList<Line>();
        path.add(mdb.getLine(15280002805007L));
        path.add(mdb.getLine(15280002805010L));
        path.add(mdb.getLine(15280002805011L));
        path.add(mdb.getLine(15280002805003L));

        // instantiate Location object without offsets
        Location location = LocationFactory.createLineLocation("Location-1",
            path);

        LocationReferenceHolder locRef = null;
        try {
            // prepare encoder parameter with map database and properties
            Configuration conf = OpenLRPropertiesReader
                .loadPropertiesFromStream(ENCODER_PROPERTIES, true);

            OpenLREncoderParameter encParams = new OpenLREncoderParameter.Builder()
                .with(mdb).with(conf).buildParameter();

            // encode the location
            OpenLREncoder encoder = new OpenLREncoder();
            // prepare encoding result object
            locRef = encoder.encodeLocation(encParams, location);
        } catch (OpenLRProcessingException e) {
            e.printStackTrace();
            System.exit(2);
        }

        // check validity of the location reference
        if (locRef.isValid()) {

            // location reference is valid
            // e.g. get binary format
            LocationReference lr = locRef.getLocationReference("binary");
            ByteArray ba = (ByteArray) lr.getLocationReferenceData();
            String base64EncodedOpenLRLocation = new String(Base64.getEncoder().encode(ba.getData()), "ASCII");

            System.out.println("Location reference is valid: " + base64EncodedOpenLRLocation);
        } else {
            // location reference is not valid, print out error code
            System.out
                .println("Location reference is NOT valid -- error code: "
                    + locRef.getReturnCode());
        }
    }
}