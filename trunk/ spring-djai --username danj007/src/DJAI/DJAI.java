/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author deej
 */
package DJAI;

import DJAI.Resources.ResourceHandler;
import DJAI.TaskManager.TaskManager;
import DJAI.Utilities.VectorUtils;
import com.springrts.ai.AICommand;
import com.springrts.ai.AICommandWrapper;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.command.AttackAreaUnitAICommand;
import com.springrts.ai.command.AttackUnitAICommand;
import com.springrts.ai.command.BuildUnitAICommand;
import com.springrts.ai.command.GuardUnitAICommand;
import com.springrts.ai.command.MoveUnitAICommand;
import com.springrts.ai.command.SendTextMessageAICommand;
import com.springrts.ai.oo.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class DJAI extends com.springrts.ai.oo.AbstractOOAI {

    public OOAICallback Callback;
    private Unit commander;
    private ResourceHandler m_ResourceHandler = new ResourceHandler();
    private TaskManager m_TaskManager = new TaskManager();
    private static final int DEFAULT_ZONE = 0;
    private List<DJAIUnit> units = new ArrayList();
    private List<DJAIEnemyUnit> enemies = new ArrayList();
    private AIFloat3 basePos;
    private Boolean m_FirstFactory=true;
    public Unit FirstFactoryUnit;
    private Random m_Rand = new Random();

    public int handleEngineCommand(AICommand command) {
		return Callback.getEngine().handleCommand(
				com.springrts.ai.AICommandWrapper.COMMAND_TO_ID_ENGINE,
				-1, command);
	}
	public int sendTextMsg(String msg) {

		//SendTextMessageAICommand msgCmd
			//= new SendTextMessageAICommand(msg, DEFAULT_ZONE);
		//return handleEngineCommand(msgCmd);
        return 0;
	}

  @Override public int init(int teamId, OOAICallback callback){
      Callback = callback;

      loadModInfo();
      sendTextMsg("creating resource handler");
      m_ResourceHandler.initializeResources(Callback,this);
      sendTextMsg("resource handler created");
      return 0;
  }

  @Override public int unitFinished(Unit unit) {

    DJAIUnit newUnit = new DJAIUnit(unit);
    sendTextMsg("unit IsFactory: "+String.valueOf(newUnit.IsFactory));
    units.add(newUnit);
    try{
        if(unit.getDef().getName().equals("armpw")){
            if(m_Rand.nextInt(10)==0||units.size()<15) {
                sendTextMsg("scout created");
                newUnit.IsScouter=true;
                newUnit.IsAttacker=false;
            }
        }
    }catch(Exception ex){
        sendTextMsg("error creating scout: "+ex.getMessage());
    }

    if (unit.getDef().getName().equals("armcom")){
       this.commander = unit;
       basePos = unit.getPos();
    }

    else if(m_FirstFactory&&newUnit.IsFactory){
        m_FirstFactory = false;
        basePos = unit.getPos();
        FirstFactoryUnit = unit;
    }

    
    m_TaskManager.allocateTaskToUnit(newUnit, m_ResourceHandler, this);
    return 0;
}

  @Override
	public int unitIdle(Unit unit) {
            try{
                for(DJAIUnit djUn: units){
                    if(unit.equals(djUn.SpringUnit)){
                        m_TaskManager.allocateTaskToUnit(djUn, m_ResourceHandler, this);
                        break;
                    }
                }
            }catch(Exception ex){
                sendTextMsg("Failed to do next build: "+ex);

            }



            return 0; // signaling: OK
	}

    private void checkForEnableWaitingFactories() {
        for(DJAIUnit unit: units){
            if(unit.IsFactory) {
                sendTextMsg("found factory");
                if(unit.IsFactoryOnWait){
                    sendTextMsg("waiting factory unleashed");
                    unit.IsFactoryOnWait=false;
                    m_TaskManager.allocateTaskToUnit(unit, m_ResourceHandler, this);
                }
            }
        }
    }

  

  private void distributeAttackers(int frame){
      int boost=frame/15000;
      int maxAttackers=3+boost;
             int attackers=0;
             sendTextMsg("Enemy Count: "+String.valueOf(enemies.size()));
                for(DJAIEnemyUnit enemy:enemies){
                    sendTextMsg("In Enemy Loop");
                    if(enemy==null){
                        sendTextMsg("No Enemy");
                        break;
                    }
                     sendTextMsg("In Enemy Loop 1");
                    //sendTextMsg("ASSESSING: " +enemy.getDef().getName());
                    attackers=enemy.BeingAttackedBy.size();
                    sendTextMsg("current enemy attackers: "+String.valueOf(enemy.BeingAttackedBy.size()));
                     sendTextMsg("In Enemy Loop 2");
                     if(units==null) break;
                     sendTextMsg("In Enemy Loop 3");
                     sendTextMsg("current number of units: "+String.valueOf(units.size()));

                    for(DJAIUnit unit : units){
                        sendTextMsg("In Attackers Loop");
                        //if(frame-unit.FrameCommand>60000){
                           //unit.Attaking=null;
                            //sendTextMsg("Unit Job Cleared");
                        //}
                        if(attackers==maxAttackers){
                            sendTextMsg("Enough Attackers");
                            break;
                        }

                       
                        //sendTextMsg("ALLOCATING ATTACKER: " +enemy.getDef().getName());
                        if(unit==null){
                            sendTextMsg("Own Attacker dead");
                            //units.remove(unit);
                            break;

                        }

                        if(unit.SpringUnit==null){
                            sendTextMsg("Own Attacker sp unit dead");
                            //units.remove(unit);
                            break;

                        }

                        if(unit.IsAttacker&&frame-unit.FrameCommand>6000){
                                 try{
                                    
                                    sendTextMsg("creating attack command");
                                    AICommand command;
                                    //if(enemy.SpringUnit.getDef()==null){
                                        //no idea about this attack it
                                        if(enemy.SpringUnit.getPos().x==0){
                                            //enemies.remove(enemy);
                                            sendTextMsg("no enemy position");
                                            break;

                                        }//else{
                                            //command = new AttackUnitAICommand(unit.SpringUnit, 0,new ArrayList(), 1000, enemy.SpringUnit);
                                        //}

                                   // }else{
                                        command = new MoveUnitAICommand(unit.SpringUnit, 0,new ArrayList(), 1000, enemy.SpringUnit.getPos());

                                   // }
                                    
                                    int retVal = this.Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
                                    //sendTextMsg("ATTACKING: " +enemy.getDef().getName());
                                    sendTextMsg("attack command sent");
                                    if(retVal==0){
                                        enemy.BeingAttackedBy.add(unit);
                                        unit.Attaking = enemy.SpringUnit;
                                        unit.FrameCommand=frame;
                                        attackers++;
                                        sendTextMsg("attack command ok");
                                    }else{

                                        sendTextMsg("attack command failed");
                                        unit.FrameCommand=frame;
                                    }
                                    
                                }catch(Exception ex){
                                    sendTextMsg("attack command failed: "+ex.getMessage());
                                    //enemies.remove(enemy);
                                }
                        }else if(!unit.IsAttacker){

                            sendTextMsg("not attacker");
                        }else{
                            sendTextMsg("already on a job");

                        }
                    }
                    sendTextMsg("Left Attacker Loop");
             }
  }

  @Override public int update(int frame) {
        if (frame % 300 == 0) {
            sendTextMsg("Update Time yeah!");
             try{
                 distributeAttackers(frame);
             }catch(Exception ex){
                 sendTextMsg("attack distribution failed: "+ex.getMessage());
             }

            
             sendTextMsg("Update Complete");
        }
        if (frame % 400 == 0) {

            checkForEnableWaitingFactories();
        }

   return 0;
}

    private void loadModInfo() {
        Mod modInfo = Callback.getMod();



    }

    @Override
	public int enemyEnterLOS(Unit enemy) {
        sendTextMsg("enemyEnterLOS");
        registerEnemy(enemy);
       sendTextMsg("enemyEnterLOS OK");
		return 0; // signaling: OK
	}
    
    @Override
	public int enemyLeaveLOS(Unit enemy) {
        sendTextMsg("enemyLeaveLOS");
        removeEnemy(enemy);
        clearUnitsAttacking(enemy);
        sendTextMsg("enemyLeaveLOS OK");
		return 0; // signaling: OK
	}

	@Override
	public int enemyEnterRadar(Unit enemy) {
        sendTextMsg("enemyEnterRadar");
        registerEnemy(enemy);
        sendTextMsg("enemyEnterRadar OK");
		return 0; // signaling: OK
	}

	@Override
	public int enemyLeaveRadar(Unit enemy) {
        sendTextMsg("enemyLeaveRadar");
        removeEnemy(enemy);
        clearUnitsAttacking(enemy);
        sendTextMsg("enemyLeaveRadar OK");
		return 0; // signaling: OK
	}

	@Override
	public int enemyDamaged(Unit enemy, Unit attacker, float damage, AIFloat3 dir, WeaponDef weaponDef, boolean paralyzed) {
		return 0; // signaling: OK
	}

	@Override
	public int enemyDestroyed(Unit enemy, Unit attacker) {
        sendTextMsg("enemyDestroyed");
        removeEnemy(enemy);
        clearUnitsAttacking(enemy);
        sendTextMsg("enemyDestroyed OK");
		return 0; // signaling: OK
	}

    @Override
	public int unitDestroyed(Unit unit, Unit attacker) {
        sendTextMsg("unitDestroyed");
        if(unit.getDef().getName().equals("armmex")){
            sendTextMsg("freed mex spot");
            m_ResourceHandler.freeUpMexSpot(unit.getPos(), this);

        }
        try{
            for(DJAIUnit u:units){
            if(u.SpringUnit==null) {
                units.remove(u);
                sendTextMsg("removed null unit");
            }
            if(u.SpringUnit.equals(unit)){
                units.remove(u);
                sendTextMsg("removed dead unit");
                break;
            }

            updateEnemies(unit);
        }
        }catch(Exception ex){
            sendTextMsg("unit destoyed prob: "+ex.getMessage());
        }
        
        sendTextMsg("unitDestroyed OK");
		return 0; // signaling: OK
	}

    private void clearUnitsAttacking(Unit enemy){
        try{
            for(DJAIUnit unit : units){
               if(unit.IsAttacker){
                   if(unit.Attaking!=null){
                        if(unit.Attaking.equals(enemy)){
                           unit.Attaking=null;
                           unit.FrameCommand=0;
                           sendTextMsg("Cleared attacker for future use");
                        }
                    }
               }
        }
        }catch(Exception ex){
            sendTextMsg("clearUnitsAttacking: "+ex.getMessage());
        }
        
    }

    private void registerEnemy(Unit enemy){

        DJAIEnemyUnit unit = new DJAIEnemyUnit(enemy);
        if(!enemies.contains(unit))
            enemies.add(unit);
        sendTextMsg("registered enemy");

    }

    private void removeEnemy(Unit enemy){
        clearUnitsAttacking(enemy);
        if(enemy.getDef()!=null){
            if(enemy.getHealth()!=0&&!enemy.getDef().isAbleToMove()){
                //building keep it in list
                return;
            }
        }
        for(DJAIEnemyUnit de: enemies){
            if(de.SpringUnit==null){
                sendTextMsg("Cleared null enemy");
            }
            if(de.SpringUnit.equals(enemy)){
                enemies.remove(de);
                sendTextMsg("enemy removed");
                break;
            }
        }
    }

    private void updateEnemies(Unit unit) {
       for(DJAIEnemyUnit de: enemies){
           for(DJAIUnit dj: de.BeingAttackedBy){
               if(dj.SpringUnit==null){
                   de.BeingAttackedBy.remove(dj);
                   sendTextMsg("Cleared null attacker");
               }else if(dj.SpringUnit.equals(unit)){
                   de.BeingAttackedBy.remove(dj);
                   sendTextMsg("Cleared attacker");
                   break;
               }
               }
           }
    }
    



}
