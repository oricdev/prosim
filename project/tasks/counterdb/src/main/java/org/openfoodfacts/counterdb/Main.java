/*
 * PROSIM (PROduct SIMilarity): backend engine for comparing OpenFoodFacts products 
 * by pairs based on their score (Nutrition Score, Nova Classification, etc.).
 * Results are stored in a Mongo-Database.
 *
 * Url: https://offmatch.blogspot.com/
 * Author/Developer: Olivier Richard (oric_dev@iznogoud.neomailbox.ch)
 * License: GNU Affero General Public License v3.0
 * License url: https://github.com/oricdev/prosim/blob/master/LICENSE
 */
package org.openfoodfacts.counterdb;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bson.Document;
import org.openfoodfacts.mongo.MongoMgr;
import org.openfoodfacts.utils.CfgMgr;

/**
 *
 * useful links: :gson: https://github.com/google/gson :gson api:
 * http://www.javadoc.io/doc/com.google.code.gson/gson/2.8.5 :gson sample
 * streaming: https://sites.google.com/site/gson/streaming :log4j:
 * https://www.mkyong.com/logging/log4j-hello-world-example/
 */
public class Main {

    // Tags in config.xml file
    final static String CONF_PATH_TO_ROOT = "path_to_root";

    // Access to Mongo-Db
    final static String CONF_OFF_ORIG_HOST = "off_orig_host";
    final static String CONF_OFF_ORIG_PORT = "off_orig_port";
    final static String CONF_OFF_ORIG_LOGIN = "off_orig_login";
    final static String CONF_OFF_ORIG_PWD = "off_orig_pwd";
    final static String CONF_OFF_ORIG_DBNAME = "off_orig_db_name";
    final static String CONF_OFF_ORIG_DBCOLL = "off_orig_db_collection";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Main.dbConnect();

        long nb_products = MongoMgr.getCountProducts_Prosim();
        System.out.println("Number of products in the Db = " + nb_products);
        // Read all products and store them into JSON file
        MongoCollection<Document> cursor_products = MongoMgr.getAllProducts(CfgMgr.getConf(CONF_OFF_ORIG_DBNAME), CfgMgr.getConf(CONF_OFF_ORIG_DBCOLL));

        long nb_intersections_in_db = countIntersections(cursor_products);
        MongoMgr.disconnect();

    }

    private static long countIntersections(MongoCollection<Document> cursor_prosim_products) {
        long counter = 0;
        long nb_intersections = 0;
        //HashMap<Short, HashMap<Short, String[]>> similarity;
        Document similarity;

        for (Iterator<Document> productIterator = cursor_prosim_products.find().iterator(); productIterator.hasNext(); counter++) {
            Document mongoDocument = productIterator.next();

            if (mongoDocument.containsKey("similarity")) {
                similarity = (Document) mongoDocument.get("similarity");
                for (Iterator sim_percentage = similarity.keySet().iterator(); sim_percentage.hasNext();) {
                    Document percentage = (Document) similarity.get(sim_percentage.next());

                    for (Iterator sim_score = percentage.keySet().iterator(); sim_score.hasNext();) {
                        List score = (ArrayList) percentage.get(sim_score.next());
                        nb_intersections += score.size();
                    }

                }
            }
            if (counter % 1000 == 0) {
                System.out.println("nb of products parsed " + counter + "\tintersections: " + nb_intersections);
            }
        }
        System.out.println();
        System.out.println("TOTAL nb of products parsed " + counter + "\tintersections: " + nb_intersections);
        return nb_intersections;
    }

    private static void dbConnect() {
        String mongo_host = CfgMgr.getConf(CONF_OFF_ORIG_HOST);
        int mongo_port = (CfgMgr.getConf(CONF_OFF_ORIG_PORT).equals("")) ? 0 : Integer.valueOf(CfgMgr.getConf(CONF_OFF_ORIG_PORT));
        String mongo_db = CfgMgr.getConf(CONF_OFF_ORIG_DBNAME);
        String mongo_login = CfgMgr.getConf(CONF_OFF_ORIG_LOGIN);
        String mongo_pwd = CfgMgr.getConf(CONF_OFF_ORIG_PWD);

        MongoMgr.connect(mongo_host, mongo_port, mongo_login, mongo_pwd, mongo_db, "", false);
    }

}
