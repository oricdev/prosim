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


public class products extends Product {
    
    public products(String _id, String code, String product_name, String pnns_groups_1, List<String> countries_tags, List<String> categories_tags, List<String> ingredients_tags, List<String> brands_tags, List<String> stores_tags, Object languages_codes, Object nutriments, Double nova_group, Object images, Float nutrition_score_uk) {
        super(_id, code, product_name, pnns_groups_1, countries_tags, categories_tags, ingredients_tags, brands_tags, stores_tags, languages_codes, nutriments, nova_group, images, nutrition_score_uk);
    }
}
