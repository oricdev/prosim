package org.openfoodfacts.intersect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.openfoodfacts.aggregator.ProductAggregator;
import org.openfoodfacts.entities.Prosim;
import org.openfoodfacts.mongo.MongoMgr;
import org.openfoodfacts.preparer.JsonTools;
import org.openfoodfacts.products.IProduct;
import org.openfoodfacts.products.Product;
import org.openfoodfacts.products.ProductExt;
import org.openfoodfacts.utils.CfgMgr;
import org.openfoodfacts.utils.EnumFileType;
import org.openfoodfacts.utils.FileMgr;
import org.openfoodfacts.utils.Tuple;

/**
 *
 * @author oric
 */
public class Main {

    final static Logger logger = Logger.getLogger(org.openfoodfacts.intersect.Main.class);

    final static String MAX_DB_SIZE_GB = "max_db_size_gigabytes";
    // Tags in config.xml file
    final static String CONF_SIMILARITY_MIN_PERCENTAGE = "similarity_min_percentage";
    // This mode is VERY IMPORTANT and should be handled with great care.
    // First thing to understand is that the current process inserts into the Mongo db a subset (data-matrix set) of the overall grid of products.
    // Hence a product in db with similarity field set (similar products) may be inserted again the db with extra similar products coming from the partial intersection process.
    // In this case, we may want:
    // - mode QUICK_INIT: to accelerate the import process: this mode ONLY merges the content of the "similarity" data when a product code (with similar products) exists already in the database, while updating this entry with a product to import (same code, but extra similar products attached). All difference in standard OpenFoodFacts.org fields will be ignored (countries_tags, categories_tags, etc.)
    // - mode FULL_CHECK_UPDATE: to ensure the overall consistency of the data in the db. Hence , in addition to what is performed in the QUICK_INIT mode, standard OFF data are also checked and updated if necessary.
    final static String CONF_MONGO_IMPORT_MODE = "mongo_import_mode";
    final static String CONF_STOP_WHEN_MIN_DATA_REACHED = "stop_when_min_data_reached";
    final static String CONF_PATH_TO_ROOT = "path_to_root";
    final static String CONF_PATH_LOGFILE_NAME = "path_to_logfile_name";
    final static String CONF_PATH_INCOMING_DATA = "path_to_incoming_data";
    final static String CONF_PATH_RESULTS = "path_absolute_output_results";
    final static String CONF_BACKUP_DIRNAME = "backup_dirname";
    final static String CONF_OFF_DEST_HOST = "off_dest_host";
    final static String CONF_OFF_DEST_PORT = "off_dest_port";
    final static String CONF_OFF_DEST_LOGIN = "off_dest_login";
    final static String CONF_OFF_DEST_PWD = "off_dest_pwd";
    final static String CONF_OFF_DEST_DBNAME = "off_dest_db_name";

    public static void main(String[] args) {
        // init log file: NOT WORKING with relative path?
        /* FileAppender f_appender = (FileAppender)Logger.getRootLogger().getAppender("file");
        String fname_log = CfgMgr.getConf(CONF_PATH_TO_ROOT).concat("/").concat(CfgMgr.getConf(CONF_PATH_LOGFILE_NAME));
        f_appender.setFile(fname_log);
        */
        
        logger.info("Process Intersect started..");
        logger.info("Log level is " + logger.getParent().getLevel().toString().toUpperCase());

        //Main.dropDb();

        logger.info("*****************");
        logger.info("retrieving next data set..");

        Main.dbConnect();

        Float max_dbSize = Float.valueOf(CfgMgr.getConf(MAX_DB_SIZE_GB));
        double dbSize = MongoMgr.getDbSize();
        float sizeInGB = ((float) dbSize) / 1024 / 1024 / 1024;
        logger.info("Database SIZE is " + sizeInGB + " GB.");

        File dirDataset = Main.getPathNextDataSet(Integer.valueOf(CfgMgr.getConf(CONF_STOP_WHEN_MIN_DATA_REACHED)));
        while (dirDataset != null && sizeInGB < max_dbSize) {
            Tuple<File, File> dataset = Main.getNextDataPackage(dirDataset);
            List<IProduct> productsExt = intersectMatrixProducts(dataset);
            logger.info(productsExt.size() + " product-intersections are about to be exported in the Mongo-database..");
            //saveResults(productsExt, dirDataset.getName());
            feedDb(productsExt);
            //back package
            FileMgr.backup(CfgMgr.getConf(CONF_PATH_TO_ROOT).concat("/").concat(CfgMgr.getConf(CONF_BACKUP_DIRNAME)), dirDataset);
            // proceed with next file
            logger.info("*****************");
            logger.info("retrieving next data set..");
            dirDataset = Main.getPathNextDataSet(Integer.valueOf(CfgMgr.getConf(CONF_STOP_WHEN_MIN_DATA_REACHED)));

            dbSize = MongoMgr.getDbSize();
            sizeInGB = ((float) dbSize) / 1024 / 1024 / 1024;
            logger.info("Database SIZE is " + sizeInGB + " GB.");
        }

        MongoMgr.disconnect();

        if (sizeInGB >= max_dbSize) {
            logger.warn("MAX DB Size of " + max_dbSize + " GB reached! No further datasets will be processed. Please make free space AND update config.xml accordingly with the new extended size!");
        }
        logger.info("Process Intersect finished.");
    }

    private static File getPathNextDataSet(int min_data_available) {
        String path_incoming_data = CfgMgr.getConf(CONF_PATH_INCOMING_DATA);
        if (path_incoming_data.equals("")) {
            logger.error("check your configuration file (variable " + CONF_PATH_INCOMING_DATA + "): wrong location for incoming data ('" + path_incoming_data + "')");
            return null;
        }
        String full_path = CfgMgr.getConf(CONF_PATH_TO_ROOT).concat("/").concat(path_incoming_data);
        return FileMgr.getOldestDataset(full_path, min_data_available);
    }

    /*
    * Returns the oldest modified data directory found
     */
    private static Tuple<File, File> getNextDataPackage(File dirDataset) {
        Tuple<File, File> dataset = null;

        if (dirDataset != null) {
            File[] fileDatasets = FileMgr.getAllFilesInDirectory(dirDataset.getAbsolutePath(), EnumFileType.FILE);
            int nb_files = fileDatasets.length;
            switch (nb_files) {
                case 0:
                    logger.error("no matrix-data files could be found in '" + dirDataset.getAbsolutePath() + "'");
                    logger.error("Process aborted!");
                    break;
                case 2:
                    logger.info("matrix-data files retrieved successfully in '" + dirDataset.getAbsolutePath() + "' (2 files)");
                    dataset = new Tuple<>(fileDatasets[0], fileDatasets[1]);
                    break;
                default:
                    logger.error("Exactly 2 matrix-data files should co-exist in package '" + dirDataset.getAbsolutePath() + "' and " + nb_files + " was/were found!");
                    logger.error("Process aborted!");
            }
        }
        return dataset;
    }

    private static void dropDb() {
        String mongo_host = CfgMgr.getConf(CONF_OFF_DEST_HOST);
        int mongo_port = (CfgMgr.getConf(CONF_OFF_DEST_PORT).equals("")) ? 0 : Integer.valueOf(CfgMgr.getConf(CONF_OFF_DEST_PORT));
        String mongo_db = CfgMgr.getConf(CONF_OFF_DEST_DBNAME);

        MongoMgr.dropDb(mongo_host, mongo_port, mongo_db);
    }

    private static void saveResults(List<IProduct> products, String out_subdir) {
        logger.info("saving results of all intersections for this dataset:");
        String cfg_result_path = CfgMgr.getConf(CONF_PATH_RESULTS);
        String result_full_path = cfg_result_path + "/" + out_subdir;
        FileMgr.mkdir(result_full_path);
        JsonTools.writeJsonStream(result_full_path, "intersected_products.json", products);
    }

    private static List<IProduct> intersectMatrixProducts(Tuple<File, File> dataset) {
        List<IProduct> products_intersected = new ArrayList<>();
        short min_percentage = Short.valueOf(CfgMgr.getConf(CONF_SIMILARITY_MIN_PERCENTAGE));
        logger.info("starting intersecting (min. percentage is " + min_percentage + "%)");
        // statistics
        long stats_nb_intersects = 0;
        long stats_nb_valid_intersects = 0;
        long stats_empty_nutrition_score = 0;
        long stats_empty_nutriments = 0;
        long stats_below_min_percentage = 0;
        long stats_nb_ignored = 0;

        try {
            // reading of the matrix
            FileInputStream istr_A = new FileInputStream(dataset.x.getAbsolutePath());
            FileInputStream istr_B = new FileInputStream(dataset.y.getAbsolutePath());
            List<Product> products_A = JsonTools.readJsonStream(istr_A);
            List<Product> products_B = JsonTools.readJsonStream(istr_B);

            for (int i = 0; i < products_A.size(); i++) {
                for (int j = 0; j < products_B.size(); j++) {
                    ProductExt prodExt_A = new ProductExt(products_A.get(i));
                    ProductExt prodExt_B = new ProductExt(products_B.get(j));
                    if (!prodExt_A.getCode().equals(prodExt_B.getCode())) {
                        // non-exclusive statistics ( a product may have nor nutrition code neither nutriments)
                        stats_empty_nutrition_score += prodExt_A.getNutrition_score() == null ? 1 : 0;
                        stats_empty_nutrition_score += prodExt_B.getNutrition_score() == null ? 1 : 0;
                        stats_empty_nutriments += (null == prodExt_A.getCategories_tags() || prodExt_A.getCategories_tags().isEmpty()) ? 1 : 0;
                        stats_empty_nutriments += (null == prodExt_B.getCategories_tags() || prodExt_B.getCategories_tags().isEmpty()) ? 1 : 0;

                        if (prodExt_A.getNutrition_score() != null
                                && prodExt_B.getNutrition_score() != null) {
                            prodExt_A.computeSimilarity(prodExt_B);
                            prodExt_B.computeSimilarity(prodExt_A);
                            if (prodExt_A.getSimilarity_with_product() >= min_percentage) {
                                products_intersected.add(prodExt_A);
                                stats_nb_valid_intersects++;
                            } else {
                                stats_below_min_percentage++;
                                stats_nb_ignored++;
                            }
                            if (prodExt_B.getSimilarity_with_product() >= min_percentage) {
                                products_intersected.add(prodExt_B);
                                stats_nb_valid_intersects++;
                            } else {
                                stats_below_min_percentage++;
                                stats_nb_ignored++;
                            }
                        } else {
                            stats_nb_ignored++;
                        }
                    }
                    stats_nb_intersects++;
                }
            }

        } catch (FileNotFoundException ex) {
        }

        logger.info("*********************************************************");
        logger.info("RESULTS OF INTERSECTIONS:");
        logger.info("    number of intersections:          " + (new DecimalFormat("###,###")).format(stats_nb_intersects));
        logger.info("    number of VALID intersections: ** " + (new DecimalFormat("###,###")).format(stats_nb_valid_intersects) + " **");
        logger.info("  IGNORED:                           [" + (new DecimalFormat("###,###")).format(stats_nb_ignored) + "]");
        logger.info("    products below min. percentage:   " + (new DecimalFormat("###,###")).format(stats_below_min_percentage));
        logger.info("    products with NO nutrition score: " + (new DecimalFormat("###,###")).format(stats_empty_nutrition_score));
        logger.info("    products with NO categories:      " + (new DecimalFormat("###,###")).format(stats_empty_nutriments));
        logger.info("*********************************************************");
        return products_intersected;
    }

    private static void dbConnect() {
        String mongo_host = CfgMgr.getConf(CONF_OFF_DEST_HOST);
        int mongo_port = (CfgMgr.getConf(CONF_OFF_DEST_PORT).equals("")) ? 0 : Integer.valueOf(CfgMgr.getConf(CONF_OFF_DEST_PORT));
        String mongo_db = CfgMgr.getConf(CONF_OFF_DEST_DBNAME);
        String mongo_login = CfgMgr.getConf(CONF_OFF_DEST_LOGIN);
        String mongo_pwd = CfgMgr.getConf(CONF_OFF_DEST_PWD);
        String mongo_mode = CfgMgr.getConf(CONF_MONGO_IMPORT_MODE);

        MongoMgr.connect(mongo_host, mongo_port, mongo_login, mongo_pwd, mongo_db, mongo_mode);
    }

    private static void feedDb(List<IProduct> productsExt) {
        // create db if does not exist yet!
        String mongo_host = CfgMgr.getConf(CONF_OFF_DEST_HOST);
        int mongo_port = (CfgMgr.getConf(CONF_OFF_DEST_PORT).equals("")) ? 0 : Integer.valueOf(CfgMgr.getConf(CONF_OFF_DEST_PORT));
        String mongo_db = CfgMgr.getConf(CONF_OFF_DEST_DBNAME);
        String mongo_login = CfgMgr.getConf(CONF_OFF_DEST_LOGIN);
        String mongo_pwd = CfgMgr.getConf(CONF_OFF_DEST_PWD);
        String mongo_mode = CfgMgr.getConf(CONF_MONGO_IMPORT_MODE);

        // Browse the matrix of products and aggregate the similarity part (similar products grouped by percentage of similarity and nutrition-score)
        ProductAggregator productsAggregator = new ProductAggregator();
        // Aggregate products into Prosim products ready to merge its entries (similarity) with those stored in the Mongo-Db
        for (IProduct product : productsExt) {
            Prosim prosim = new Prosim(product);
            productsAggregator.aggregate(prosim);
        }
        logger.info("After REDUCTION, " + productsAggregator.products.keySet().size() + " products will be inserted/merged in the Mongo-Db.");
        MongoMgr.connect(mongo_host, mongo_port, mongo_login, mongo_pwd, mongo_db, mongo_mode);
        logger.info("BEFORE insertion/merge: db <" + mongo_db + "> holds " + MongoMgr.getCountProducts() + " products already.");
        // Aggregate once again with entries found in Mongo-Db (merge of similarity fields)
        productsAggregator.products.keySet().forEach((code_product) -> {
            if (!MongoMgr.existsProduct(code_product)) {
                MongoMgr.saveProduct(productsAggregator.products.get(code_product));
                logger.debug("product <" + code_product + "> created.");
            } else {
                // merge similarity of entry found in db for this product with current one
                Prosim prosim_db = MongoMgr.getProduct(code_product);
                productsAggregator.aggregate(prosim_db);
                MongoMgr.saveProduct(productsAggregator.products.get(code_product));
                logger.debug("product <" + code_product + "> updated.");
            }
        });
        logger.info("AFTER insertion/merge: db <" + mongo_db + "> holds " + MongoMgr.getCountProducts() + " products.");
    }
}
