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
    
    private float m_AttackPotential = -1;
    public Unit Attaking;
    public int FrameCommand=0;

    public Boolean IsScouter=false;
    public Boolean IsBuilder=false;
    public Boolean IsFactory=false;
    public Boolean IsCommander=false;
    public Boolean IsAttacker=false;
    public Boolean IsExtractor=false;

    public Boolean IsFactoryOnWait=false;

    public DJAIUnit(Unit springUnit){
        SpringUnit = springUnit;

        IsCommander = springUnit.getDef().isCommander();
        IsFactory = springUnit.getDef().getSpeed()==0&&springUnit.getDef().getBuildOptions().size()>0;
        IsBuilder = springUnit.getDef().isBuilder()&&!IsFactory;
        IsAttacker = springUnit.getDef().isAbleToFight()&&!IsCommander&&!IsFactory&&!IsBuilder;
        IsScouter = springUnit.getDef().getName().equals("armflea");

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
