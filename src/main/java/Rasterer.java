import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
    public class Rasterer {


    public Rasterer() {
        // YOUR CODE HERE
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        double LonDPP = (params.get("lrlon") - params.get("ullon"))/params.get("w") * 288200;
        int depth = 0;
        double curLonDPP = (122.29980 - 122.21191) / 256 *288200;
        while(curLonDPP>LonDPP){
            depth+=1;
            curLonDPP/=2;
            if(depth==7)
                break;
        }
        double intervalLon = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON)/ pow(2,depth);
        double intervalLat = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT)/ pow(2,depth);
        int Lon_Down = 0;
        int Lon_Up = 0;
        int Lat_Down = 0;
        int Lat_Up = 0;
        double curLon = MapServer.ROOT_ULLON;
        double curLat = MapServer.ROOT_ULLAT;
        double raster_ul_lon = 0;
        double raster_ul_lat = 0;
        double raster_lr_lon = 0;
        double raster_lr_lat = 0;
        boolean query_success = true;

//        for(int i = 0; i<(int)pow(2,depth); i++){
//            if(params.get("ullat")>=MapServer.ROOT_ULLAT){
//                Lat_Down = 0;
//                raster_ul_lat = MapServer.ROOT_ULLAT;
//            }
//            if(params.get("lrlat")<=MapServer.ROOT_LRLAT){
//                Lat_Up = (int)pow(2,depth) - 1;
//                raster_lr_lat = MapServer.ROOT_LRLAT;
//            }
//            if(curLat>=params.get("ullat")&&curLat-intervalLat<=params.get("ullat")) {
//                Lat_Down = i;
//                raster_ul_lat = curLat;
//            }
//            if(curLat>=params.get("lrlat")&&curLat-intervalLat<=params.get("ullat")) {
//                Lat_Up = i;
//                raster_lr_lat = curLat - intervalLat;
//            }
//            if(params.get("ullon")<=MapServer.ROOT_ULLON) {
//                Lon_Down = 0;
//                raster_ul_lon = MapServer.ROOT_ULLON;
//            }
//            if(params.get("lrlon")>=MapServer.ROOT_LRLON){
//                Lon_Up = (int)pow(2,depth) - 1;
//                raster_lr_lon = MapServer.ROOT_LRLON;
//            }
//            if(curLon<=params.get("ullon")&&curLon+intervalLon>=params.get("ullon")) {
//                Lon_Down = i;
//                raster_ul_lon = curLon;
//            }
//            if(curLon<=params.get("lrlon")&&curLon+intervalLon>=params.get("lrlon")) {
//                Lon_Up = i;
//                raster_lr_lon = curLon + intervalLon;
//            }
//            curLon+=intervalLon;
//            curLat-=intervalLat;
//        }
        if(params.get("ullat")>=MapServer.ROOT_ULLAT){
            Lat_Down = 0;
            raster_ul_lat = MapServer.ROOT_ULLAT;
        }
        else{
            Lat_Down = (int)((MapServer.ROOT_ULLAT - params.get("ullat"))/intervalLat);
            raster_ul_lat = MapServer.ROOT_ULLAT - intervalLat * Lat_Down;
        }
        if(params.get("lrlat")<=MapServer.ROOT_LRLAT){
                Lat_Up = (int)pow(2,depth) - 1;
                raster_lr_lat = MapServer.ROOT_LRLAT;
        }
        else{
            Lat_Up = (int)((MapServer.ROOT_ULLAT - params.get("lrlat"))/intervalLat);
            raster_lr_lat = MapServer.ROOT_ULLAT - Lat_Up * intervalLat - intervalLat;
        }
        if(params.get("ullon")<=MapServer.ROOT_ULLON) {
                Lon_Down = 0;
                raster_ul_lon = MapServer.ROOT_ULLON;
        }
        else{
            Lon_Down = (int)((params.get("ullon") - MapServer.ROOT_ULLON)/intervalLon);
            raster_ul_lon = MapServer.ROOT_ULLON + Lon_Down * intervalLon;
        }
        if(params.get("lrlon")>=MapServer.ROOT_LRLON){
                Lon_Up = (int)pow(2,depth) - 1;
                raster_lr_lon = MapServer.ROOT_LRLON;
        }
        else{
            Lon_Up = (int)((params.get("lrlon") - MapServer.ROOT_ULLON)/intervalLon);
            raster_lr_lon = MapServer.ROOT_ULLON + Lon_Up * intervalLon + intervalLon;
        }

        //System.out.println(Lon_Down+" "+Lon_Up+" "+Lat_Down+" "+Lat_Up);




        results.put("raster_ul_lon", raster_ul_lon);
        results.put("raster_lr_lon", raster_lr_lon);
        results.put("raster_ul_lat", raster_ul_lat);
        results.put("raster_lr_lat", raster_lr_lat);
        results.put("depth", depth);
        results.put("query_success", query_success);
        String[][] render_grid = new String[Lat_Up-Lat_Down+1][Lon_Up-Lon_Down+1];
        //System.out.println(Lon_Down+" "+Lon_Up+" "+Lat_Down+" "+Lat_Up);
        for(int i=Lat_Down;i<=Lat_Up;i++) {
            for (int j = Lon_Down; j <= Lon_Up; j++) {
                render_grid[i - Lat_Down][j - Lon_Down] = "d" + depth + "_x" + j + "_y" + i+".png";
            }
        }
        results.put("render_grid", render_grid);
        return results;
    }

}
