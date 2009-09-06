/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI;

import com.springrts.ai.oo.Unit;
import com.springrts.ai.oo.WeaponMount;

/**
 *
 * @author deej
 */


public class DJAIUnit{

    public Unit SpringUnit;
    public int BuildIndex = 0;
    public Boolean IsAttacker=false;
    private float m_AttackPotential = -1;
    public Unit Attaking;
    public int FrameCommand=0;
    public Boolean IsScouter=false;

    public DJAIUnit(Unit springUnit){
        SpringUnit = springUnit;
        IsAttacker = !springUnit.getDef().isBuilder();
        //IsScouter = springUnit.getDef().getName().equals("armpw");

    }

    public String CurrentlyBuilding;

    public float AttackPotential(){

        if(m_AttackPotential==-1) calculateAttackPotential();

        return m_AttackPotential;
    }

    public Boolean CanAttackAircraft(){
        return false;
    }

    private void calculateAttackPotential() {
        m_AttackPotential=0;
       for(WeaponMount weapon:this.SpringUnit.getDef().getWeaponMounts() ){

            m_AttackPotential+=weapon.getWeaponDef().getDynDamageExp();
       }
    }



}
