/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.TaskManager;

import DJAI.DJAI;
import com.springrts.ai.oo.Unit;
import com.springrts.ai.oo.UnitDef;

/**
 *
 * @author deej
 */
public class TaskManager {

    public enum UnitNames{
        armcom,armlab,armck;
    }

    public String[] getTaskForUnit(Unit unit, DJAI ai){

        String name = unit.getDef().getName();
        String[] list = null;

        if(unit.getDef().isBuilder()){
            for(UnitDef def: unit.getDef().getBuildOptions()){
               //ai.sendTextMsg("unit: " + unit.getDef().getName()+ " can build: "+ def.getName());
            }
        }

        switch(UnitNames.valueOf(name)){
            case armcom: case armck:
                String[] ret = {"armmex","armsolar","armmex","armmex","armsolar","armlab","armrad","armmex","armsolar","armmex","armmex","armsolar","armmex","armmex","armmex"};
                return ret;
            case armlab:
                String[] ret2 = {"armck","armpw","armjeth","armpw","armwar","armpw","armpw","armwar","armpw","armpw","armpw"};
                return ret2;
            default:
                ai.sendTextMsg("no list found for: "+name);
                break;
        }

        return list;


    }


}
