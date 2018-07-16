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

/**
 * Product Similarity (Pro/sim)
 *
 * @author oric
 */
@Indexes({
    @Index(fields = {
        @Field("code")
        , @Field("countries_tags")
        , @Field("product_name")
        , @Field("nutrition_score")})})
@Entity
public class Prosim extends BaseEntity {

    private String code;
    private String product_name;
    private List<String> countries_tags;
    private List<String> categories_tags;
    private List<String> stores_tags;
    private List<String> brands_tags;
    private LinkedTreeMap nutriments;
    private LinkedTreeMap images;
    private Float nutrition_score_uk;
    private Short nutrition_score;
    private HashMap<Short, HashMap<Short, String[]>> similarity;

    public Prosim() {
    }

    public Prosim(IProduct product) {
        ProductExt prodExt = (ProductExt) product;
        this.code = prodExt.getCode();
        this.product_name = prodExt.getProduct_name();
        this.categories_tags = prodExt.getCategories_tags();
        this.countries_tags = prodExt.getCountries_tags();
        this.stores_tags = prodExt.getStores_tags();
        this.brands_tags = prodExt.getBrands_tags();
        this.nutriments = (LinkedTreeMap) prodExt.getNutriments();
        this.images = (LinkedTreeMap) prodExt.getImages();
        this.nutrition_score_uk = prodExt.getNutrition_score_uk();
        this.nutrition_score = prodExt.getNutrition_score();
        // similarity section
        this.similarity = new HashMap<>();
        HashMap<Short, String[]> sim_nutrition_score = new HashMap<>();
        sim_nutrition_score.put(prodExt.getOther_nutrition_score(), new String[]{prodExt.getOther_code()});
        this.similarity.put(prodExt.getSimilarity_with_product(), sim_nutrition_score);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public List<String> getCountries_tags() {
        return countries_tags;
    }

    public void setCountries_tags(List<String> countries_tags) {
        this.countries_tags = countries_tags;
    }

    public List<String> getCategories_tags() {
        return categories_tags;
    }

    public void setCategories_tags(List<String> categories_tags) {
        this.categories_tags = categories_tags;
    }

    public LinkedTreeMap getNutriments() {
        return nutriments;
    }

    public void setNutriments(Object nutriments) {
        this.nutriments = (LinkedTreeMap) nutriments;
    }

    public Float getNutrition_score_uk() {
        return nutrition_score_uk;
    }

    public void setNutrition_score_uk(Float nutrition_score_uk) {
        this.nutrition_score_uk = nutrition_score_uk;
    }

    public Short getNutrition_score() {
        return nutrition_score;
    }

    public void setNutrition_score(Short nutrition_score) {
        this.nutrition_score = nutrition_score;
    }

    public HashMap<Short, HashMap<Short, String[]>> getSimilarity() {
        return similarity;
    }

    public void setSimilarity(HashMap<Short, HashMap<Short, String[]>> similarity) {
        this.similarity = similarity;
    }

    public List<String> getStores_tags() {
        return stores_tags;
    }

    public void setStores_tags(List<String> stores_tags) {
        this.stores_tags = stores_tags;
    }

    public List<String> getBrands_tags() {
        return brands_tags;
    }

    public void setBrands_tags(List<String> brands_tags) {
        this.brands_tags = brands_tags;
    }

    public LinkedTreeMap getImages() {
        return images;
    }

    public void setImages(LinkedTreeMap images) {
        this.images = images;
    }

}
