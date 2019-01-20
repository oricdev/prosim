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
package org.openfoodfacts.feeders;

import com.mongodb.client.MongoCollection;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.openfoodfacts.mongo.MongoMgr;
import org.openfoodfacts.utils.JsonTools;
import org.openfoodfacts.utils.CfgMgr;
import org.openfoodfacts.utils.FileMgr;

/**
 *
 * useful links: :gson: https://github.com/google/gson :gson api:
 * http://www.javadoc.io/doc/com.google.code.gson/gson/2.8.5 :gson sample
 * streaming: https://sites.google.com/site/gson/streaming :log4j:
 * https://www.mkyong.com/logging/log4j-hello-world-example/
 */
public class Main {

    final static Logger logger = Logger.getLogger(org.openfoodfacts.feeders.Main.class);
    // Tags in config.xml file
    final static String CONF_PATH_TO_ROOT = "path_to_root";
    final static String CONF_PATH_LOGFILE_NAME = "path_to_logfile_name";
    final static String CONF_PATH_OUT_FEEDERS = "out_path_feeders";
    final static String CONF_FILE_FEEDER = "out_feeder_filename";
    final static String CONF_LOCK_FEEDER_FILENAME = "out_feeder_lock_filename";
    
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
        /// init log file: NOT WORKING with relative path?
        /* FileAppender f_appender = (FileAppender)Logger.getRootLogger().getAppender("file");
        String fname_log = CfgMgr.getConf(CONF_PATH_TO_ROOT).concat("/").concat(CfgMgr.getConf(CONF_PATH_LOGFILE_NAME));
        f_appender.setFile(fname_log);
         */

        logger.info("Process Feeder_1 started..");
        logger.info("Log level is " + logger.getParent().getLevel().toString().toUpperCase());

        Main.dbConnect();

        long nb_products = MongoMgr.getCountProducts_products();
        logger.info("Number of products in the Db = " + nb_products);
        // Read all products and store them into JSON file
        MongoCollection<Document> cursor_products = MongoMgr.getAllProducts(CfgMgr.getConf(CONF_OFF_ORIG_DBNAME), CfgMgr.getConf(CONF_OFF_ORIG_DBCOLL));
        String dir_path = CfgMgr.getConf(CONF_PATH_TO_ROOT) + "/" + CfgMgr.getConf(CONF_PATH_OUT_FEEDERS);
        String json_filename = CfgMgr.getConf(CONF_FILE_FEEDER);
        JsonTools.writeJsonStreamWithMongoCursor(dir_path, json_filename, cursor_products);
        MongoMgr.disconnect();
        
        // Create a lock file when finished
        logger.info("creating lock file...");
        String lock_filename = CfgMgr.getConf(CONF_LOCK_FEEDER_FILENAME);
        FileMgr.createLockFile(dir_path, lock_filename);
        
        logger.info("Process Feeder_1 finished.");
    }

    private static void dbConnect() {
        String mongo_host = CfgMgr.getConf(CONF_OFF_ORIG_HOST);
        int mongo_port = (CfgMgr.getConf(CONF_OFF_ORIG_PORT).equals("")) ? 0 : Integer.valueOf(CfgMgr.getConf(CONF_OFF_ORIG_PORT));
        String mongo_db = CfgMgr.getConf(CONF_OFF_ORIG_DBNAME);
        String mongo_login = CfgMgr.getConf(CONF_OFF_ORIG_LOGIN);
        String mongo_pwd = CfgMgr.getConf(CONF_OFF_ORIG_PWD);

        MongoMgr.connect(mongo_host, mongo_port, mongo_login, mongo_pwd, mongo_db, "", true);
    }

}
