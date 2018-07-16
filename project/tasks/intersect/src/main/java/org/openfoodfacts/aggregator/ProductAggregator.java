/*
 * Useful links:
 * - merge of HasMaps: //https://stackoverflow.com/questions/4299728/how-can-i-combine-two-hashmap-objects-containing-the-same-types
 *
 * The structure of a ProductAggregator is typically the one retrieved from the Mongo-Db after a find() without any projection of fields.
 * i.e. it is the structure of a Prosim object which is made of 2 distinct parts:
 * - the standard OFF (OpenFoodFacts) fields: code, categories_tags, countries_tags, etc.
 * - the extendion of OFF with similar products grouped by percentage of similarity and nutrition-score. This is attached under Prosim::similarity
 * The purpose of the current class is to manage insertion, merge, deletion of this similarity part in order to manage/extend/update the database with new entries
 */
package org.openfoodfacts.aggregator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.openfoodfacts.entities.Prosim;

/**
 *
 * @author oric
 */
public class ProductAggregator {
    final static Logger logger = Logger.getLogger(ProductAggregator.class);
    
    public HashMap<String, Prosim> products = new HashMap<>();

    public void aggregate(Prosim other_prosim) {
        String code_product = other_prosim.getCode();        
        if (!this.products.containsKey(code_product)) {
            // new entry
            this.products.put(code_product, other_prosim);
        } else {
            Prosim this_prosim = this.products.get(code_product);
            HashMap<Short, HashMap<Short, String[]> > this_sim = this_prosim.getSimilarity();
            HashMap<Short, HashMap<Short, String[]> > other_sim = other_prosim.getSimilarity();
            
            // start aggregation (grouped by similarity percentage, and then by nurition-score
            other_sim.keySet().forEach((percentage) -> {
                if (!this_sim.containsKey(percentage)) {
                    // add new similarity entry
                    this_sim.put(percentage, new HashMap<>());
                }
                other_sim.get(percentage).keySet().forEach((nutrition_score) -> {
                    HashMap<Short, String[]> this_grouped_nutrition_scores = this_sim.get(percentage);
                    if (!this_grouped_nutrition_scores.containsKey(nutrition_score)) {
                        this_grouped_nutrition_scores.put(nutrition_score, new String[]{});
                    }
                    // Add to this current similarity all product codes found in other similarity per percentage and nutrition-score
                    Arrays.asList(other_sim.get(percentage).get(nutrition_score)).forEach((other_code)-> {
                            String[] t_codes = this_grouped_nutrition_scores.get(nutrition_score);
                            ArrayList<String> existing_codes = new ArrayList<>(Arrays.asList(t_codes));
                            if (!existing_codes.contains(other_code)) {
                                existing_codes.add(other_code);
                                // replace array of codes
                                this_grouped_nutrition_scores.put(nutrition_score, existing_codes.stream().toArray(String[]::new));
                            }
                        });
                    this_sim.put(percentage, this_grouped_nutrition_scores);                            
                });
            });
            this_prosim.setSimilarity(this_sim);
            this_prosim.setId(other_prosim.getId());
            this.products.put(code_product, this_prosim);
        }
    }
}
