package server.tz;

import javafx.scene.control.CheckBox;
import org.postgis.LinearRing;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Dogovor implements Serializable {
    private UUID dogovorId;
    private String dogNo;
    private Timestamp dogDate;
    private Timestamp updateTime;
    private boolean check;
    private CheckBox checkBox;
    private String coordinates;
    private MultiPolygon polygon;

    public Dogovor() {
    }

    public UUID getDogovorId() {
        return dogovorId;
    }

    public void setDogovorId(UUID dogovorId) {
        this.dogovorId = dogovorId;
    }

    public String getDogNo() {
        return dogNo;
    }

    public void setDogNo(String dogNo) {
        this.dogNo = dogNo;
    }

    public Timestamp getDogDate() {
        return dogDate;
    }

    public void setDogDate(Timestamp dogDate) {
        this.dogDate = dogDate;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public MultiPolygon getPolygon() throws XPathExpressionException, TransformerException, IOException, SAXException, ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(coordinates)));

        List<List<LatLng>> result;
        result = transformFromXmlToWgs(document);
        polygon = convetToGeometry(result);
        return polygon;
    }

    public void setPolygon(MultiPolygon polygon) {
        this.polygon = polygon;
    }

    public CheckBox getCheck() {
        checkBox = new CheckBox();
        checkBox.selectedProperty().setValue(checkActulalDate());
        return checkBox;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean checkActulalDate() {
        long updateTimeEpoch = updateTime.toInstant().getEpochSecond();
        long currentTimeEpoch = System.currentTimeMillis() / 1000;
        long difference = currentTimeEpoch - updateTimeEpoch;
        return difference < 5184000L;
    }


    public static List<List<LatLng>> transformFromXmlToWgs(Node root)
            throws XPathExpressionException, TransformerException {
        List<List<LatLng>> result = new ArrayList<>();

        Node outline = selectSingleNode(root,"КоординатыУчастка/Контур");

        for (int i = 0; i < outline.getChildNodes().getLength(); i++) {
            Node outlineElement = outline.getChildNodes().item(i);
            if (!outlineElement.hasChildNodes()) {
                continue;
            }

            List<LatLng> points = new ArrayList<>();

            for (int j = 0; j < outlineElement.getChildNodes().getLength(); j++) {
                Node pointNode = outlineElement.getChildNodes().item(j);
                if (pointNode.getAttributes() == null) {
                    continue;
                }

                points.add(new LatLng(
                        Double.parseDouble(pointNode.getAttributes().getNamedItem("Y").getNodeValue()),
                        Double.parseDouble(pointNode.getAttributes().getNamedItem("X").getNodeValue())
                ));
            }
            result.add(points);
        }

        return result;
    }


    public static org.postgis.MultiPolygon convetToGeometry(List<List<LatLng>> contours) {
        List<Polygon> geometries = new ArrayList<>();
        for (List<LatLng> contur : contours) {
            List<org.postgis.Point> points = new ArrayList<>();
            if (contur.size() == 1) {
                for (int i = 0; i < 4; i++) {
                    points.add(new Point(contur.get(0).getLng(), contur.get(0).getLat()));
                }
            } else {
                for (LatLng latLng : contur) {
                    points.add(new Point(latLng.getLng(), latLng.getLat()));
                }
                if (contur.get(0).equals(contur.get(contur.size() - 1))) {
                } else {
                    List<org.postgis.Point> pointList = new ArrayList<>();
                    pointList.addAll(points);
                    Collections.reverse(points);
                    pointList.addAll(points);
                    points = pointList;
                }
            }
            geometries.add(createPolygin(points));
        }

        MultiPolygon multiPolygon = new MultiPolygon(geometries.stream().toArray(org.postgis.Polygon[]::new));

        return multiPolygon;
    }


    private static org.postgis.Polygon createPolygin(List<org.postgis.Point> points) {
        Point[] pointsArr = points.stream().toArray(Point[]::new);
        org.postgis.Polygon geo = new org.postgis.Polygon(
                new LinearRing[]{
                        new LinearRing(pointsArr)
                }
        );
        return geo;
    }

    private static Node selectSingleNode(Node node, String xpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();

        return ((NodeList) xPath.evaluate(xpath,
                node, XPathConstants.NODESET)).item(0);
    }



    /*public void updateGeometryFromCoordinates(UUID elementId, List<List<LatLng>> conturs) throws SQLException {
        org.postgis.Geometry geometry = Geometry.convetToGeometry(conturs);
        getDb().executeScript(String.format("update ROBJECT SET geom=ST_GeomFromText('%1$s', 4326) " +
                "WHERE object_id= %2$s ", geometry, getDb().getQueryValue(elementId)));
    }*/
}

