/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Resources;

import DJAI.Units.DJAIUnitDef;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.oo.Map;
import com.springrts.ai.oo.UnitDef;

/**
 *
 * @author deej
 */
public class ResourceSquare {

    public ResourceSquare(){}

    public ResourceSquare(AIFloat3 location){
        ExactLocation = location;
    }
    
    public Boolean Occupied=false;
    public UnitDef OccupiedBy;
    public AIFloat3 ExactLocation;

}
