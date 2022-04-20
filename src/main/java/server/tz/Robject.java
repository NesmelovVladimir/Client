package server.tz;

import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.ProjCoordinate;
import org.postgis.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Robject implements Serializable {
    private UUID objectId;
    private String coordinates;
    private GeometryCollection geom;
    private String oldGeom;

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

    /**
     * Обработка пустой геометрии полученной из базы
     */
    public String getOldGeom() {
        String geometry;
        if (oldGeom == null) {
            geometry = "";
        } else {
            geometry = oldGeom;
        }
        return geometry;
    }

    public void setOldGeom(String oldGeom) {
        if (oldGeom.isEmpty()) {
            this.oldGeom = null;
        } else {
            this.oldGeom = oldGeom;
        }
    }

    /**
     * Обработка пустой геометрии, которую не получилось преобразовать из coordinates
     */
    public String getGeom() {
        String geometry;
        if (geom == null) {
            geometry = "";
        } else {
            geometry = geom.toString();
        }
        return geometry;
    }

    /**
     * Преобразование coordinates в Geom
     */
    public void setGeom(Map<String, String> coodrinateSystemMap) {
        GeometryCollection geom;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(coordinates)));
            List<List<LatLng>> result;
            result = transformFromXmlToWgs(document, coodrinateSystemMap);
            geom = convetToGeometry(result);
        } catch (Exception e) {
            geom = null;
        }
        this.geom = geom;
    }

    /**
     * Метод преобразования XML в координаты WGS 84
     */
    public static List<List<LatLng>> transformFromXmlToWgs(Node root, Map<String, String> coodrinateSystemMap)
            throws Exception {
        List<List<LatLng>> result = new ArrayList<>();

        Node outline;
        String coordinateSystem = null;
        if (root.getFirstChild().getNodeName().equals("EntitySpatial")) {
            Node outlineElement = root.getChildNodes().item(0);
            coordinateSystem = outlineElement.getAttributes().getNamedItem("EntSys").getNodeValue();
            outline = selectSingleNode(root, "EntitySpatial/SpatialElement");
        } else {
            outline = selectSingleNode(root, "КоординатыУчастка/Контур");
        }
        //Добавить конвертацию координать от системы координат
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
                ProjCoordinate coordSrc = new ProjCoordinate(
                        Double.parseDouble(pointNode.getAttributes().getNamedItem("Y").getNodeValue().replace(",", ".")),
                        Double.parseDouble(pointNode.getAttributes().getNamedItem("X").getNodeValue().replace(",", "."))
                );
                if (coordinateSystem != null) {
                    CRSFactory crsFactory = new CRSFactory();
                    CoordinateReferenceSystem crsSource = Robject
                            .createReferenceSystemFromMskName(coodrinateSystemMap.get(coordinateSystem));
                    CoordinateReferenceSystem crsTarget = crsFactory.createFromParameters("WGS84",
                            "+proj=longlat +ellps=WGS84 +datum=WGS84 +units=degrees");

                    BasicCoordinateTransform t = new BasicCoordinateTransform(crsSource, crsTarget);
                    ProjCoordinate coordTrg = new ProjCoordinate();

                    t.transform(coordSrc, coordTrg);
                    points.add(new LatLng(coordTrg.x, coordTrg.y));
                } else {
                    points.add(new LatLng(coordSrc.x, coordSrc.y));
                }
            }
            result.add(points);
        }

        return result;
    }

    /**
     * Метод преобразования Координат WGS84 в геометрию(MultiPolygon)
     */
    public static GeometryCollection convetToGeometry(List<List<LatLng>> contours) {
        GeometryCollection geometry = new GeometryCollection();
        for (List<LatLng> contur : contours) {
            if (contur.size() == 1) {
                Point point = new Point(contur.get(0).getLng(), contur.get(0).getLat());
                geometry = new GeometryCollection(new Point[]{point});
            } else if (contur.get(0).getLng() != contur.get(contur.size() - 1).getLng() &&
                    contur.get(0).getLat() != contur.get(contur.size() - 1).getLat()) {
                List<Point> linePoints = new ArrayList<>();
                for (LatLng count : contur) {
                    linePoints.add(new Point(count.getLng(), count.getLat()));
                }
                LineString lineString = new LineString(linePoints.toArray(Point[]::new));
                geometry = new GeometryCollection(new LineString[]{lineString});

            } else if (contur.get(0).getLng() == contur.get(contur.size() - 1).getLng() &&
                    contur.get(0).getLat() == contur.get(contur.size() - 1).getLat()) {
                List<Point> polygonPoints = new ArrayList<>();
                for (LatLng count : contur) {
                    polygonPoints.add(new Point(count.getLng(), count.getLat()));
                }
                Point[] pointsArr = polygonPoints.toArray(Point[]::new);
                Polygon polygon = new org.postgis.Polygon(new LinearRing[]{new LinearRing(pointsArr)});
                geometry = new GeometryCollection(new Polygon[]{polygon});
            }
        }
        return geometry;
    }

    /**
     * Метод получения отдельного листа из XML
     */
    private static Node selectSingleNode(Node node, String xpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();

        return ((NodeList) xPath.evaluate(xpath, node, XPathConstants.NODESET)).item(0);
    }

    public static CoordinateReferenceSystem createReferenceSystemFromMskName(String nameMsk) {
        CRSFactory crsFactory = new CRSFactory();
        return crsFactory.createFromParameters(nameMsk, MskParams.getValues().get(nameMsk));
    }
}

