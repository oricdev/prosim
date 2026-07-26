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

/* NOTE: only QUICK_INIT is used!
 *
 * This mode is to be set in the config.xml file.
 * The mode is VERY IMPORTANT and should be handled with great care.
 * First thing to understand is that the current process inserts into the Mongo db a subset (data-matrix set) of the overall grid of products.
 * Hence a product in db with similarity field set (similar products) may be inserted again the db with extra similar products coming from the partial intersection process.
 * In this case, we may want:
 * - mode QUICK_INIT: to accelerate the import process: this mode ONLY merges the content of the "similarity" data when a product code (with similar products) exists already in the database, while updating this entry with a product to import (same code, but extra similar products attached). All difference in standard OpenFoodFacts.org fields will be ignored (countries_tags, categories_tags, etc.)
 * - mode FULL_CHECK_UPDATE: to ensure the overall consistency of the data in the db. Hence , in addition to what is performed in the QUICK_INIT mode, standard OFF data are also checked and updated if necessary.
*/
package org.openfoodfacts.mongo;

/**
 *
 * @author olivier
 */
public enum EnumMongoImportMode {
    QUICK_INIT,
    FULL_CHECK_UPDATE
}
