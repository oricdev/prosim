package org.openfoodfacts.products;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author olivier
 */
public class ProductExt extends Product implements IProduct {

    private final static int VOID_SIMILARITY = -1;

    private Short nutrition_score = null;
    private short similarity_with_product = VOID_SIMILARITY;
    private String other_code;
    private Short other_nutrition_score=null;

    public ProductExt(Product product) {
        super(product.getId(), product.getCode(), product.getProduct_name(), product.getCountries_tags(), product.getCategories_tags(), product.getBrands_tags(), product.getStores_tags(), product.getNutriments(), product.getImages(), product.getNutrition_score_uk());
        this.computeNutritionScore();
    }

    public Short getNutrition_score() {
        if (this.nutrition_score == null) {
            this.computeNutritionScore();
        }
        return this.nutrition_score;
    }

    public void setNutrition_score(short nutrition_score) {
        this.nutrition_score = nutrition_score;
    }

    public short getSimilarity_with_product() {
        return this.similarity_with_product;
    }

    public void setSimilarity_with_product(short similarity_with_product) {
        this.similarity_with_product = similarity_with_product;
    }

    private void computeNutritionScore() {
        // todo: distinguer Eaux et Boissons des aliments solides .. ici, que aliments solides
        /* ici http://fr.openfoodfacts.org/score-nutritionnel-france
         * A - Vert : jusqu'à -1
         * B - Jaune : de 0 à 2
         * C - Orange : de 3 à 10
         * D - Rose : de 11 à 18
         * E - Rouge : 19 et plus
         */
        if (this.getNutrition_score_uk() != null) {
            double score_uk = this.getNutrition_score_uk();
            if (score_uk >= 19) {
                this.nutrition_score = 1;
            } else if (score_uk >= 11) {
                this.nutrition_score = 2;
            } else if (score_uk >= 3) {
                this.nutrition_score = 3;
            } else if (score_uk >= 0) {
                this.nutrition_score = 4;
            } else {
                this.nutrition_score = 5;
            }
        }
    }

    public void computeSimilarity(IProduct other_product) {
        if (other_product != null) {
            this.other_code = other_product.getCode();
            this.other_nutrition_score = ((ProductExt)other_product).getNutrition_score();
            List<String> categs_ref = this.getCategories_tags();
            List<String> categs_other = ((Product) other_product).getCategories_tags();
            if (null == categs_ref || 0 == categs_ref.size() || null == categs_other || 0 == categs_other.size()) {
                this.similarity_with_product = VOID_SIMILARITY;
            } else {
                double nb_intersect = categs_other.stream().filter(categ_other -> categs_ref.contains(categ_other)).count();
                this.similarity_with_product = (short) ((nb_intersect / categs_ref.size()) * 100);
            }
        }
    }

    public String getOther_code() {
        return other_code;
    }

    public void setOther_code(String other_code) {
        this.other_code = other_code;
    }

    public Short getOther_nutrition_score() {
        return other_nutrition_score;
    }

    public void setOther_nutrition_score(Short other_nutrition_score) {
        this.other_nutrition_score = other_nutrition_score;
    }

}
