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
package org.openfoodfacts.computers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.openfoodfacts.products.ProductExt;


public abstract class ProductComputer {

    private static final String FIELD_DB_NICKNAME = "DB_NICKNAME";
    private static final String FIELD_DB_NAME = "DB_NAME";
    private static final String FIELD_SIMILARITY_MIN_PERCENTAGE = "SIMILARITY_MIN_PERCENTAGE";
    private static final String FIELD_YOUR_NAME = "YOUR_NAME";
    private static final String FIELD_EMAIL_ADDRESS = "EMAIL_ADDRESS";
    private static final String FIELD_DB_SUMMARY = "DB_SUMMARY";
    private static final String FIELD_DB_DESCRIPTION_EN = "DB_DESCRIPTION_EN";
    private static final String FIELD_DB_DESCRIPTION = "DB_DESCRIPTION";
    private static final String FIELD_DB_MAX_SIZE_GB = "DB_MAX_SIZE_GB";

    /*
    * Returns False if product has to be ignored for the comparison with other products in the database; True in order to state that product is effectively taken into account
    * e.g.: you may want to consider only products of a specific country, ignore products with allergens, etc.
     */
    public static boolean filter(ProductExt product) throws Exception {
        Object t = null;
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Method method = c.getDeclaredMethod("filter", ProductExt.class);
            t = method.invoke(c, product);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new Exception("FATAL ERROR: a <ComputingInstance> project was missing when calling the java -cp statement. Hence no score for the products could be computed. Refer to the PROSIM blog to learn how to add a new instance of score and simlarity computation to the PROSIM-Engine.", ex);
        }
        return (Boolean) t;
    }

    /*
    * Computes the proximity/similarity of prodB regarding prodA based on intersections of property categories_tags of both products
     */
    public static Short computeSimilarity(ProductExt prodA, ProductExt prodB) throws Exception {
        Object t = null;
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Method method = c.getDeclaredMethod("computeSimilarity", ProductExt.class, ProductExt.class);
            t = method.invoke(c, prodA, prodB);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new Exception("FATAL ERROR: a <ComputingInstance> project was missing when calling the java -cp statement. Hence no score for the products could be computed. Refer to the PROSIM blog to learn how to add a new instance of score and simlarity computation to the PROSIM-Engine.", ex);
        }
        return (Short) t;
    }

    public static Short computeScore(ProductExt product) throws Exception {
        Object t = null;
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Method method = c.getDeclaredMethod("computeScore", ProductExt.class);
            t = method.invoke(c, product);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new Exception("FATAL ERROR: a <ComputingInstance> project was missing when calling the java -cp statement. Hence no score for the products could be computed. Refer to the PROSIM blog to learn how to add a new instance of score and simlarity computation to the PROSIM-Engine.", ex);
        }
        return (Short) t;
    }

    // ********************************
    // Getters for constants properties in ComputingInstance (configuration of db, user name, email, etc.)
    // ********************************
    public static String getDbNickName() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_DB_NICKNAME);
            }
            if (isFound) {
                return (String) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_DB_NICKNAME + " in class file!", ex);
        }
        return "";
    }

    public static String getDbName() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_DB_NAME);
            }
            if (isFound) {
                return (String) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_DB_NAME + " in class file!", ex);
        }
        return "";
    }

    public static String getDbSummary() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_DB_SUMMARY);
            }
            if (isFound) {
                return (String) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_DB_SUMMARY + " in class file!", ex);
        }
        return "";
    }

    public static String getDbDescriptionEN() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_DB_DESCRIPTION_EN);
            }
            if (isFound) {
                return (String) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_DB_DESCRIPTION_EN + " in class file!", ex);
        }
        return "";
    }

    public static String getDbDescription() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_DB_DESCRIPTION);
            }
            if (isFound) {
                return (String) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_DB_DESCRIPTION + " in class file!", ex);
        }
        return "";
    }

    public static short getDbMaxSize() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_DB_MAX_SIZE_GB);
            }
            if (isFound) {
                return (Short) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_DB_MAX_SIZE_GB + " in class file!", ex);
        }
        return 0;
    }

    public static String getSimilarityMinPercentage() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_SIMILARITY_MIN_PERCENTAGE);
            }
            if (isFound) {
                return (String) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_SIMILARITY_MIN_PERCENTAGE + " in class file!", ex);
        }
        return "";
    }

    public static String getYourkName() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_YOUR_NAME);
            }
            if (isFound) {
                return (String) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_YOUR_NAME + " in class file!", ex);
        }
        return "";
    }

    public static String getEmailAddress() throws Exception {
        try {
            Class<?> c = Class.forName("org.openfoodfacts.computers.ComputingInstance");
            Field[] properties = c.getDeclaredFields();
            boolean isFound = false;
            int i = 0;
            for (i = 0; i < properties.length && !isFound; i++) {
                isFound = properties[i].getName().equals(FIELD_EMAIL_ADDRESS);
            }
            if (isFound) {
                return (String) properties[i - 1].get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException ex) {
            throw new Exception("FATAL ERROR while retrieving " + FIELD_EMAIL_ADDRESS + " in class file!", ex);
        }
        return "";
    }
}
