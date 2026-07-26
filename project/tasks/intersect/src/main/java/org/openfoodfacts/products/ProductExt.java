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
package org.openfoodfacts.products;

import java.util.List;
import org.openfoodfacts.computers.ProductComputer;


public class ProductExt extends Product implements IProduct {

    protected final static int VOID_SIMILARITY = -1;

    protected Short score = null;
    protected short similarity_with_product = VOID_SIMILARITY;
    protected String other_code;
    protected Short other_score = null;

    public ProductExt(Product product) throws Exception {
        super(product.getId(), product.getCode(), product.getProduct_name(), product.getPnns_groups_1(), product.getCountries_tags(), product.getCategories_tags(), product.getIngredients_tags(), product.getBrands_tags(), product.getStores_tags(), product.getLanguages_codes(), product.getNutriments(), product.getNova_group(), product.getImages(), product.getNutritionGrades());
        //this.computeScore();
    }

    public Short getScore() throws Exception {
        if (this.score == null) {
            this.computeScore();
        }
        return this.score;
    }

    public void setScore(short score) {
        this.score = score;
    }

    public short getSimilarity_with_product() {
        return this.similarity_with_product;
    }

    public void setSimilarity_with_product(short similarity_with_product) {
        this.similarity_with_product = similarity_with_product;
    }

    protected void computeScore() throws Exception {
        this.score = ProductComputer.computeScore(this);
    }

    public void computeSimilarity(IProduct other_product) throws Exception {
        if (other_product != null) {
            this.other_code = other_product.getCode();
            this.other_score = ((ProductExt) other_product).getScore();
            this.similarity_with_product = ProductComputer.computeSimilarity(this, (ProductExt)other_product);
        }
    }

    public String getOther_code() {
        return other_code;
    }

    public void setOther_code(String other_code) {
        this.other_code = other_code;
    }

    public Short getOtherScore() {
        return other_score;
    }

    public void setOtherScore(Short other_score) {
        this.other_score = other_score;
    }

}
