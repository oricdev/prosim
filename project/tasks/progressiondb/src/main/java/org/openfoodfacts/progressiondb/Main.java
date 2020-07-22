
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
package org.openfoodfacts.progressiondb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.openfoodfacts.utils.JsonTools;
import org.openfoodfacts.utils.CfgMgr;
import org.openfoodfacts.utils.Tuple;

/**
 *
 * This is not used anymore since it has been replaced by the dbStats task.
 */
public class Main {

    // Tags in config.xml file
    final static String CONF_PATH_TO_ROOT = "path_to_root";
    final static String CONF_DB_NAME = "db_name";
    final static String CONF_TASK_PREPARER_PATH = "task_preparer_path";
    // Used to retrieve the matrix height used by the Preparer task
    //final static String CONF_PREPARER_CONFIG_FILE = "preparer_config_file";
    final static String CONF_PREPARER_PROGRESSION_FILE = "preparer_progression_file";
    final static String CONF_OUT_PATH_FEEDERS = "out_path_feeders_json";
    final static String CONF_OUT_WIDTH_DIMENSION_PRODUCTS = "out_w_products";
    final static String CONF_OUT_HEIGHT_DIMENSION_PRODUCTS = "out_h_products";

    // Tags in progress.xml file
    final static String PROGRESS_LAST_CODE_ALL_PRODUCTS = "last_intersect_code_all_products";
    final static String PROGRESS_LAST_CODE_UPDATED_PRODUCTS = "last_intersect_code_to_be_inserted";

    /**
     * computes progression based on barcode width/height stored in
     * progression.xml of preparer task Formula: [((h.x - prep.height) * w.y) +
     * (prep.height * w.x) ] / (w.y * h.y) where prep.height= height of matrix
     * stored in config.xml of preparer task (copied in config.xml of current
     * task for simplification)
     *
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String dbName = CfgMgr.getConf(CONF_DB_NAME);
        System.out.println("computing progression of database <" + dbName + ">");

        // read progress.xml file to get data about the last extracted file (last intersection block created)
        // ..last barcode in ALL products processed in previous batch (w=width in Matrix)
        String last_code_w = getProgress(PROGRESS_LAST_CODE_ALL_PRODUCTS);
        // ..last barcode of UPDATED products processed in previous batch (h=height in Matrix)
        String last_code_h = getProgress(PROGRESS_LAST_CODE_UPDATED_PRODUCTS);
        System.out.println("last_code_w [all_products.json] = " + last_code_w);
        System.out.println("last_code_h [updated_products.json] = " + last_code_h);

        String path_json_matrix_w = CfgMgr.getConf(CONF_PATH_TO_ROOT) + "/" + CfgMgr.getConf(CONF_OUT_PATH_FEEDERS) + "/" + CfgMgr.getConf(CONF_OUT_WIDTH_DIMENSION_PRODUCTS);
        String path_json_matrix_h = CfgMgr.getConf(CONF_PATH_TO_ROOT) + "/" + CfgMgr.getConf(CONF_OUT_PATH_FEEDERS) + "/" + CfgMgr.getConf(CONF_OUT_HEIGHT_DIMENSION_PRODUCTS);
        Tuple<Long, Long> position_w = getCursorPositionInJsonMatrix(path_json_matrix_w, last_code_w);
        Tuple<Long, Long> position_h = getCursorPositionInJsonMatrix(path_json_matrix_h, last_code_h);
        float progression = (Float.valueOf(position_h.x * position_w.y) ) / (Float.valueOf(position_w.y * position_h.y)) * 100;

        //float progression = (Float.valueOf(position_w.x * position_h.x)) / (Float.valueOf(position_w.y * position_h.y)) * 100;
        System.out.println("all_products     :: position = " + position_w.x + " / " + position_w.y);
        System.out.println("updated_products :: position = " + position_h.x + " / " + position_h.y);
        System.out.println();
        System.out.println("Progression of Database <" + dbName + "> is: " + progression + " %");

    }

    private static String getProgress(String tag) {
        String path_progression_file = CfgMgr.getConf(CONF_PATH_TO_ROOT) + "/" + CfgMgr.getConf(CONF_TASK_PREPARER_PATH) + "/" + CfgMgr.getConf(CONF_PREPARER_PROGRESSION_FILE);
        return CfgMgr.readFromXml(path_progression_file, tag);
    }

    /*
    returns (x,y) where:
    - x = position of progress_code in the pathJsonMatrixFile JSON file (initialized to 0)
    - y = overall number of barcodes in the pathJsonMatrixFile JSON file
     */
    private static Tuple<Long, Long> getCursorPositionInJsonMatrix(String pathJsonMatrixFile, String progress_code) throws FileNotFoundException, IOException {
        FileInputStream istr = new FileInputStream(pathJsonMatrixFile);
        return JsonTools.findBarcodeInJsonStream(progress_code, istr);
    }

}
