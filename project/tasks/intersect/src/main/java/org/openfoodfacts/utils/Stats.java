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
package org.openfoodfacts.utils;


public class Stats {
    public static void outputInXml(String stats_fname, long pos_width, double nb_width, long pos_height, double nb_height, double progress_percentage) {
        /* use 
        <stats>
          <width_nb></>
          <width_pos></>
          <height_nb></>
          <height_pos></>
          <progress></progress>
        </stats>
        */
        CfgMgr.addRootNodeInXml(stats_fname, "stats");
        CfgMgr.addChildNodeInXml(stats_fname, "width_nb", ((Double)nb_width).toString());
        CfgMgr.addChildNodeInXml(stats_fname, "width_pos", ((Long)pos_width).toString());
        CfgMgr.addChildNodeInXml(stats_fname, "height_nb", ((Double)nb_height).toString());
        CfgMgr.addChildNodeInXml(stats_fname, "height_pos", ((Long)pos_height).toString());
    }
    
}
