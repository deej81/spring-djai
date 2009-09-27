/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Units;

import com.springrts.ai.oo.Unit;
import com.springrts.ai.oo.WeaponMount;

/**
 *
 * @author deej
 */


public class DJAIUnit{

    public Unit SpringUnit;
    public int BuildIndex = 0;
    
    private float m_AttackPotential = -1;
    public Unit Attaking;
    public int FrameCommand=0;

    public DJAIUnitDef DJUnitDef;

    public DJAIUnitDef CurrentlyBuildingDef;

    public Boolean IsFactoryOnWait=false;
    public Boolean IsBuilderDoingGuard=false;

    public int Guards = 0;

    public DJAIUnit(Unit springUnit, DJAIUnitDef djDef){
        SpringUnit = springUnit;
        DJUnitDef = djDef;

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
