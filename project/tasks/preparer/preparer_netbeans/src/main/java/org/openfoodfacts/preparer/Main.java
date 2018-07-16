package org.openfoodfacts.preparer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.openfoodfacts.products.IProduct;
import org.openfoodfacts.utils.CfgMgr;
import org.openfoodfacts.utils.EnumFileType;
import org.openfoodfacts.utils.FileMgr;
import org.openfoodfacts.utils.Tuple;

/**
 *
 * @author oric useful links: :gson: https://github.com/google/gson :gson api:
 * http://www.javadoc.io/doc/com.google.code.gson/gson/2.8.5 :gson sample
 * streaming: https://sites.google.com/site/gson/streaming :log4j:
 * https://www.mkyong.com/logging/log4j-hello-world-example/
 */
public class Main {

    final static Logger logger = Logger.getLogger(Main.class);
    // Tags in config.xml file
    final static String CONF_PATH_TO_ROOT = "path_to_root";
    final static String CONF_MAX_OUTPUT_DATA = "max_output_data";
    final static String CONF_STATS_HEIGHT = "stats_H_nb_products";
    final static String CONF_STATS_WIDTH = "stats_W_nb_products";
    final static String CONF_PATH_OUT_PREPARER = "out_path_preparer";
    final static String CONF_PATH_OUT_FEEDERS = "out_path_feeders";
    final static String CONF_OUT_FNAME_ALL_PRODUCTS = "out_all_products";
    final static String CONF_OUT_FNAME_UPDATED_PRODUCTS = "out_updated_products";
    final static String CONF_OUT_FNAME_STATS = "out_path_stats";
    final static String CONF_SERVER_PATH = "server_path";
    final static String CONF_FILEPATH_NB_FREE_SLOTS = "filepath_nb_free_slots";
    final static String CONF_WIDTH = "width";
    final static String CONF_HEIGHT = "height";
    // Tags in progress.xml file
    final static String PROGRESS_LAST_CODE_ALL_PRODUCTS = "last_intersect_code_all_products";
    final static String PROGRESS_LAST_CODE_UPDATED_PRODUCTS = "last_intersect_code_to_be_inserted";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        logger.info("Process Preparer started..");
        logger.info("Log level is " + logger.getParent().getLevel().toString().toUpperCase());
        // get available slots N on server => provide N new files
        int nb_free_slots = getNbSlots();
        logger.info("<"+nb_free_slots + "> new slots are available on the server");

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
        logger.info("Process Preparer finished.");
    }

    private static String getProgress(String tag) {
        return CfgMgr.readFromXml("progress.xml", tag);
    }

    private static boolean setProgress(String tag, String a_value) {
        return CfgMgr.updateXml("progress.xml", tag, a_value);
    }

    private static int getNbSlots() {
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
    private static int prepareNewFiles(int nb_files_to_create) {
        int nb_files = 0;
        boolean isFinished = false;
        boolean isSomethingWrong = false;
        int width = Integer.parseInt(CfgMgr.getConf(Main.CONF_WIDTH));

        do {
            logger.info(nb_files + " dataset-package(s) created yet..");
            // read progress.xml file to get data about the last extracted file (last intersection block created)
            // ..last barcode in ALL products processed in previous batch (w=width in Matrix)
            String last_code_w = getProgress(PROGRESS_LAST_CODE_ALL_PRODUCTS);
            // ..last barcode of UPDATED products processed in previous batch (h=height in Matrix)
            String last_code_h = getProgress(PROGRESS_LAST_CODE_UPDATED_PRODUCTS);

            Tuple< List<IProduct>, List<IProduct>> cell_matrix = JsonTools.extractOneCellMatrix(last_code_h, last_code_w);
            isSomethingWrong = cell_matrix == null;
            if (!isSomethingWrong) {
                int cell_width = cell_matrix.x.size();
                int cell_height = cell_matrix.y.size();
                if (cell_width > 0 && cell_height > 0) {
                    // create h and w files
                    // ..random name for directory (not important, just for gathering couples {h, w} matrix-data
                    String uniqueID = UUID.randomUUID().toString();
                    String out_matrix_dir_for_files = CfgMgr.getConf(CONF_PATH_TO_ROOT) + File.separator + CfgMgr.getConf(CONF_PATH_OUT_PREPARER) + File.separator + uniqueID;
                    isSomethingWrong = !(FileMgr.mkdir(out_matrix_dir_for_files));
                    if (!isSomethingWrong) {
                        JsonTools.writeJsonStream(out_matrix_dir_for_files, "h_products.json", cell_matrix.x);
                        JsonTools.writeJsonStream(out_matrix_dir_for_files, "w_products.json", cell_matrix.y);
                        nb_files++;
                    }

                }
                if ((cell_width < width || cell_width == 0) && cell_height != 0) {
                    // End of line-X-matrix reached, we start a new block on next line
                    last_code_w = "";
                    last_code_h = cell_matrix.y.get(cell_matrix.y.size() - 1).getCode();
                } else if (cell_height == 0) {
                    // finished: all cells have been processed and associated files generated
                    isFinished = true;
                } else {
                    // update of last products processed in h and w dimensions
                    last_code_w = cell_matrix.x.get(cell_matrix.x.size() - 1).getCode();
                    last_code_h = cell_matrix.y.get(0).getCode();

                }
                if (!isFinished) {
                    // save progress
                    setProgress(PROGRESS_LAST_CODE_ALL_PRODUCTS, last_code_w);
                    setProgress(PROGRESS_LAST_CODE_UPDATED_PRODUCTS, last_code_h);
                }
            }
        } while (nb_files < nb_files_to_create && !isFinished && !isSomethingWrong);
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
