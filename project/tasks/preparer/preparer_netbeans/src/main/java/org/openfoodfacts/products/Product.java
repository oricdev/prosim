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

import com.google.gson.internal.LinkedTreeMap;
import java.util.List;


public class Product implements IProduct {

    public Product(String _id, String code, String product_name, String pnns_groups_1, List<String> countries_tags, List<String> categories_tags, List<String> ingredients_tags, List<String> brands_tags, List<String> stores_tags, Object languages_codes, Object nutriments, Double nova_group, Object images, Float nutrition_score_uk) {
        this.code = code;
        this.product_name = product_name;
        this.pnns_groups_1 = pnns_groups_1;
        this.countries_tags = countries_tags;
        this.categories_tags = categories_tags;
        this.ingredients_tags = ingredients_tags;
        this.brands_tags = brands_tags;
        this.languages_codes = languages_codes;
        this.stores_tags = stores_tags;
        this.nutriments = nutriments;
        this.nova_group = nova_group;
        this.images = images;
        this.nutrition_score_uk = nutrition_score_uk;
        this._id = _id;
    }

    protected String code;
    protected List<String> countries_tags;
    protected List<String> categories_tags;
    protected List<String> ingredients_tags;
    protected List<String> stores_tags;
    protected List<String> brands_tags;
    protected Object languages_codes;
    protected String product_name;
    protected String pnns_groups_1;
    protected Object nutriments;
    protected Double nova_group;
    protected Object images;
    protected Float nutrition_score_uk;
    protected String _id;
    
    
    /*
    * Extracts info from nutriments
    */
    public void prepare() {
        if (null != this.getNutriments() && ((LinkedTreeMap)this.getNutriments()).containsKey("nutrition-score-uk") ) {
            this.nutrition_score_uk = Float.parseFloat(((LinkedTreeMap)this.getNutriments()).get("nutrition-score-uk").toString());
        }
    }

    public String getId() {
        return this._id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public Object getLanguages_codes() {
        return this.languages_codes;
    }

    public void setLanguages_codes(Object languages_codes) {
        this.languages_codes = languages_codes;
    }
    
    public Object getNutriments() {
        return this.nutriments;
    }

    public void setNutriments(Object nutriments) {
        this.nutriments = nutriments;
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

    public String getProduct_name() {
        return this.product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getPnns_groups_1() {
        return this.pnns_groups_1;
    }

    public void setPnns_groups_1(String pnns_groups_1) {
        this.pnns_groups_1 = pnns_groups_1;
    }
    
    public Object getImages() {
        return this.images;
    }

    public void setImages(Object images) {
        this.images = images;
    }

}
