package com.gdx.cellular.box2d;

import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.box2d.linesimplification.Visvalingam;
import com.gdx.cellular.box2d.marchingsquares.MooreNeighborTracing;
import com.gdx.cellular.elements.Element;
import com.overdrivr.tools.*;
import java.util.ArrayList;
import java.util.List;

public class ShapeFactory {

    private World world;
    private static ShapeFactory shapeFactory;
    private static final EarClippingTriangulator earClippingTriangulator = new EarClippingTriangulator();
    private static final DelaunayTriangulator delaunayTriangulator = new DelaunayTriangulator();
    private static final ContourToPolygons ctp = new ContourToPolygons();

    private ShapeFactory(World world) {
        this.world = world;
    }

    public static void initialize(World world) {
        if (shapeFactory == null) {
            shapeFactory = new ShapeFactory(world);
        }
    }

    public static Body createDynamicPolygonFromElementArray(int x, int y, Array<Array<Element>> elements) {
        return createPolygonFromElementArray(x, y, elements, BodyDef.BodyType.DynamicBody);
    }

    public static Body createStaticPolygonFromElementArray(int x, int y, Array<Array<Element>> elements) {
        return createPolygonFromElementArray(x, y, elements, BodyDef.BodyType.StaticBody);
    }

    public static Body createPolygonFromElementArrayDeleteOldBody(int x, int y, Array<Array<Element>> elements, Body body) {
        Body newBody = createPolygonFromElementArray(x, y, elements, body.getType());
        if (newBody == null) return null;
        shapeFactory.world.destroyBody(body);
        return newBody;
    } 

    public static Body createPolygonFromElementArray(int x, int y, Array<Array<Element>> elements, BodyDef.BodyType shapeType) {
        int mod = CellularAutomaton.box2dSizeModifier / 2;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = shapeType;
        int xWidth = (elements.get(0).size / 2);
        int yWidth = (elements.size / 2);

        Vector2 center = new Vector2((float) (xWidth + x) / mod, (float) (yWidth + y) / mod);

        bodyDef.position.set(center);
        Body body = shapeFactory.world.createBody(bodyDef);

        elements.reverse();
        List<Vector2> allVerts = MooreNeighborTracing.getOutliningVerts(elements);
        elements.reverse();
        List<Vector2> allVertsTransformed = new ArrayList<>();
        PNGtoBox2D ptb = new PNGtoBox2D();
        Array<Vector2> allVertsArray = ListToArray(allVerts);
        if(allVertsArray.size > 2){
        Array<Vector2> simplifiedVerts = ptb.RDP(allVertsArray,0.03f);
        //Visvalingam.simplify(allVerts);

        for (int i = 0; i < simplifiedVerts.size; i++) {

            allVertsTransformed.add(new Vector2((simplifiedVerts.get(i).x - xWidth) / (mod), (simplifiedVerts.get(i).y - yWidth) / (mod)));

        }
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 5;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0.1f;
//        if (allVertsTransformed.size() < 4) {
//            Vector2 v1 = allVertsTransformed.get(0);
//            Vector2 v2 = allVertsTransformed.get(1);
//            int maxx, maxy, minx, miny;
//            if (v1.x > v2.x) {
//                maxx = (int) Math.floor(v1.x);
//                minx = (int) Math.floor(v2.x);
//            } else {
//                maxx = (int) Math.floor(v2.x);
//                minx = (int) Math.floor(v1.x);
//            }
//            if (v1.y > v2.y) {
//                maxy = (int) Math.floor(v1.y);
//                miny = (int) Math.floor(v2.y);
//            } else {
//                maxy = (int) Math.floor(v2.y);
//                miny = (int) Math.floor(v1.y);
//            }
//            ctp.BuildShape(body, fixtureDef, toFloatArray(getRectVertices(minx, maxx, miny, maxy)));
//        } else {
            ctp.BuildShape(body, fixtureDef, toFloatArray(allVertsTransformed));
        
        return body;
        } else {
            return null;
        }
    }

    // This method takes a 2D array of elements and performs the following steps.
    // Finds the outlining vertices
    // Converts them to Box2D scale
    // Uses Douglas-Peucker simplification to remove unnecessary vertices
    // Uses a SweepLine algorithm to break the polygon into 1 or more convex polygons
    // Uses a triangulation algorithm
    // Creates Box2D Fixtures from the triangles and adds them to the body
//    public static Body createPolygonFromElementArray(int x, int y, Array<Array<Element>> elements, BodyDef.BodyType shapeType) {
//        int mod = CellularAutomaton.box2dSizeModifier/2;
//        BodyDef bodyDef = new BodyDef();
//        bodyDef.type = shapeType;
//        int xWidth = (elements.get(0).size/2);
//        int yWidth = (elements.size / 2);
//
//        Vector2 center = new Vector2((float) (xWidth + x) / mod, (float) (yWidth + y) / mod);
//
//        bodyDef.position.set(center);
//        Body body = shapeFactory.world.createBody(bodyDef);
//
//        elements.reverse();
//        List<Vector2> allVerts = MooreNeighborTracing.getOutliningVerts(elements);
//        elements.reverse();
//        List<Vector2> allVertsTransformed = new ArrayList<>();
//        for (int i = 0; i < allVerts.size(); i++) {
//
//            allVertsTransformed.add(new Vector2((allVerts.get(i).x - xWidth) / (mod), (allVerts.get(i).y - yWidth) / (mod)));
//
//        }
//
//// LOCATIONTECH POLYGON GENERATION
//        List<Float> earVertsList = new ArrayList<>();
//        for (int i = 0; i < allVertsTransformed.size(); i++) {
//            earVertsList.add(allVertsTransformed.get(i).x);
//            earVertsList.add(allVertsTransformed.get(i).y);
//        }
//        
//        earVertsList.add(allVertsTransformed.get(0).x);
//        earVertsList.add(allVertsTransformed.get(0).y);
//        float[] earVerts = new float[earVertsList.size()];
//        for(int i = 0; i < earVertsList.size(); ++i) {
//            earVerts[i] = earVertsList.get(i);
//        }
//
//        GeometryFactory geometryFactory = new GeometryFactory();
//        CoordinateSequence coordinateSequence = new PackedCoordinateSequence.Float(earVerts, 2, 0);
//        if (!(coordinateSequence.size() == 0 || coordinateSequence.size() >= 4)) {
//            return body;
//        }
//        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
//        Polygon polygon = geometryFactory.createPolygon(linearRing);
//
//        final Geometry simplifiedPolygon = DouglasPeuckerSimplifier.simplify(polygon, .3);
//
//        List<org.dyn4j.geometry.Vector2> dyn4jVerts = new ArrayList<>();
//        for (int i = 0; i < simplifiedPolygon.getCoordinates().length; i++) {
//        
//            dyn4jVerts.add(new org.dyn4j.geometry.Vector2(simplifiedPolygon.getCoordinates()[i].x, simplifiedPolygon.getCoordinates()[i].y));
//            
//        }
//
//        if (dyn4jVerts.size() <= 2) {
//            return null;
//        }
//        dyn4jVerts.remove(dyn4jVerts.size() - 1);
//        List<Convex> convexes;
//        if (dyn4jVerts.size() == 3) {
//            Convex convex = new org.dyn4j.geometry.Polygon((org.dyn4j.geometry.Vector2) dyn4jVerts);
//            convexes = new ArrayList<>();
//            convexes.add(convex);
//        } else if (dyn4jVerts.size() > 3) {
//            try {
//                convexes = sweepLine.decompose(dyn4jVerts.toArray(new org.dyn4j.geometry.Vector2[0]));
//            } catch (Exception e) {
//                return null;
//            }
//        } else {
//            return null;
//        }
//
//        for (Convex convex : convexes) {
//            org.dyn4j.geometry.Polygon dynConvexPolygon = (org.dyn4j.geometry.Polygon) convex;
//            org.dyn4j.geometry.Vector2[] dyn4jConvexVerts = dynConvexPolygon.getVertices();
//            float[] convexVerts = new float[dyn4jConvexVerts.length * 2 + 2];
//            for(int i = 0; i < dyn4jConvexVerts.length; ++i) {
//                convexVerts[i * 2] = (float) dyn4jConvexVerts[i].x;
//                convexVerts[i * 2 + 1] = (float) dyn4jConvexVerts[i].y;
//            }
//            convexVerts[convexVerts.length - 2] = (float) dyn4jConvexVerts[0].x;
//            convexVerts[convexVerts.length - 1] = (float) dyn4jConvexVerts[0].y;
//
//            CoordinateSequence convexCoordinateSequence = new PackedCoordinateSequence.Float(convexVerts, 2, 0);
//            LinearRing convexLinearRing = new LinearRing(convexCoordinateSequence, geometryFactory);
//            Polygon ltConvexPolygon = geometryFactory.createPolygon(convexLinearRing);
//
//            DelaunayTriangulationBuilder triangulationBuilder = new DelaunayTriangulationBuilder();
//            triangulationBuilder.setSites(ltConvexPolygon);
//            Geometry triangulatedGeometry = triangulationBuilder.getTriangles(geometryFactory);
//            int geometryCount = triangulatedGeometry.getNumGeometries();
//
//            for (int i = 0; i < geometryCount; i++) {
//                Geometry currentGeometry = triangulatedGeometry.getGeometryN(i);
//                Coordinate[] coordinates = currentGeometry.getCoordinates();
//                Vector2[] triangleVerts = new Vector2[3];
//                for (int c = 0; c < 3; c++) {
//                    Coordinate currentCoordinate = coordinates[c];
//                    Vector2 transformedCoordinate = new Vector2();
//                    transformedCoordinate.x = (float) currentCoordinate.x;
//                    transformedCoordinate.y = (float) currentCoordinate.y;
//                    triangleVerts[c] = transformedCoordinate;
//                }
//                PolygonShape polygonForFixture = new PolygonShape();
//                polygonForFixture.set(triangleVerts);
//                FixtureDef fixtureDef = new FixtureDef();
//                fixtureDef.shape = polygonForFixture;
//                fixtureDef.density = 5;
//                fixtureDef.friction = 1f;
//                fixtureDef.restitution = 0.1f;
//                body.createFixture(fixtureDef);
//                polygonForFixture.dispose();
//            }
//        }
//        return body;
//        }

    public static Body createDefaultDynamicCircle(int x, int y, int radius) {
        return createDynamicCircle(x, y, radius, 0.5f, 0.4f, 0.6f);
    }

    public static Body createDynamicCircle(int x, int y, int radius, float density, float friction, float restituion) {
        int mod = CellularAutomaton.box2dSizeModifier;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;


        bodyDef.position.set((float) x / mod, (float) y / mod);

        Body body = shapeFactory.world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);


        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restituion;

        Fixture fixture = body.createFixture(fixtureDef);

        body.setLinearVelocity(new Vector2(0, -10));

        circle.dispose();
        return body;
    }

    public static Body createDynamicBox(int x, int y, int size, float density, float friction, float restitution) {
        int mod = CellularAutomaton.box2dSizeModifier;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;


        bodyDef.position.set((float) x / mod, (float) y / mod);

        Body body = shapeFactory.world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(size, size);


        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        Fixture fixture = body.createFixture(fixtureDef);

        body.setLinearVelocity(new Vector2(0, -10));

        box.dispose();
        return body;
    }




    private static List<List<Vector2>> getOutliningVertices(Array<Array<Element>> elements) {
        List<Vector2> leftSideVertices = new ArrayList<>();
        List<Vector2> rightSideVertices = new ArrayList<>(elements.size);
        boolean foundFirst;
        Vector2 mostRecentElementPos = null;
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            foundFirst = false;
            for (int x = 0; x < elements.get(0).size; x++) {
                Element element = row.get(x);
                if (element != null) {
                    if (!foundFirst) {
                        leftSideVertices.add(new Vector2(x, y));
                        foundFirst = true;
                    } else {
                        mostRecentElementPos = new Vector2(x, y);
                    }
                }
            }
            if (mostRecentElementPos != null) {
                rightSideVertices.add(0, mostRecentElementPos.cpy());
                mostRecentElementPos = null;
            }

        }
        List<List<Vector2>> outliningVerts = new ArrayList<>();
        outliningVerts.add(new ArrayList<>(leftSideVertices));
        outliningVerts.add(new ArrayList<>(rightSideVertices));
        return outliningVerts;
    }

    public static Body createDefaultDynamicBox(int x, int y, int size) {
        return createDynamicBox(x, y, size, 0.5f, 0.4f, 0.1f);
    }



    public static Body createStaticRect(Vector3 boxCenter, List<Vector2> vertices) {
        return createRect(boxCenter, vertices, 0, 0.5f, BodyDef.BodyType.StaticBody);
    }

    public static Body createDynamicRect(Vector3 boxCenter, List<Vector2> vertices) {
        return createRect(boxCenter, vertices, 0, 0.5f, BodyDef.BodyType.DynamicBody);
    }

    public static Body createBoxByBodyType(Vector3 boxCenter, List<Vector2> vertices, BodyDef.BodyType type) {
        return createRect(boxCenter, vertices, 0, 0.5f, type);
    }

    public static Body createRect2(Vector3 boxCenter, List<Vector2> vertices, int angle, float friction, BodyDef.BodyType type) {
        int mod = CellularAutomaton.box2dSizeModifier / 2;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;

        Vector2 center = new Vector2(Math.round(boxCenter.x / mod), Math.round(boxCenter.y / mod));
        bodyDef.position.set(center);

        Body body = shapeFactory.world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
//        List<Vector2> updatedVertices = vertices.stream().map(v -> v.scl(1f/(float) mod)).collect(Collectors.toList());
//        updatedVertices = vertices.stream().map(v -> {
//            v.x = (float) Math.floor(v.x);
//            v.y = (float) Math.floor(v.y);
//            return v;
//        }).collect(Collectors.toList());
//        Vector2[] verticesAsArray = new Vector2[4];
//        verticesAsArray[0] = updatedVertices.get(0);
//        verticesAsArray[1] = updatedVertices.get(1);
//        verticesAsArray[2] = updatedVertices.get(2);
//        verticesAsArray[3] = updatedVertices.get(3);
//        box.set(verticesAsArray);

        box.setAsBox(5, 5);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 1;
        fixtureDef.friction = friction;

        Fixture fixture = body.createFixture(fixtureDef);

        box.dispose();
        body.setTransform(body.getPosition(), angle);
        return body;
    }

    public static Body createRect(Vector3 boxCenter, List<Vector2> vertices, int angle, float friction, BodyDef.BodyType type) {
        int mod = CellularAutomaton.box2dSizeModifier / 2;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;

        Vector2 center = new Vector2(Math.round(boxCenter.x / mod), Math.round(boxCenter.y / mod));
        bodyDef.position.set(center);

        Vector2 v1 = vertices.get(0);
        Vector2 v2 = vertices.get(1);
        int maxx, maxy, minx, miny;
        if (v1.x > v2.x) {
            maxx = (int) Math.floor(v1.x);
            minx = (int) Math.floor(v2.x);
        } else {
            maxx = (int) Math.floor(v2.x);
            minx = (int) Math.floor(v1.x);
        }
        if (v1.y > v2.y) {
            maxy = (int) Math.floor(v1.y);
            miny = (int) Math.floor(v2.y);
        } else {
            maxy = (int) Math.floor(v2.y);
            miny = (int) Math.floor(v1.y);
        }


        Body body = shapeFactory.world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.friction = 0.8f;

        ctp.BuildShape(body, fixtureDef, toFloatArray(getRectVertices(minx, maxx, miny, maxy)));
        body.setTransform(body.getPosition(), angle);
        return body;
    }

    private static FloatArray toFloatArray(List<Vector2> reducedVerts) {
        // float[] floatArray = new float[reducedVerts.size() * 2];
        FloatArray array = new FloatArray();
        for (int i = 1 ; i < reducedVerts.size() ; i++) {
            array.add(reducedVerts.get(i).x);
            array.add(reducedVerts.get(i).y);
        }
        //   for (int i = 0; i < reducedVerts.size(); i++) {
        //Vector2 vert = reducedVerts.get(i);
        //  floatArray[i * 2] = vert.x;
        //    floatArray[(i * 2) + 1] = vert.y;
        //  }
        return array;
    }
    
    public static Array<Vector2> ListToArray(List<Vector2> v){
        Array<Vector2> nv = new Array<Vector2>();
        for (int i = 0; i < v.size(); i++) {           
            nv.add(v.get(i));
        }
        return nv;
    }

    private static List<Vector2> getRectVertices(int minX, int maxX, int minY, int maxY) {
        List<Vector2> verts = new ArrayList<>();
        verts.add(new Vector2(minX, minY));
        verts.add(new Vector2(minX, maxY));
        verts.add(new Vector2(maxX, maxY));
        verts.add(new Vector2(maxX, minY));
        return verts;
    }

    public static void clearAllActors() {
        Array<Body> bodies = new Array<>();
        shapeFactory.world.getBodies(bodies);
        for (int i = 0; i < bodies.size; i++) {
            if (!shapeFactory.world.isLocked())
                shapeFactory.world.destroyBody(bodies.get(i));
        }
    }
}
