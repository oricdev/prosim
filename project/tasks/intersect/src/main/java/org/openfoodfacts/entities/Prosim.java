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

 /*
 * See here the full sample for using Morphia Entities with Mongo:
 * https://dzone.com/articles/using-morphia-map-java-objects
 */
package org.openfoodfacts.entities;

import com.google.gson.internal.LinkedTreeMap;
import java.util.HashMap;
import java.util.List;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.openfoodfacts.products.IProduct;
import org.openfoodfacts.products.ProductExt;


@Indexes({
    @Index(fields = {
        @Field("code")
        , @Field("countries_tags")
        , @Field("product_name")
        , @Field("score")})})
@Entity
public class Prosim extends BaseEntity {

    private String code;
    private String product_name;
    private List<String> countries_tags;
    private List<String> categories_tags;
    private List<String> ingredients_tags;
    private List<String> stores_tags;
    private List<String> brands_tags;
    private LinkedTreeMap languages_codes;
    private LinkedTreeMap nutriments;
    private Double nova_group;
    private LinkedTreeMap images;
    private Float nutrition_score_uk;
    private Short score;
    private HashMap<Short, HashMap<Short, String[]>> similarity;

    public Prosim() {
    }

    public Prosim(IProduct product) throws Exception {
        ProductExt prodExt = (ProductExt) product;
        this.code = prodExt.getCode();
        this.product_name = prodExt.getProduct_name();
        this.categories_tags = prodExt.getCategories_tags();
        this.ingredients_tags = prodExt.getIngredients_tags();
        this.countries_tags = prodExt.getCountries_tags();
        this.stores_tags = prodExt.getStores_tags();
        this.brands_tags = prodExt.getBrands_tags();
        this.languages_codes = (LinkedTreeMap) prodExt.getLanguages_codes();
        this.nutriments = (LinkedTreeMap) prodExt.getNutriments();
        this.nova_group = prodExt.getNova_group();
        this.images = (LinkedTreeMap) prodExt.getImages();
        this.nutrition_score_uk = prodExt.getNutrition_score_uk();
        this.score = prodExt.getScore();
        // similarity section
        this.similarity = new HashMap<>();
        HashMap<Short, String[]> sim_score = new HashMap<>();
        sim_score.put(prodExt.getOtherScore(), new String[]{prodExt.getOther_code()});
        this.similarity.put(prodExt.getSimilarity_with_product(), sim_score);
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProduct_name() {
        return this.product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public List<String> getCountries_tags() {
        return this.countries_tags;
    }

    public void setCountries_tags(List<String> countries_tags) {
        this.countries_tags = countries_tags;
    }

    public List<String> getCategories_tags() {
        return this.categories_tags;
    }

    public void setCategories_tags(List<String> categories_tags) {
        this.categories_tags = categories_tags;
    }
    
    public List<String> getIngredients_tags() {
        return this.ingredients_tags;
    }

    public void setIngredients_tags(List<String> ingredients_tags) {
        this.ingredients_tags = ingredients_tags;
    }
    
    public LinkedTreeMap getLanguages_codes() {
        return this.languages_codes;
    }

    public void setLanguages_codes(Object languages_codes) {
        this.languages_codes = (LinkedTreeMap) languages_codes;
    }

    public LinkedTreeMap getNutriments() {
        return this.nutriments;
    }

    public void setNutriments(Object nutriments) {
        this.nutriments = (LinkedTreeMap) nutriments;
    }

    public Double getNova_group() {
        return this.nova_group;
    }

    public void setNova_group(Double nova_group) {
        this.nova_group = nova_group;
    }

    public Float getNutrition_score_uk() {
        return this.nutrition_score_uk;
    }

    public void setNutrition_score_uk(Float nutrition_score_uk) {
        this.nutrition_score_uk = nutrition_score_uk;
    }

    public Short getScore() {
        return this.score;
    }

    public void setScore(Short score) {
        this.score = score;
    }

    public HashMap<Short, HashMap<Short, String[]>> getSimilarity() {
        return this.similarity;
    }

    public void setSimilarity(HashMap<Short, HashMap<Short, String[]>> similarity) {
        this.similarity = similarity;
    }

    public List<String> getStores_tags() {
        return this.stores_tags;
    }

    public void setStores_tags(List<String> stores_tags) {
        this.stores_tags = stores_tags;
    }

    public List<String> getBrands_tags() {
        return this.brands_tags;
    }

    public void setBrands_tags(List<String> brands_tags) {
        this.brands_tags = brands_tags;
    }

    public LinkedTreeMap getImages() {
        return this.images;
    }

    public void setImages(LinkedTreeMap images) {
        this.images = images;
    }

}
