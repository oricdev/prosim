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
package org.openfoodfacts.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.log4j.Logger;
import java.util.logging.Level;


public class FileMgr {

    final static Logger logger = Logger.getLogger(FileMgr.class);

    public static boolean deleteFile(String fname) {
        logger.info("deleting " + fname + "..");
        File file = new File(fname);
        if (file.delete()) {
            logger.info("ok");
            return true;
        } else {
            logger.error("..could not be deleted!");
            return false;
        }
    }

    public static boolean mkdir(String dname) {
        boolean result = true;
        File dirName = new File(dname);
        try {
            dirName.mkdirs();
        } catch (SecurityException se) {
            logger.error("could not create directory '" + dname + "'");
            result = false;
        }
        return result;
    }

    /**
     *
     * @param dirpath
     * @param fileType
     * @return
     */
    public static File[] getAllFilesInDirectory(String dirpath, EnumFileType fileType) {
        File directory = null;
        File[] paths = null;

        try {
            directory = new File(dirpath);
            if (!directory.isDirectory()) {
                logger.error("cannot list content of '" + dirpath + "' since it is NOT a directory!");
                logger.error("Process aborted!");
            } else {
                FileFilter filter = (File pathname) -> fileType.equals(EnumFileType.DIRECTORY) ? pathname.isDirectory() : pathname.isFile();
                paths = directory.listFiles(filter);
            }
        } catch (Exception ex) {
        }
        return paths;
    }

    /**
     *
     * @param dirpath
     * @param min_datasets
     * @return
     */
    public static File getOldestDataset(String dirpath, int min_datasets) {
        File oldestDataset = null;
        File[] dataSets = FileMgr.getAllFilesInDirectory(dirpath, EnumFileType.DIRECTORY);
        if (null != dataSets && dataSets.length >= min_datasets) {
            Arrays.sort(dataSets, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
            oldestDataset = dataSets[0];
        }
        return oldestDataset;
    }

    public static void moveDir(String dest_dir, File dirDataset) {
        if (!dirDataset.isDirectory()) {
            logger.error("cannot backup '" + dirDataset.getAbsolutePath() + "' since it is NOT a directory!");
            logger.error("Process aborted!");
        } else {
            try {
                FileMgr.mkdir(dest_dir);
                String dataset_dir_part = dirDataset.getPath().split("/")[dirDataset.getPath().split("/").length - 1];
                String dest_backup_dir = dest_dir.concat("/").concat(dataset_dir_part);
                Path path_backup = FileSystems.getDefault().getPath(dest_backup_dir);
                Files.move(dirDataset.toPath(), path_backup);
            } catch (IOException ex) {
                logger.error("could not backup file <" + dirDataset.getAbsolutePath());
                java.util.logging.Logger.getLogger(FileMgr.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public static void deleteDataset(File dirDataset) {
        if (!dirDataset.isDirectory()) {
            logger.error("cannot delete '" + dirDataset.getAbsolutePath() + "' since it is NOT a directory!");
            logger.error("Process aborted!");
        } else {
            deleteDirectory(dirDataset);
        }
    }

    /*
    * Retrieved from:
    * https://stackoverflow.com/questions/3775694/deleting-folder-from-java
     */
    protected static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            logger.info("deleting <" + directory.getAbsolutePath() + ">..");
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    public static boolean createLockFile(String dirpath, String filename) {
        boolean success = true;

        try {
            FileMgr.mkdir(dirpath);
            String full_lock_filename = dirpath.concat("/").concat(filename);
            File file = new File(full_lock_filename);
            success = file.createNewFile();
        } catch (IOException ioex) {
            logger.error("lock file could not be created!");
            logger.error(ioex.getMessage());
            success = false;
        }
        return success;
    }
}
