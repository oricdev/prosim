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
package org.openfoodfacts.preparer;

import org.openfoodfacts.utils.JsonTools;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.openfoodfacts.products.IProduct;
import org.openfoodfacts.utils.CfgMgr;
import org.openfoodfacts.utils.EnumFileType;
import org.openfoodfacts.utils.FileMgr;
import org.openfoodfacts.utils.Tuple;

/**
 * useful links: :gson: https://github.com/google/gson :gson api:
 * http://www.javadoc.io/doc/com.google.code.gson/gson/2.8.5 :gson sample
 * streaming: https://sites.google.com/site/gson/streaming :log4j:
 * https://www.mkyong.com/logging/log4j-hello-world-example/
 */
public class Main {

    final static Logger logger = Logger.getLogger(org.openfoodfacts.preparer.Main.class);
    // Tags in config.xml file
    final static String CONF_PATH_TO_ROOT = "path_to_root";
    final static String CONF_MAX_OUTPUT_DATA = "max_output_data";
    final static String CONF_PATH_OUT_PREPARER = "out_path_preparer";
    final static String CONF_PATH_OUT_FEEDERS = "out_path_feeders";
    final static String CONF_OUT_FNAME_ALL_PRODUCTS = "out_all_products";
    final static String CONF_OUT_FNAME_UPDATED_PRODUCTS = "out_updated_products";
    final static String CONF_WIDTH = "width";
    final static String CONF_HEIGHT = "height";
    // Tags in progress.xml file
    final static String PROGRESS_LAST_CODE_ALL_PRODUCTS = "last_intersect_code_all_products";
    final static String PROGRESS_LAST_CODE_UPDATED_PRODUCTS = "last_intersect_code_to_be_inserted";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // init log file: NOT WORKING with relative path?
        /* FileAppender f_appender = (FileAppender)Logger.getRootLogger().getAppender("file");
        String fname_log = CfgMgr.getConf(CONF_PATH_TO_ROOT).concat("/").concat(CfgMgr.getConf(CONF_PATH_LOGFILE_NAME));
        f_appender.setFile(fname_log);
        */

        logger.info("Process Preparer started..");
        logger.info("Log level is " + logger.getParent().getLevel().toString().toUpperCase());

        int nbSecondsSuspension = 300 * 1000;
        String strNbSecondsSuspension = System.getenv("NB_SECONDS_SUSPENSION");
        if (null != strNbSecondsSuspension && !strNbSecondsSuspension.isEmpty()) {
            nbSecondsSuspension = Integer.parseInt(strNbSecondsSuspension) * 1000;
        }

        // get available slots N on server => provide N new files and suspend before looping
        do {
            try {
                int nb_free_slots = getNbSlots();

                logger.info("<" + nb_free_slots + "> new slots are available on the server");

                // if nb_free_slots > 0 do prepare N files
                if (nb_free_slots > 0) {
                    logger.info("creating " + nb_free_slots + " new files");

                    // prepare N files which are to be used on remote/local server in order to create work units
                    int nb_new_files = prepareNewFiles(nb_free_slots);
                    if (nb_new_files != nb_free_slots) {
                        logger.info(nb_free_slots + " free slots are available but ONLY " + nb_new_files + " data files were needed! All done!");
                    } else {
                        logger.info(nb_free_slots + " new files for Matrix.WIDTH and Matrix.Height were created successfully.");
                    }
                } else {
                    logger.info("no need to create new files now.");
                }
                logger.info("Process Preparer finished so far..");
            } catch (RuntimeException rex) {
                logger.error("Error occurred while reading config file..!");
            }
            logger.info("..SUSPENDING preparer for " + strNbSecondsSuspension + " seconds..");
            try {
                Thread.sleep(nbSecondsSuspension);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("..STARTS AGAIN preparer..");
        } while (true);
    }

    private static String getProgress(String tag) {
        String path_progress = System.getenv("PATH_PROGRESS_FILE");
        return CfgMgr.readFromXml(path_progress, tag);
    }

    private static boolean setProgress(String tag, String a_value) {
        String path_progress = System.getenv("PATH_PROGRESS_FILE");
        return CfgMgr.updateXml(path_progress, tag, a_value);
    }

    private static int getNbSlots() throws IOException {
        int max_slots_allowed = Integer.valueOf(CfgMgr.getConf(CONF_MAX_OUTPUT_DATA));
        String srv = CfgMgr.getConf(CONF_PATH_TO_ROOT);
        String path_output = CfgMgr.getConf(CONF_PATH_OUT_PREPARER);
        String full_path = srv + File.separator + path_output;
        File[] f_slots = FileMgr.getAllFilesInDirectory(full_path, EnumFileType.DIRECTORY);
        return f_slots == null ? max_slots_allowed : (max_slots_allowed - f_slots.length);
    }

    /*
     * Based on a matrix of products All intersected with Updated, generates nb_files_to_create files with 2 cursors (height and width)
     */
    private static int prepareNewFiles(int nb_files_to_create) throws IOException {
        int nb_files = 0;
        boolean isFinished = false;
        boolean isSomethingWrong = false;

        String fullpath_updated_products = CfgMgr.getConf(CONF_PATH_TO_ROOT) + File.separator + CfgMgr.getConf(Main.CONF_PATH_OUT_FEEDERS) + File.separator + CfgMgr.getConf(Main.CONF_OUT_FNAME_UPDATED_PRODUCTS);
        int height = Integer.parseInt(CfgMgr.getConf(Main.CONF_HEIGHT));
        String fullpath_all_products = CfgMgr.getConf(CONF_PATH_TO_ROOT) + File.separator + CfgMgr.getConf(Main.CONF_PATH_OUT_FEEDERS) + File.separator + CfgMgr.getConf(Main.CONF_OUT_FNAME_ALL_PRODUCTS);
        int width = Integer.parseInt(CfgMgr.getConf(Main.CONF_WIDTH));

        do {
            logger.info(nb_files + " dataset-package(s) created yet..");
            // read progress.xml file to get data about the last extracted file (last intersection block created)
            // ..last barcode in ALL products processed in previous batch (w=width in Matrix)
            String last_code_w = getProgress(PROGRESS_LAST_CODE_ALL_PRODUCTS);
            // ..last barcode of UPDATED products processed in previous batch (h=height in Matrix)
            String last_code_h = getProgress(PROGRESS_LAST_CODE_UPDATED_PRODUCTS);

            String uniqueID = UUID.randomUUID().toString();
            String out_matrix_dir_for_files = CfgMgr.getConf(CONF_PATH_TO_ROOT) + File.separator + CfgMgr.getConf(CONF_PATH_OUT_PREPARER) + File.separator + uniqueID;
            isSomethingWrong = !(FileMgr.mkdir(out_matrix_dir_for_files));
            if (!isSomethingWrong) {
                // Tuple<nb_codes_added, last_code_read>
                Tuple<Long, String> cell_matrix_h = JsonTools.extractAndStreamProducts(fullpath_updated_products, last_code_h, height, out_matrix_dir_for_files + File.separator + "h_products.json");
                Tuple<Long, String> cell_matrix_w = JsonTools.extractAndStreamProducts(fullpath_all_products, last_code_w, width, out_matrix_dir_for_files + File.separator + "w_products.json");
                isSomethingWrong = cell_matrix_h == null || cell_matrix_w == null;
                if (!isSomethingWrong) {
                    // save progress
                    setProgress(PROGRESS_LAST_CODE_ALL_PRODUCTS, cell_matrix_w.y);
                    setProgress(PROGRESS_LAST_CODE_UPDATED_PRODUCTS, cell_matrix_h.y);
                }
                isFinished = Objects.requireNonNull(cell_matrix_h).y.isEmpty() && Objects.requireNonNull(cell_matrix_w).y.isEmpty();
                nb_files++;
            }
        }
        while (nb_files < nb_files_to_create && !isFinished && !isSomethingWrong);
        if (isFinished) {
            logger.info("SUCCESS: all Matrix data files have been generated and are ready to be transferred [" + nb_files + " file(s)]");
            // reinit progress
            setProgress(PROGRESS_LAST_CODE_ALL_PRODUCTS, "");
            setProgress(PROGRESS_LAST_CODE_UPDATED_PRODUCTS, "");
            logger.info("progress.xml file has been initialized back.");
            // remove all fles outputted by feeders to unlock the feeders
            String path_output_feeders = CfgMgr.getConf(CONF_PATH_TO_ROOT) + File.separator + CfgMgr.getConf(CONF_PATH_OUT_FEEDERS) + File.separator;
            FileMgr.deleteFile(path_output_feeders + "feeder_1_ok.txt");
            FileMgr.deleteFile(path_output_feeders + "feeder_2_ok.txt");
            FileMgr.deleteFile(path_output_feeders + "all_products.json");
            FileMgr.deleteFile(path_output_feeders + "updated_products.json");
        } else if (isSomethingWrong) {
            logger.error("Something went wrong in creating matrix-data files. Please check that h_code and w_code stored in progress.xml can be found in the input xml files. Possible extra checking: disk space, write-permissions.");
            logger.error("Process is aborted!");
        } else {
            logger.info("Waiting for new free slots on server before generating new files..");
        }
        logger.info("Process terminated.");

        return nb_files;
    }
}
