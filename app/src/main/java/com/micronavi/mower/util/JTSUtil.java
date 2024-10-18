/*********************************************************************
 *
 *  This file is part of the [OPEN_MICRO_MOWER_ANDROID] project.
 *  Licensed under the MIT License for non-commercial purposes.
 *  Author: Brook Li
 *  Email: lguitech@126.com
 *
 *  For more details, refer to the LICENSE file or contact [lguitech@126.com].
 *
 *  Commercial use requires a separate license.
 *
 *  This software is provided "as is", without warranty of any kind.
 *
 *********************************************************************/

package com.micronavi.mower.util;

import com.micronavi.mower.bean.GeoPoint;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import java.util.LinkedList;

public class JTSUtil {
    private static final String TAG = "JTSUtil";
    public static void dealSelfIntersect_polygon(LinkedList<GeoPoint> listPoint) {
        boolean isClosed;
        GeoPoint firstPoint = listPoint.getFirst();
        GeoPoint lastPoint = listPoint.getLast();
        isClosed = firstPoint.isEqual(lastPoint);
        Coordinate[] coordinates;
        if (!isClosed) {
            coordinates = new Coordinate[listPoint.size() + 1];
        } else {
            coordinates = new Coordinate[listPoint.size()];
        }

        int index = 0;
        for (GeoPoint point : listPoint) {
            coordinates[index++] = new Coordinate(point.x, point.y);
        }
        if (!isClosed) {
            coordinates[index] = new Coordinate(firstPoint.x, firstPoint.y);
        }
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);

        GeometryFactory geometryFactory = new GeometryFactory();
        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
        Polygon polygon = new Polygon(linearRing, null, geometryFactory);

        if (!polygon.isValid()) {
            Geometry fixedPolygon = polygon.buffer(0);

            listPoint.clear();
            Coordinate[] fixedCoords = fixedPolygon.getCoordinates();
            for (Coordinate coord : fixedCoords) {
                listPoint.add(new GeoPoint(coord.x, coord.y));
            }
        } else {

        }
    }

    public static void dealSelfIntersect_polyline(LinkedList<GeoPoint> listPoint) {
        boolean selfIntersecting = true;
        while (selfIntersecting && listPoint.size() > 3) {
            selfIntersecting = false;
            for (int i = listPoint.size() - 1; i >= 2; i--) {
                LineSegment segment1 = new LineSegment(
                        new Coordinate(listPoint.get(i - 1).x, listPoint.get(i - 1).y),
                        new Coordinate(listPoint.get(i).x, listPoint.get(i).y)
                );
                for (int j = 0; j < i - 2; j++) {
                    LineSegment segment2 = new LineSegment(
                            new Coordinate(listPoint.get(j).x, listPoint.get(j).y),
                            new Coordinate(listPoint.get(j + 1).x, listPoint.get(j + 1).y)
                    );
                    if (segment1.intersection(segment2) != null) {
                        listPoint.subList(j + 1, i).clear();
                        selfIntersecting = true;
                        break;
                    }
                }
                if (selfIntersecting) break;
            }
        }
    }

    private static Point createPointFromGeoPoint(GeoPoint ptInput)
    {
        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createPoint(new Coordinate(ptInput.x, ptInput.y));
    }

    private static Polygon createPolygonFromPoints(LinkedList<GeoPoint> points) {
        if (points.size() < 3) {
            throw new IllegalArgumentException("points number error");
        }
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates;

        GeoPoint firstPoint = points.getFirst();
        GeoPoint lastPoint = points.getLast();
        boolean isClosed = firstPoint.isEqual(lastPoint);
        if (!isClosed) {
            coordinates = new Coordinate[points.size() + 1];
        }
        else {
            coordinates = new Coordinate[points.size()];
        }

        int i = 0;
        for (GeoPoint point : points) {
            coordinates[i++] = new Coordinate(point.x, point.y);
        }
        if (!isClosed) {
            coordinates[i] = new Coordinate(firstPoint.x, firstPoint.y);
        }

        return geometryFactory.createPolygon(coordinates);
    }

    private static LinkedList<GeoPoint> polygonToLinkedList(Polygon polygon) {
        LinkedList<GeoPoint> points = new LinkedList<>();
        Coordinate[] coordinates = polygon.getCoordinates();
        for (Coordinate coordinate : coordinates) {
            points.add(new GeoPoint(coordinate.x, coordinate.y));
        }
        points.removeLast();
        return points;
    }

    public static LinkedList<GeoPoint> dealMergePolygon(LinkedList<GeoPoint> listPoint1, LinkedList<GeoPoint> listPoint2) {
        Polygon polygon1 = createPolygonFromPoints(listPoint1);
        Polygon polygon2 = createPolygonFromPoints(listPoint2);

        boolean doIntersect = polygon1.intersects(polygon2);

        if (doIntersect) {
            Geometry union = polygon1.union(polygon2);

            return polygonToLinkedList((Polygon) union);
        } else {
            return null;
        }
    }

    public static boolean isPolygonIntersect(LinkedList<GeoPoint> listPoint1, LinkedList<GeoPoint> listPoint2) {
        Polygon polygon1 = createPolygonFromPoints(listPoint1);
        Polygon polygon2 = createPolygonFromPoints(listPoint2);

        return polygon1.intersects(polygon2);
    }

    public static boolean isPointIntersectPolygon(GeoPoint ptInput, LinkedList<GeoPoint> listPoint) {
        Point point = createPointFromGeoPoint(ptInput);
        Polygon polygon = createPolygonFromPoints(listPoint);
        return polygon.contains(point);
    }

    public static LinkedList<GeoPoint> getIntersectPolygon(LinkedList<GeoPoint> listPoint1, LinkedList<GeoPoint> listPoint2) {
        Polygon polygon1 = createPolygonFromPoints(listPoint1);
        Polygon polygon2 = createPolygonFromPoints(listPoint2);
        Geometry intersection = polygon1.intersection(polygon2);

        if (intersection != null) {
            return polygonToLinkedList((Polygon) intersection);
        } else {
            return null;
        }

    }

}
