import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        long startId = g.closest(stlon, stlat);
        long endId = g.closest(destlon, destlat);
        //System.out.println(startId);
        for(Map.Entry<Long, GraphDB.Node>each:g.nodeMap.entrySet()){
            each.getValue().distanceFromStart = Long.MAX_VALUE;
        }
        GraphDB.Node start = g.nodeMap.get(startId);
       // System.out.println(start.distanceFromStart+" start");
        start.distanceFromStart = 0;
        GraphDB.Node end = g.nodeMap.get(endId);
        Set<Long> passbyNode = new HashSet<>();


        PriorityQueue<GraphDB.Node> pqNode = new PriorityQueue<>(g.nodeMap.size(), new Comparator<GraphDB.Node>() {
            @Override
            public int compare(GraphDB.Node o1, GraphDB.Node o2) {
                //return (int)(o1.distanceFromStart + g.distance(o1.id, endId) - o2.distanceFromStart - g.distance(o2.id, endId));
                if((o1.distanceFromStart + g.distance(o1.id, endId) - o2.distanceFromStart - g.distance(o2.id, endId))>=0) {
                    //System.out.println("出现相等的情况啦");
                    return 1;
                }
                else{
                    return -1;
                }
                //return (int)(o1.distanceFromStart - o2.distanceFromStart);
                //return (int)(o1.distanceFromStart + g.distance(o1.id, endId) - o2.distanceFromStart - g.distance(o2.id, endId));
            }
        });
        for(Map.Entry<Long, GraphDB.Node> each : start.edge.entrySet()){
            GraphDB.Node cur = each.getValue();
            //System.out.println("cur's id is "+ cur.id);
            cur.lastNode = start;
            cur.distanceFromStart = g.distance(cur.id, startId);
            //System.out.println("最初的几个点"+cur.distanceFromStart);
            cur.distanceFromStart = g.distance(cur.id, startId);
            passbyNode.add(startId);
            pqNode.add(cur);
        }

        while(true){
            GraphDB.Node cur = pqNode.remove();
            while(passbyNode.contains(cur.id)){
                cur = pqNode.remove();
            }
            passbyNode.add(cur.id);
            if(cur.id==endId){
                break;
            }
            for(Map.Entry<Long, GraphDB.Node> each : cur.edge.entrySet()){
                if(passbyNode.contains(each.getKey()))
                    continue;
                GraphDB.Node curEdge = each.getValue();
                //System.out.println("距离 "+ cur.distanceFromStart+ " "+ g.distance(cur.id, curEdge.id)+ " "+ curEdge.distanceFromStart);
                if(cur.distanceFromStart + g.distance(cur.id, curEdge.id)<curEdge.distanceFromStart) {
                    curEdge.lastNode = cur;
                    curEdge.distanceFromStart = cur.distanceFromStart + g.distance(cur.id, curEdge.id);
                    pqNode.add(curEdge);
                }
                //curEdge.distanceFromStart = Math.min(cur.distanceFromStart + g.distance(cur.id, curEdge.id), curEdge.distanceFromStart);
            }
        }
        Stack<GraphDB.Node> stack = new Stack<>();
        GraphDB.Node now = end;
        while(now.id!=startId){
            //System.out.println(now.id);
            stack.push(now);
            now = now.lastNode;
        }
        stack.push(start);
        List<Long> result = new ArrayList<>();
        while(!stack.empty()){
            result.add(stack.pop().id);

        }
        return result;
    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        List<NavigationDirection> result = new ArrayList<>();
        int index = 0;
        long startId = route.get(index);
        NavigationDirection lastNavi = new NavigationDirection();
        lastNavi.direction = 0;
        if(g.nodeMap.get(startId).wayName==null){
            g.nodeMap.get(startId).wayName = NavigationDirection.UNKNOWN_ROAD;
        }
        lastNavi.way = g.nodeMap.get(startId).wayName;
        System.out.println("wayname "+lastNavi.way);
        lastNavi.distance = 0;
        GraphDB.Node lastNode = g.nodeMap.get(startId);

        while(++index<route.size()){
            //NavigationDirection curNavi = new NavigationDirection();
            long curId = route.get(index);
            GraphDB.Node curNode = g.nodeMap.get(curId);
            if(curNode.wayName==null){
                curNode.wayName = NavigationDirection.UNKNOWN_ROAD;
            }
            if(lastNavi.way.equals(curNode.wayName)){
                lastNavi.distance += g.distance(curId, lastNode.id);
                lastNode = curNode;
            }
            else{
                int judge = index+1;
                if(judge==route.size()){
                    result.add(new NavigationDirection());
                    NavigationDirection sizeMinusOne = result.get(result.size() - 1);
                    sizeMinusOne.distance = lastNavi.distance;
                    sizeMinusOne.way = lastNavi.way;
                    sizeMinusOne.direction = lastNavi.direction;
                    lastNavi = new NavigationDirection();
                    lastNavi.distance = g.distance(curId, lastNode.id);
                    lastNavi.way = g.nodeMap.get(curId).wayName;
                    int Direction;
                    double bearing = g.bearing(lastNode.id, curId);
                    if (bearing >= -15 && bearing <= 15) {
                        Direction = 1;
                    } else if (bearing >= -30 && bearing < -15) {
                        Direction = 2;
                    } else if (bearing > 15 && bearing <= 30) {
                        Direction = 3;
                    } else if (bearing < -30 && bearing >= -100) {
                        Direction = 5;
                    } else if (bearing > 30 && bearing <= 100) {
                        Direction = 4;
                    } else if (bearing < -100) {
                        Direction = 6;
                    } else {
                        Direction = 7;
                    }
                    lastNavi.direction = Direction;
                    System.out.println(sizeMinusOne.toString());
                }
                else if(g.nodeMap.get(route.get(judge)).wayName==null){
                    if(!curNode.wayName.equals(NavigationDirection.UNKNOWN_ROAD)){
                        lastNavi.distance += g.distance(curId, lastNode.id);
                        lastNode = curNode;
                    }
                    else{
                        //lastNavi.distance += g.distance(curId, lastNode.id);
                        result.add(new NavigationDirection());
                        NavigationDirection sizeMinusOne = result.get(result.size() - 1);
                        sizeMinusOne.distance = lastNavi.distance;
                        sizeMinusOne.way = lastNavi.way;
                        sizeMinusOne.direction = lastNavi.direction;
                        lastNavi = new NavigationDirection();
                        lastNavi.distance = g.distance(curId, lastNode.id);
                        lastNavi.way = NavigationDirection.UNKNOWN_ROAD;
                        int Direction;
                        double bearing = g.bearing(lastNode.id, curId);
                        if (bearing >= -15 && bearing <= 15) {
                            Direction = 1;
                        } else if (bearing >= -30 && bearing < -15) {
                            Direction = 2;
                        } else if (bearing > 15 && bearing <= 30) {
                            Direction = 3;
                        } else if (bearing < -30 && bearing >= -100) {
                            Direction = 5;
                        } else if (bearing > 30 && bearing <= 100) {
                            Direction = 4;
                        } else if (bearing < -100) {
                            Direction = 6;
                        } else {
                            Direction = 7;
                        }
                        lastNavi.direction = Direction;
                        lastNode = curNode;
                        System.out.println(sizeMinusOne.toString());
                    }
                }
                else if(!g.nodeMap.get(route.get(judge)).wayName.equals(curNode.wayName)){
                    lastNavi.distance += g.distance(curId, lastNode.id);
                    lastNode = curNode;
                }
                else {
                    //lastNavi.distance += g.distance(curId, lastNode.id);
                    //lastNode = curNode;
                    result.add(new NavigationDirection());
                    NavigationDirection sizeMinusOne = result.get(result.size() - 1);
                    sizeMinusOne.distance = lastNavi.distance;
                    sizeMinusOne.way = lastNavi.way;
                    sizeMinusOne.direction = lastNavi.direction;
                    lastNavi = new NavigationDirection();
                    lastNavi.distance = g.distance(curId, lastNode.id);
                    lastNavi.way = g.nodeMap.get(curId).wayName;
                    int Direction;
                    double bearing = g.bearing(lastNode.id, curId);
                    if (bearing >= -15 && bearing <= 15) {
                        Direction = 1;
                    } else if (bearing >= -30 && bearing < -15) {
                        Direction = 2;
                    } else if (bearing > 15 && bearing <= 30) {
                        Direction = 3;
                    } else if (bearing < -30 && bearing >= -100) {
                        Direction = 4;
                    } else if (bearing > 30 && bearing <= 100) {
                        Direction = 5;
                    } else if (bearing < -100) {
                        Direction = 6;
                    } else {
                        Direction = 7;
                    }
                    lastNavi.direction = Direction;
                    lastNode = curNode;
                    System.out.println(sizeMinusOne.toString());
                }
            }
        }
        result.add(lastNavi);
        System.out.println(result.get(result.size()-1).toString());
        return result;
    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "unknown road";
        
        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
