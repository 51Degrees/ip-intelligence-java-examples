/* *********************************************************************
 * This Original Work is copyright of 51 Degrees Mobile Experts Limited.
 * Copyright 2026 51 Degrees Mobile Experts Limited, Davidson House,
 * Forbury Square, Reading, Berkshire, United Kingdom RG1 3EU.
 *
 * This Original Work is licensed under the European Union Public Licence
 * (EUPL) v.1.2 and is subject to its terms as set out below.
 *
 * If a copy of the EUPL was not distributed with this file, You can obtain
 * one at https://opensource.org/licenses/EUPL-1.2.
 *
 * The 'Compatible Licences' set out in the Appendix to the EUPL (as may be
 * amended by the European Commission) shall be deemed incompatible for
 * the purposes of the Work and the provisions of the compatibility
 * clause in Article 5 of the EUPL shall not apply.
 *
 * If using the Work as, or as part of, a network application, by
 * including the attribution notice(s) required under Article 5 of the EUPL
 * in the end user terms of the application under an appropriate heading,
 * such notice(s) shall fulfill the requirements of that article.
 * ********************************************************************* */

package fiftyone.ipintelligence.examples.console.areas;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to work out the common values for areas in the examples.
 * <p>
 * As the areas involved can be quite large a grid is used to break down areas
 * into smaller areas of no more than 1 degree of latitude and longitude
 * before applying a UTM calculation to work out the area. The area of these
 * smaller areas are then combined to provide the result.
 * <p>
 * This approach handles the differences in area calculation nearer the
 * equator or the poles.
 */
public class Calculations {

    /**
     * Parses WKT strings into geometries. WKTReader is not thread safe so a
     * thread local instance is used.
     */
    private static final ThreadLocal<WKTReader> wktReader =
            ThreadLocal.withInitial(WKTReader::new);

    /**
     * A grid of latitude and longitude rectangles. Used to work out the
     * geographic area using individual polygons no larger than 1 unit of
     * latitude and longitude to avoid distortions due to the differences in
     * calculation near the equator or the poles.
     */
    private static final Rectangle[][] grid = createGrid();

    private Calculations() {
    }

    /**
     * Returns the result for the WKT string, and geographic point.
     * @param wkt WKT format geometric area(s)
     * @param latitude of the point being tested for inclusion in the
     *                 geographic area
     * @param longitude of the point being tested for inclusion in the
     *                  geographic area
     * @return the calculated result
     */
    public static Result getAreas(
            String wkt,
            double latitude,
            double longitude) throws Exception {
        Geometry geo = wktReader.get().read(wkt);
        if (geo != null) {
            return getAreas(geo, latitude, longitude);
        }
        return new Result(0, 0, false);
    }

    /**
     * Returns the result for the geometric area, and geographic point.
     * @param geo geometric area(s)
     * @param latitude of the point being tested for inclusion in the
     *                 geographic area
     * @param longitude of the point being tested for inclusion in the
     *                  geographic area
     * @return the calculated result
     */
    public static Result getAreas(
            Geometry geo,
            double latitude,
            double longitude) {
        // True if the area contains the point. This must be done before
        // the geo instance is manipulated by getAreas and converted to
        // different coordinate units.
        boolean contains = geo.contains(geo.getFactory().createPoint(
                new Coordinate(longitude, latitude)));
        double area = getAreas(geo);
        return new Result(
                // The total area in square kms.
                (int) Math.round(area / 1_000_000),
                // Number of polygons in the area.
                geo.getNumGeometries(),
                // Whether the geographic area contains the point.
                contains);
    }

    private static double getAreas(Geometry geo) {
        double area = 0.0;
        if (geo.getNumGeometries() > 1) {
            for (int i = 0; i < geo.getNumGeometries(); i++) {
                area += getAreas(geo.getGeometryN(i));
            }
        }
        else if (geo.isEmpty() == false) {
            area += getArea(geo);
        }

        // Calculate area in square meters and convert to square kilometers
        return area;
    }

    private static double getArea(Geometry geo) {
        double area = 0.0;
        for (Rectangle rectangle : getRectangles(geo)) {
            try {
                area += getArea(geo, rectangle);
            }
            catch (TopologyException e) {
                // In rare situations the intersection between the rectangle
                // and the geometric area results in an exception. When this
                // happens fall back to calculating the area with the
                // rectangle's transformation not using the intersection or
                // other rectangles.
                area = getArea(geo, geo, rectangle.getTransformation());
                break;
            }
        }
        return area;
    }

    private static double getArea(Geometry geo, Rectangle rectangle) {
        double area = 0.0;
        // The rectangle might relate to an area that doesn't intersect the
        // geometric shape. For example, when the shape does not include a
        // grid rectangle.
        if (geo.intersects(rectangle.getPolygon())) {
            Geometry intersect = geo.intersection(rectangle.getPolygon());
            if (intersect.getNumGeometries() == 1) {
                if (intersect.getArea() > 0) {
                    area = getArea(
                            geo,
                            intersect,
                            rectangle.getTransformation());
                }
            }
            else {
                for (int i = 0; i < intersect.getNumGeometries(); i++) {
                    if (geo.getGeometryN(i).getArea() > 0) {
                        area += getArea(
                                geo,
                                geo.getGeometryN(i),
                                rectangle.getTransformation());
                    }
                }
            }
        }
        return area;
    }

    private static double getArea(
            Geometry geo,
            Geometry intersect,
            CoordinateTransform transformation) {
        try {
            // Re-project the intersecting polygon to the UTM
            // coordinate system.
            Geometry transformedPolygon = transformGeometry(
                    intersect,
                    transformation);

            // Return the area in square meters.
            return transformedPolygon.getArea();
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(geo.toText(), e);
        }
    }

    private static Geometry transformGeometry(
            Geometry geometry,
            CoordinateTransform transform) {
        GeometryFactory factory = geometry.getFactory();
        Coordinate[] coordinates = new Coordinate[
                geometry.getCoordinates().length];

        for (int i = 0; i < coordinates.length; i++) {
            ProjCoordinate transformed = transform.transform(
                    new ProjCoordinate(
                            geometry.getCoordinates()[i].getX(),
                            geometry.getCoordinates()[i].getY()),
                    new ProjCoordinate());
            coordinates[i] = new Coordinate(
                    transformed.x,
                    transformed.y);
        }

        return factory.createPolygon(coordinates);
    }

    /**
     * Constructs a grid that covers the world for each latitude and longitude
     * rectangle.
     */
    private static Rectangle[][] createGrid() {
        Rectangle[][] grid = new Rectangle[360][];
        GeometryFactory geometryFactory = new GeometryFactory();
        for (int x = -180; x < 180; x++) {
            grid[x + 180] = new Rectangle[180];
            for (int y = -90; y < 90; y++) {
                grid[x + 180][y + 90] = new Rectangle(
                        geometryFactory.createPolygon(new Coordinate[]{
                                new Coordinate(x, y),
                                new Coordinate(x + 1, y),
                                new Coordinate(x + 1, y + 1),
                                new Coordinate(x, y + 1),
                                new Coordinate(x, y)}));
            }
        }
        return grid;
    }

    private static List<Rectangle> getRectangles(Geometry source) {
        // Work out the lowest and highest latitude and longitudes for the
        // source.
        double xa = source.getCoordinate().getX();
        double xb = source.getCoordinate().getX();
        double ya = source.getCoordinate().getY();
        double yb = source.getCoordinate().getY();
        for (int i = 1; i < source.getCoordinates().length; i++) {
            Coordinate c = source.getCoordinates()[i];
            if (c.getX() < xa) xa = c.getX();
            if (c.getX() > xb) xb = c.getX();
            if (c.getY() < ya) ya = c.getY();
            if (c.getY() > yb) yb = c.getY();
        }

        // Return all the rectangles from the grid that intersect with the
        // polygon provided.
        List<Rectangle> rectangles = new ArrayList<>();
        for (int x = (int) Math.floor(xa); x < (int) Math.ceil(xb); x++) {
            for (int y = (int) Math.floor(ya); y < (int) Math.ceil(yb); y++) {
                rectangles.add(grid[x + 180][y + 90]);
            }
        }
        return rectangles;
    }
}
