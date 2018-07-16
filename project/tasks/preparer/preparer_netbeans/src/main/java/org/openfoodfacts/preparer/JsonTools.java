package org.openfoodfacts.preparer;

import org.openfoodfacts.products.Product;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import static org.openfoodfacts.preparer.Main.CONF_PATH_TO_ROOT;
import org.openfoodfacts.products.IProduct;
import org.openfoodfacts.products.ProductExt;
import org.openfoodfacts.utils.CfgMgr;
import org.openfoodfacts.utils.Stats;
import org.openfoodfacts.utils.Tuple;

/**
 *
 * @author oric useful links: :gson: https://github.com/google/gson :gson api:
 * http://www.javadoc.io/doc/com.google.code.gson/gson/2.8.5 :gson sample
 * streaming: https://sites.google.com/site/gson/streaming
 */
public class JsonTools {

    final static Logger logger = Logger.getLogger(JsonTools.class);

    public static List<Product> readJsonStream(InputStream in) {
        List<Product> products = new ArrayList<Product>();
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginArray();
            Gson gson = new Gson();
            while (reader.hasNext()) {
                Product product = gson.fromJson(reader, Product.class);
                product.prepare();
                products.add(product);
            }
            reader.endArray();
            reader.close();
        } catch (IOException ioe) {

        }
        return products;
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

    public static Tuple< List<IProduct>, List<IProduct>> extractOneCellMatrix(String code_h, String code_w) {
        long h_stats_nb_read = 0;
        long w_stats_nb_read = 0;
        List<IProduct> h_products = new ArrayList<>();
        List<IProduct> w_products = new ArrayList<>();
        // Get a matrix/tuple of products
        String fullpath_updated_products = CfgMgr.getConf(CONF_PATH_TO_ROOT) + File.separator + CfgMgr.getConf(Main.CONF_PATH_OUT_FEEDERS) + File.separator + CfgMgr.getConf(Main.CONF_OUT_FNAME_UPDATED_PRODUCTS);
        int height = Integer.parseInt(CfgMgr.getConf(Main.CONF_HEIGHT));
        String fullpath_all_products = CfgMgr.getConf(CONF_PATH_TO_ROOT) + File.separator + CfgMgr.getConf(Main.CONF_PATH_OUT_FEEDERS) + File.separator + CfgMgr.getConf(Main.CONF_OUT_FNAME_ALL_PRODUCTS);
        int width = Integer.parseInt(CfgMgr.getConf(Main.CONF_WIDTH));

        try {
            // ..start with vertical (Height=h)
            FileInputStream istr_updated_products = new FileInputStream(fullpath_updated_products);
            Tuple<Long, List<IProduct>> h_tuple = JsonTools.extractOneCellMatrixInOneDimension(istr_updated_products, code_h, height);
            if (h_tuple == null) {
                h_products = null;
            } else {
                h_stats_nb_read = h_tuple.x;
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
                w_stats_nb_read = w_tuple.x;
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

        logger.info("**********************************************");
        logger.info("S T A T I S T I C S");
        logger.info("-------------------");
        double stats_global_height = Long.valueOf(CfgMgr.getConf(Main.CONF_STATS_HEIGHT));
        double stats_global_width = Long.valueOf(CfgMgr.getConf(Main.CONF_STATS_WIDTH));
        double progress_percentage = (h_stats_nb_read * w_stats_nb_read) / (stats_global_height * stats_global_width) * 100;
        logger.info("position of cursor WIDTH : " + w_stats_nb_read + " / " + stats_global_width);
        logger.info("position of cursor HEIGTH: " + h_stats_nb_read + " / " + stats_global_height);
        logger.info("--> overall PROGRESSION  : " + (new DecimalFormat("###.#####")).format(progress_percentage));
        logger.info("**********************************************");
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
     * position of code related to the beginning of file List of length products
     * starting just after code
     */
    public static Tuple<Long, List<IProduct>> extractOneCellMatrixInOneDimension(InputStream in, String code, int length) {
        List<IProduct> products = new ArrayList<>();
        // If empty barcode, start adding products from the beginning; otherwise until found
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
        // return cell-matrix of products
        Tuple<Long, List<IProduct>> result = new Tuple<>(nb_read, products);

        return (!isCodeFound) ? null : result;
    }
}
