/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Units;

import DJAI.Units.DJAIUnit;
import com.springrts.ai.oo.Unit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author deej
 */
public class DJAIEnemyUnit implements Comparator{


    public DJAIEnemyUnit(Unit springUnit){
        SpringUnit = springUnit;
    }
    public DJAIEnemyUnit(){}

    public Unit SpringUnit;
    public int BuildingsKilled=0;

    public List<DJAIUnit> BeingAttackedBy = new ArrayList();

    public int compare(Object arg0, Object arg1) {

        DJAIEnemyUnit a = (DJAIEnemyUnit)arg0;
        DJAIEnemyUnit b = (DJAIEnemyUnit)arg1;

        if(a.SpringUnit==null&&b.SpringUnit==null) return 0;

        if(a.SpringUnit==null) return -1;
        if(b.SpringUnit==null) return 1;

        if(a.BuildingsKilled==b.BuildingsKilled){
        
            float aExp = a.SpringUnit.getExperience();
            float bExp = b.SpringUnit.getExperience();

            if(aExp==bExp) return 0;
            if(aExp > bExp) return 1;

            
        }else{
            if(a.BuildingsKilled>b.BuildingsKilled) return 1;
        }
        return -1;
    }


}
