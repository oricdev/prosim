/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openfoodfacts.utils;

/**
 *
 * @author olivier
 */
public class Stats {
    public static void outputInXml(String stats_fname, long pos_width, double nb_width, long pos_height, double nb_height, double progress_percentage) {
        // TODO
        /* use 
        <stats>
          <width_nb></>
          <width_pos></>
          <height_nb></>
          <height_pos></>
          <progress></progress>
        </stats>
        */
        // I C I // ICI
        CfgMgr.addRootNodeInXml(stats_fname, "stats");
        CfgMgr.addChildNodeInXml(stats_fname, "width_nb", ((Double)nb_width).toString());
        CfgMgr.addChildNodeInXml(stats_fname, "width_pos", ((Long)pos_width).toString());
        CfgMgr.addChildNodeInXml(stats_fname, "height_nb", ((Double)nb_height).toString());
        CfgMgr.addChildNodeInXml(stats_fname, "height_pos", ((Long)pos_height).toString());
    }
    
}
