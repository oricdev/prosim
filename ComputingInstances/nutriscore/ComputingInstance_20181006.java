/*
 * Delivered by oric.dev@iznogoud.neomailbox.ch (Admin)
 * Feel free to use this template to create your own OpenFoodFacts scoring and submit it back at the above email to see it live in off_graph App.
 * 
 * IMPORTANT: by using and submitting your own file, you agree that it is publicly available to anyone, 
 * and that Admins are authorized to eventually post it on THE PROSIM blog (https://offmatch.blogspot.com/) so that other people 
 * or communities can watch and get inspiration from interesting code samples.
 * Hence please be careful with your personal data you write down in the constants below and decide carefully!
 
 * Consult this page for details on how to proceed:
 * https://offmatch.blogspot.com/2018/10/how-can-i-request-new-score-database.html
 * 
 */
package org.openfoodfacts.computers;

import java.util.List;
import org.openfoodfacts.products.ProductExt;

/**
 *
 * @author oric.dev (oric.dev@iznogoud.neomailbox.ch)
 */
public class ComputingInstance {
    // START // Note to user: please fill in the constants below with your data
    // (check your email in order to get a validation status from the Admin)
    // .. nick name of your database in 1 single word with no spaces, no special
    // chars except "_" (used for building progression statistics, in OFF_GRAPH App
    // to add your own graph, to query your db through the PROSIM-Engine API, etc.)
    public static final String DB_NICKNAME = "nutriscore";
    // .. name being displayed client side (GUI)
    public static final String DB_DISPLAY_NAME = "Nutriscore";
    // .. MongoDb Name : must start with "tuttifrutti_"
    public static final String DB_NAME = "tuttifrutti_nutriscore_next";
    // .. summary of the purpose of the database (ex.: products without gluten,
    // vegan products, etc.)
    public static final String DB_SUMMARY = "worldwide products based on their nutriscore";
    // .. full description in English
    public static final String DB_DESCRIPTION_EN = "Database aimed at classifying OpenFoodFacts products based on their similarity to each other along with their nutrition-score";
    // .. full description in your own language
    public static final String DB_DESCRIPTION = "Base de classification des produits OpenFoodFacts selon leur proximité avec des produits équivalents et leur score nutritionnel (nutrition-score)";
    // .. max db size in GB (this will be decided by Admin)
    public static final short DB_MAX_SIZE_GB = 3;
    // .. db will store only comparisons which have minimum this percentage (0=all
    // intersections, 60=similarity of pairs of products with a minimum of 60%,
    // etc.)
    public static final String SIMILARITY_MIN_PERCENTAGE = "60";
    // Min and Max values returned by the computeScore method
    public static final Short SCORE_MIN_VALUE = 1;
    public static final Short SCORE_MAX_VALUE = 5;
    // If a higher value returned by the computeScore method means a BETTER value, then set bottom_up to true
    public static final Boolean bottom_up = true;
    // .. your name, nick name (will be displayed in statistics, help, as creator of
    // the database)
    public static final String YOUR_NAME = "oric.dev";
    // .. your email address to receive a validation status of your db-request from
    // the Admin
    public static final String EMAIL_ADDRESS = "oric.dev@iznogoud.neomailbox.ch";
    // .. AGREE to show email in PROSIM and OFF-GRAPH blogs and Apps?
    public static final boolean MAKE_EMAIL_VISIBLE = true;

    // END //

    protected final static int VOID_SIMILARITY = -1;

    public static void main() {
    }

    /*
     * Occurs first! computeScore and computeSimilarity will be launched only on
     * filetered products. Returns False if product has to be ignored for the
     * comparison with other products in the database; True in order to state that
     * product is effectively taken into account e.g.: you may want to consider only
     * products of a specific country, ignore products with allergens, etc.
     */
    public static boolean filter(ProductExt product) {
        return true;
    }

    /*
     * Products are already filtered Computes the proximity/similarity of prodB
     * regarding prodA based on intersections of property categories_tags of both
     * products Result is a percentage between 0 and 100 (%)
     */
    public static Short computeSimilarity(ProductExt prodA, ProductExt prodB) {
        Short similarity = VOID_SIMILARITY;
        List<String> categs_ref = prodA.getCategories_tags();
        List<String> categs_other = ((ProductExt) prodB).getCategories_tags();
        if (!(null == categs_ref || 0 == categs_ref.size() || null == categs_other || 0 == categs_other.size())) {
            double nb_intersect = categs_other.stream().filter(categ_other -> categs_ref.contains(categ_other)).count();
            similarity = (short) ((nb_intersect / categs_ref.size()) * 100);
        }
        return similarity;
    }

    /*
     * Products are already filtered compute score (nutrition score, nova score,
     * your own score) Result: 1 is bottom in the graph, 5 is top Note: you may
     * return any value, th graph adapts (or will adapt) automatically, or build
     * your own graph using the PROSIM API
     */
    public static Short computeScore(ProductExt product) {
        // todo: distinguer Eaux et Boissons des aliments solides .. ici, que aliments
        // solides
        /*
         * ici https://fr.openfoodfacts.org/score-nutritionnel-experimental-france
         */
        Short score = null;
        if (product.getNutrition_score_uk() != null) {
            double score_uk = product.getNutrition_score_uk();
            if (score_uk >= 17) {
                score = 1;
            } else if (score_uk >= 12) {
                score = 2;
            } else if (score_uk >= 4) {
                score = 3;
            } else if (score_uk >= -1) {
                score = 4;
            } else {
                score = 5;
            }
        }
        return score;
    }
}
