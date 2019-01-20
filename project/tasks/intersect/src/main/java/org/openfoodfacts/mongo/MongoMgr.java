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

 /*
 * useful links here:
 * https://docs.mongodb.com/manual/tutorial/
 * Indexes: https://stackoverflow.com/questions/44413520/how-to-make-indexes-and-different-unique-indexes-in-morphia-java
 * Morphia API: http://mongodb.github.io/morphia/
 */
 /*
 * useful links:
 * querying with Morphia: http://mongodb.github.io/morphia/1.0/guides/querying/
 */
package org.openfoodfacts.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import java.util.List;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.openfoodfacts.entities.Prosim;
import org.openfoodfacts.products.products;


public class MongoMgr {

    final static Logger logger = Logger.getLogger(MongoMgr.class);
    static EnumMongoImportMode importMode = null;
    static String host = null;
    static int port = -1;
    static String dbname = null;
    static String login = null;
    static String pwd = null;
    static MongoClient mongo = null;
    static Datastore ds = null;
    static Morphia morphia = null;

    public static void connect() {
        if (MongoMgr.mongo == null) {
            logger.error("Connection was reset and don't know which host, port, database to connect to.");
        } else {
            MongoMgr.ds = MongoMgr.morphia.createDatastore(MongoMgr.mongo, MongoMgr.dbname);
        }
    }

    public static void connect(String host, int port, String login, String pwd, String dbname, String mode, boolean ensureIndexes) {
        if (mode.isEmpty()) {
            MongoMgr.importMode = EnumMongoImportMode.QUICK_INIT;
        } else {
            MongoMgr.importMode = EnumMongoImportMode.valueOf(mode);
        }
        logger.info("****************************************************");
        logger.info("Mongo Import Mode has been set to <" + mode + ">");
        logger.info("****************************************************");
        MongoMgr.host = host;
        MongoMgr.port = port;
        MongoMgr.login = login;
        MongoMgr.pwd = pwd;
        MongoMgr.dbname = dbname;
        StringBuilder mongo_connect_string = new StringBuilder("mongodb://");
        if (!login.equals("") && !pwd.equals("")) {
            mongo_connect_string.append(login).append(":").append(pwd).append("@");
        }
        mongo_connect_string.append(host).append("/").append(dbname);
        if (port > 0) {
            MongoMgr.mongo = new MongoClient(new MongoClientURI(mongo_connect_string.toString() + ":" + port));
        } else {
            MongoMgr.mongo = new MongoClient(new MongoClientURI(mongo_connect_string.toString()));
        }

        MongoMgr.morphia = new Morphia();
        MongoMgr.ds = MongoMgr.morphia.createDatastore(MongoMgr.mongo, MongoMgr.dbname);

        MongoMgr.morphia.mapPackage("org.openfoodfacts.entities");
        if (ensureIndexes) {
            // create indexes if unavailable
            MongoMgr.ds.ensureIndexes();
        }
    }

    public static void disconnect() {
        MongoMgr.mongo.close();
        MongoMgr.ds = null;
    }

    public static boolean existsDb() {
        // WARNING: use MongoClient if authentication is required! (cf. http://www.mkyong.com/mongodb/java-mongodb-hello-world-example/)
        List<String> dbs = MongoMgr.mongo.getDatabaseNames();
        boolean exists = dbs == null ? false : dbs.contains(dbname);
        if (!exists) {
            logger.debug("db <" + dbname + "> does not exist! Existing dbs are " + dbs.toString());
        }
        return exists;
    }

    public static double getDbSize() {
        if (MongoMgr.ds.getDB().getStats().containsField("fileSize")) {
            return Double.valueOf(MongoMgr.ds.getDB().getStats().get("fileSize").toString());
        }
        return 0;
    }

    public static void createDb(String host, int port, String dbname) {
        Mongo mongo = new Mongo(host, port);
        // if database doesn't exists, MongoDB will create it for you
        mongo.getDB(dbname);
        mongo.close();
        logger.info("db <" + dbname + "> has been created.");
    }

    public static Prosim getProduct(String code) {
        Prosim productInDb = MongoMgr.ds.createQuery(Prosim.class).field("code").equal(code).get();
        return productInDb;
    }

    public static void saveProduct(Prosim product) {
        MongoMgr.ds.save(product);
    }

    public static void deleteProduct(String code) {

    }

    public static void dropDb(String host, int port, String dbname) {
        MongoClient mongo = new MongoClient(host, port);
        mongo.dropDatabase(dbname);
        mongo.close();
        logger.debug("database <" + dbname + "> has been SUCCESSFULLY deleted.");
    }

    public static MongoCollection<Document> getAllProducts(String dbname, String dbcoll) {
        MongoCollection mongoColl = MongoMgr.mongo.getDatabase(dbname).getCollection(dbcoll);
        return mongoColl;
    }

    public static boolean existsProduct(String code) {
        Query<Prosim> query = MongoMgr.ds.createQuery(Prosim.class);
        query.and(
                query.criteria("code").equal(code)
        );

        return ds.getCount(query) > 0;
    }

    /**
     * Number of products in database
     *
     * @return
     */
    public static long getCountProducts_Prosim() {
        Query<Prosim> query = MongoMgr.ds.createQuery(Prosim.class);
        return MongoMgr.ds.getCount(query);
    }

    public static double getStorageSize() throws ClassCastException {
        return (Double) MongoMgr.ds.getDB().getStats().get("dataSize");
    }
    /**
     * Number of products in database
     *
     * @return
     */
    public static long getCountProducts_products() {
        Query<products> query = MongoMgr.ds.createQuery(products.class);
        return MongoMgr.ds.getCount(query);
    }
}
