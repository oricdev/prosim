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

import org.openfoodfacts.products.Product;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mongodb.client.MongoCollection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.openfoodfacts.computers.ProductComputer;
import org.openfoodfacts.databases.ProsimDb;
import org.openfoodfacts.products.IProduct;
import org.openfoodfacts.products.ProductExt;
import org.openfoodfacts.products.products;


public class JsonTools {

    final static Logger logger = Logger.getLogger(JsonTools.class);

    public static List<Product> readJsonStream(InputStream in) throws Exception {
        List<Product> products = new ArrayList<>();

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginArray();
        Gson gson = new Gson();
        while (reader.hasNext()) {
            Product product = gson.fromJson(reader, Product.class);
            // Apply filter to decide whether one product is kept for comparison or not
            if (ProductComputer.filter(new ProductExt(product))) {
                product.prepare();
                products.add(product);
            }
        }
        reader.endArray();
        reader.close();

        return products;
    }

    public static List<ProsimDb> readJsonStreamForDbs(InputStream in) throws Exception {
        List<ProsimDb> dbs = new ArrayList<>();
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginArray();
            Gson gson = new Gson();
            while (reader.hasNext()) {
                ProsimDb oneDb = gson.fromJson(reader, ProsimDb.class);
                dbs.add(oneDb);
            }
            reader.endArray();
            reader.close();
        } catch (IOException ioe) {

        }
        return dbs;
    }

    /*
    returns (x,y) where:
    - x = position of barcodeToFind in the InputStream of JSON file (initialized to 0)
    - y = overall number of barcodes in the InputStream of JSON file
     */
    public static Tuple<Long, Long> findBarcodeInJsonStream(String barcodeToFind, InputStream in) throws UnsupportedEncodingException, IOException {
        Tuple<Long, Long> positionBarcode;
        long pos = 0;
        long total = 0;

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginArray();
        Gson gson = new Gson();
        while (reader.hasNext()) {
            Product product = gson.fromJson(reader, Product.class);
            if (pos == 0 && product.getCode().equals(barcodeToFind)) {
                // If just found
                pos = total;
            }
            total++;
        }
        reader.endArray();
        reader.close();

        positionBarcode = new Tuple<>(pos, total);
        return positionBarcode;
    }

    public static void writeJsonStream(String dname, String fname, List<IProduct> products) {
        FileOutputStream ostr_products = null;
        String full_fname = dname + File.separator + fname;
        try {
            if (products != null) {
                logger.info("saving " + products.size() + " product(s) in '" + full_fname + "'");
            }
            ostr_products = new FileOutputStream(dname + File.separator + fname);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(ostr_products, "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            Gson gson = new Gson();
            for (IProduct product : products) {
                if (product.getClass().equals(Product.class)) {
                    gson.toJson(product, Product.class, writer);
                } else if (product.getClass().equals(ProductExt.class)) {
                    gson.toJson(product, ProductExt.class, writer);
                }
            }
            writer.endArray();
            writer.close();
        } catch (IOException ex) {
            logger.error("could not write out matrix data file '" + full_fname + "'");
        }
    }

    public static void writeJsonStreamForDbs(String dname, String fname, List<ProsimDb> prosimDbs) {
        FileOutputStream ostr_prosim_dbs = null;
        String full_fname = dname + File.separator + fname;
        try {
            if (prosimDbs != null) {
                logger.info("saving " + prosimDbs.size() + " Prosim-Db(s) in '" + full_fname + "'");
            }
            ostr_prosim_dbs = new FileOutputStream(dname + File.separator + fname);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(ostr_prosim_dbs, "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            Gson gson = new Gson();
            for (ProsimDb dbProsim : prosimDbs) {
                gson.toJson(dbProsim, ProsimDb.class, writer);
            }
            writer.endArray();
            writer.close();
        } catch (IOException ex) {
            logger.error("could not write out Statistics file '" + full_fname + "'");
        }
    }

    public static void writeJsonStreamWithMongoCursor(String dname, String fname, MongoCollection<Document> cursorProducts) {
        FileOutputStream ostr_products = null;
        String full_fname = dname + File.separator + fname;
        try {
            logger.info("saving " + cursorProducts.count() + " product(s) in '" + full_fname + "'");
            ostr_products = new FileOutputStream(dname + File.separator + fname);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(ostr_products, "UTF-8"));
            writer.setIndent("  ");
            writer.beginArray();
            Gson gson = new Gson();

            // note: problem with Morphia mapping by reading, so this is the tedious part where we read each Document,
            // .. map it into a "dbs" object, and output it in the file into a file after JSON conversion
            int counter = 0;
            int counter_valid = 0;
            int counter_invalid = 0;
            String id = null;
            String code = null;
            String product_name = null;
            String pnns_groups_1 = null;
            List<String> countries_tags = null;
            List<String> categories_tags = null;
            List<String> ingredients_tags = null;
            List<String> brands_tags = null;
            List<String> stores_tags = null;
            Object languages_codes = null;
            Object nutriments = null;
            String nova_group_as_string = null;
            Double nova_group = null;
            Object images = null;
            Float nutrition_score_uk = null;
            for (Iterator<Document> productIterator = cursorProducts.find().iterator(); productIterator.hasNext(); counter++) {
                Document mongoDocument = productIterator.next();

                categories_tags = (List<String>) mongoDocument.get("categories_tags");
                if (null == mongoDocument.get("code") || null == categories_tags || !mongoDocument.containsKey("pnns_groups_1")) {
                    counter_invalid++;
                } else {
                    id = (String) mongoDocument.get("_id").toString();
                    code = (String) mongoDocument.get("code").toString();
                    product_name = (String) mongoDocument.get("product_name");
                    pnns_groups_1 = (String) mongoDocument.get("pnns_groups_1");

                    if (mongoDocument.containsKey("countries_tags")) {
                        countries_tags = (List<String>) mongoDocument.get("countries_tags");
                    } else {
                        countries_tags = null;
                    }

                    if (mongoDocument.containsKey("brands_tags")) {
                        brands_tags = (List<String>) mongoDocument.get("brands_tags");
                    } else {
                        brands_tags = null;
                    }

                    if (mongoDocument.containsKey("stores_tags")) {
                        stores_tags = (List<String>) mongoDocument.get("stores_tags");
                    } else {
                        stores_tags = null;
                    }

                    if (mongoDocument.containsKey("ingredients_tags")) {
                        ingredients_tags = (List<String>) mongoDocument.get("ingredients_tags");
                    } else {
                        ingredients_tags = null;
                    }

                    if (mongoDocument.containsKey("languages_codes")) {
                        languages_codes = mongoDocument.get("languages_codes");
                    } else {
                        languages_codes = null;
                    }

                    nutriments = mongoDocument.get("nutriments");

                    if (mongoDocument.containsKey("nova_group")) {
                        nova_group_as_string = (String) mongoDocument.get("nova_group").toString();
                        if (nova_group_as_string != null) {
                            nova_group = new Double(nova_group_as_string);
                        }
                    } else {
                        nova_group = null;
                    }

                    if (mongoDocument.containsKey("images")) {
                        images = mongoDocument.get("images");
                    } else {
                        images = null;
                    }

                    if (mongoDocument.containsKey("nutrition_score_uk")) {
                        nutrition_score_uk = (Float) mongoDocument.get("nutrition_score_uk");
                    } else {
                        nutrition_score_uk = null;
                    }

                    products product = new products(id, code, product_name, pnns_groups_1, countries_tags, categories_tags, ingredients_tags, brands_tags, stores_tags, languages_codes, nutriments, nova_group, images, nutrition_score_uk);
                    gson.toJson(product, Product.class, writer);
                    counter_valid++;
                }
            }

            writer.endArray();
            writer.close();

            logger.info("total number of records read in the mongo-Db: " + counter);
            logger.info("number of candidate records to the intersect-process: " + counter_valid);
            logger.info("number of invalid/ignored records due to missing information: " + counter_invalid);
        } catch (IOException ex) {
            logger.error("could not write out matrix data file '" + full_fname + "'");
        }
    }

    public static Tuple< List<IProduct>, List<IProduct>> extractOneCellMatrix(String code_h, String code_w, String fullpath_all_products, int width, String fullpath_updated_products, int height) {
//        long h_stats_nb_read = 0;
//        long w_stats_nb_read = 0;
        List<IProduct> h_products = new ArrayList<>();
        List<IProduct> w_products = new ArrayList<>();
        // Get a matrix/tuple of dbs

        try {
            // ..start with vertical (Height=h)
            FileInputStream istr_updated_products = new FileInputStream(fullpath_updated_products);
            Tuple<Long, List<IProduct>> h_tuple = JsonTools.extractOneCellMatrixInOneDimension(istr_updated_products, code_h, height);
            if (h_tuple == null) {
                h_products = null;
            } else {
//                h_stats_nb_read = h_tuple.x;
                h_products = h_tuple.y;
            }
        } catch (FileNotFoundException fnfe) {
            logger.error("could not find file " + fullpath_updated_products);
            logger.error("Process aborted!");
        }

        if (h_products == null) {
            // Code h was not found => error
            return null;
        }

        try {
            // ..end with horizontal (Width=w)
            FileInputStream istr_all_products = new FileInputStream(fullpath_all_products);
            Tuple<Long, List<IProduct>> w_tuple = JsonTools.extractOneCellMatrixInOneDimension(istr_all_products, code_w, width);
            if (w_tuple == null) {
                w_products = null;
            } else {
//                w_stats_nb_read = w_tuple.x;
                w_products = w_tuple.y;
            }
        } catch (FileNotFoundException fnfe) {
            logger.error("could not find file " + fullpath_all_products);
            logger.error("Process aborted!");
        }
        if (w_products == null) {
            // Code w was not found => error
            return null;
        }

//        logger.info("**********************************************");
//        logger.info("S T A T I S T I C S");
//        logger.info("-------------------");
//        double stats_global_height = Long.valueOf(CfgMgr.getConf(Main.CONF_STATS_HEIGHT));
//        double stats_global_width = Long.valueOf(CfgMgr.getConf(Main.CONF_STATS_WIDTH));
//        double progress_percentage = (h_stats_nb_read * w_stats_nb_read) / (stats_global_height * stats_global_width) * 100;
//        logger.info("position of cursor WIDTH : " + w_stats_nb_read + " / " + stats_global_width);
//        logger.info("position of cursor HEIGTH: " + h_stats_nb_read + " / " + stats_global_height);
//        logger.info("--> overall PROGRESSION  : " + (new DecimalFormat("###.#####")).format(progress_percentage));
//        logger.info("**********************************************");
        // writting statistics about overall progression
        //String stats_fname = CfgMgr.getConf(CONF_PATH_TO_ROOT) + File.separator + File.separator + CfgMgr.getConf(Main.CONF_OUT_FNAME_STATS);
        //Stats.outputInXml(stats_fname, w_stats_nb_read, stats_global_width, h_stats_nb_read, stats_global_height, progress_percentage);
        Tuple<List<IProduct>, List<IProduct>> matrixProducts = new Tuple<>(w_products, h_products);
        return matrixProducts;
    }

    /**
     *
     * @param in
     * @param code
     * @param length
     * @return Long: used for stats for computing the percentage of progreesion:
     * position of code related to the beginning of file List of length dbs
     * starting just after code
     */
    public static Tuple<Long, List<IProduct>> extractOneCellMatrixInOneDimension(InputStream in, String code, int length) {
        List<IProduct> products = new ArrayList<>();
        // If empty barcode, start adding dbs from the beginning; otherwise until found
        boolean isCodeFound = code.equals("");
        long nb_read = 0;
        int nb_codes_added = 0;

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginArray();
            Gson gson = new Gson();
            while (reader.hasNext() && nb_codes_added < length) {
                Product product = gson.fromJson(reader, Product.class);
                if (isCodeFound) {
                    products.add(product);
                    nb_codes_added++;
                } else {
                    isCodeFound = product.getCode().equals(code);
                    // Handle exception for consistency (especially Height dimension) where code found is the first read => proceed with it as well and do not bypass
                    if (isCodeFound && nb_read == 0) {
                        products.add(product);
                        nb_codes_added++;
                    }
                    nb_read++;
                }
            }
            //reader.endArray();
            reader.close();
        } catch (IOException ioe) {
            logger.error("error reading InputStream");
        }
        // return cell-matrix of dbs
        Tuple<Long, List<IProduct>> result = new Tuple<>(nb_read, products);

        return (!isCodeFound) ? null : result;
    }
}
