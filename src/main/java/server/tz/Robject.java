package server.tz;

import org.postgis.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Robject implements Serializable {
    private UUID objectId;
    private String coordinates;
    private MultiPolygon geom;

    public Robject() {
    }

    public UUID getObjectId() {
        return objectId;
    }

    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getGeom() {
        String geometry;
        if (geom==null) {
            geometry = "";
        } else {
            geometry = geom.toString();
        }
        return geometry;
    }

    public void setGeom(MultiPolygon geom) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(coordinates)));
            List<List<LatLng>> result;
            result = transformFromXmlToWgs(document);
            geom = convetToGeometry(result);
            this.geom = geom;
    }

    public static List<List<LatLng>> transformFromXmlToWgs(Node root)
            throws XPathExpressionException {
        List<List<LatLng>> result = new ArrayList<>();

        Node outline = selectSingleNode(root, "КоординатыУчастка/Контур");

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


    public static MultiPolygon convetToGeometry(List<List<LatLng>> contours) {
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


    private static Polygon createPolygin(List<org.postgis.Point> points) {
        Point[] pointsArr = points.stream().toArray(Point[]::new);
        Polygon geo = new org.postgis.Polygon(
                new LinearRing[]{
                        new LinearRing(pointsArr)
                }
        );
        return geo;
    }

    private static Node selectSingleNode(Node node, String xpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();

        return ((NodeList) xPath.evaluate(xpath, node, XPathConstants.NODESET)).item(0);
    }
}

