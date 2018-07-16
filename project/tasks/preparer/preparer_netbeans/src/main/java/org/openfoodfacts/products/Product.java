package org.openfoodfacts.products;

import com.google.gson.internal.LinkedTreeMap;
import java.util.List;

/**
 *
 * @author oric
 */
public class Product implements IProduct {

    public Product(String _id, String code, String product_name, List<String> countries_tags, List<String> categories_tags, List<String> brands_tags, List<String> stores_tags, Object nutriments, Object images, Float nutrition_score_uk) {
        this.code = code;
        this.product_name = product_name;
        this.countries_tags = countries_tags;
        this.categories_tags = categories_tags;
        this.brands_tags = brands_tags;
        this.stores_tags = stores_tags;
        this.nutriments = nutriments;
        this.images = images;
        this.nutrition_score_uk = nutrition_score_uk;
        this._id = _id;
    }

    private String code;
    private List<String> countries_tags;
    private List<String> categories_tags;
    private List<String> stores_tags;
    private List<String> brands_tags;
    private String product_name;
    private Object nutriments;
    private Object images;
    private Float nutrition_score_uk;
    private String _id;
    
    
    /*
    * Extracts info from nutriments
    */
    public void prepare() {
        if (null != this.getNutriments() && ((LinkedTreeMap)this.getNutriments()).containsKey("nutrition-score-uk") ) {
            this.nutrition_score_uk = Float.parseFloat(((LinkedTreeMap)this.getNutriments()).get("nutrition-score-uk").toString());
        }
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public Object getNutriments() {
        return this.nutriments;
    }

    public void setNutriments(Object nutriments) {
        this.nutriments = nutriments;
    }

    public Float getNutrition_score_uk() {
        return nutrition_score_uk;
    }

    public void setNutrition_score_uk(Float nutrition_score_uk) {
        this.nutrition_score_uk = nutrition_score_uk;
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

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public Object getImages() {
        return images;
    }

    public void setImages(Object images) {
        this.images = images;
    }

}
