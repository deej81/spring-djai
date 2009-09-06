/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI;

import com.springrts.ai.oo.Unit;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author deej
 */
public class DJAIEnemyUnit {

    public DJAIEnemyUnit(Unit springUnit){
        SpringUnit = springUnit;
    }

    public Unit SpringUnit;

    public List<DJAIUnit> BeingAttackedBy = new ArrayList();


}
