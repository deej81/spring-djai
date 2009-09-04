/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI;

import java.util.List;

/**
 *
 * @author deej
 */
public class UnitData {

    public Boolean IsBuilder=false;
    public Boolean IsAttacker=false;
    public Boolean IsScouter=false;
    public Boolean IsArtillery=false;
    public Boolean IsMobile=false;
    public String UnitName="";
    public List<UnitData> CanBuild;
    public BuildListRecord[] BuildLists;


}
