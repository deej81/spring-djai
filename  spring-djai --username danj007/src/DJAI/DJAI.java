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
import com.springrts.ai.command.AttackUnitAICommand;
import com.springrts.ai.command.BuildUnitAICommand;
import com.springrts.ai.command.GuardUnitAICommand;
import com.springrts.ai.command.MoveUnitAICommand;
import com.springrts.ai.command.SendTextMessageAICommand;
import com.springrts.ai.oo.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DJAI extends com.springrts.ai.oo.AbstractOOAI {

    private OOAICallback m_Callback;
    private Unit commander;
    private ResourceHandler m_ResourceHandler = new ResourceHandler();
    private TaskManager m_TaskManager = new TaskManager();
    private static final int DEFAULT_ZONE = 0;
    private List<DJAIUnit> units = new ArrayList();
    private List<DJAIEnemyUnit> enemies = new ArrayList();
    private AIFloat3 basePos;
    private Boolean m_FirstFactory=true;
    private Unit m_FirstFactoryUnit;
    private Random m_Rand = new Random();

    public int handleEngineCommand(AICommand command) {
		return m_Callback.getEngine().handleCommand(
				com.springrts.ai.AICommandWrapper.COMMAND_TO_ID_ENGINE,
				-1, command);
	}
	public int sendTextMsg(String msg) {

		SendTextMessageAICommand msgCmd
				= new SendTextMessageAICommand(msg, DEFAULT_ZONE);
		return handleEngineCommand(msgCmd);
        //return 0;
	}

  @Override public int init(int teamId, OOAICallback callback){
      m_Callback = callback;

      loadModInfo();
      sendTextMsg("creating resource handler");
      m_ResourceHandler.initializeResources(m_Callback,this);
      sendTextMsg("resource handler created");
      return 0;
  }

  @Override public int unitFinished(Unit unit) {
    if (unit.getDef().getName().equals("armcom")){
       this.commander = unit;
       basePos = unit.getPos();
    }

    else if(m_FirstFactory&&unit.getDef().getName().equals("armlab")){
        m_FirstFactory = false;
        basePos = unit.getPos();
        m_FirstFactoryUnit = unit;
    }

    DJAIUnit newUnit = new DJAIUnit(unit);
    units.add(newUnit);
    try{
        if(unit.getDef().getName().equals("armpw")){
            if(m_Rand.nextInt(10)==0) {
                sendTextMsg("scout created");
                newUnit.IsScouter=true;
                newUnit.IsAttacker=false;
            }
        }
    }catch(Exception ex){
        sendTextMsg("error creating scout: "+ex.getMessage());
    }

    doNextBuild(newUnit);
    return 0;
}

  @Override
	public int unitIdle(Unit unit) {
            try{
                for(DJAIUnit djUn: units){
                    if(unit.equals(djUn.SpringUnit)){
                        doNextBuild(djUn);
                        break;
                    }
                }
            }catch(Exception ex){
                sendTextMsg("Failed to do next build: "+ex);

            }
            return 0; // signaling: OK
	}

  private void doNextBuild(DJAIUnit unit){
       if(unit.IsAttacker) return;
       if(unit.IsScouter){
           try{
               int rand = m_Rand.nextInt(m_ResourceHandler.MexSpots().size());
               AIFloat3 pos = m_ResourceHandler.MexSpots().get(rand).ExactLocation;

               AICommand command = new MoveUnitAICommand(unit.SpringUnit, -1, new ArrayList(), 1000, pos);
                this.m_Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
                return;
           }catch(Exception ex){
                sendTextMsg("scout command failed: "+ex.getMessage());

           }
        }
        List<UnitDef> unitDefs = this.m_Callback.getUnitDefs();
        UnitDef toBuild = null;
        String[] list = this.m_TaskManager.getTaskForUnit(unit.SpringUnit, this);
        String buildID = list[unit.BuildIndex];

        unit.BuildIndex++;
        if(unit.BuildIndex==list.length){
            if(unit.SpringUnit.equals(this.commander)){
                AICommand command = new GuardUnitAICommand(this.commander,0,new ArrayList(), 1000, m_FirstFactoryUnit );
                try{
                    this.m_Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
                }catch(Exception ex){
                    sendTextMsg("command failed: "+ex.getMessage());
                                   
                }
                return;
                
            }else{
                unit.BuildIndex=0;
            }
        }

        for (UnitDef def : unitDefs)

            if (def.getName().equals(buildID))
            {
                 toBuild = def;
                    break;
            }
            sendTextMsg("looking for build spot");
        AIFloat3 buildPos = m_ResourceHandler.getSpotforUnit(toBuild, m_Callback, unit.SpringUnit.getPos(),this);

       if(buildPos==null) return;
       AICommand command = new BuildUnitAICommand(unit.SpringUnit, -1,
           new ArrayList<AICommand.Option>(), 10000, toBuild,
           buildPos, 0);
       int retVal = this.m_Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE,
           -1, command);
  }

  private void distributeAttackers(int frame){
      int maxAttackers=2;
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
                        if(frame-unit.FrameCommand>60000){
                           //unit.Attaking=null;
                            //sendTextMsg("Unit Job Cleared");
                        }
                        if(attackers==maxAttackers){
                            sendTextMsg("Enough Attackers");
                            break;
                        }

                       
                        //sendTextMsg("ALLOCATING ATTACKER: " +enemy.getDef().getName());
                        if(unit==null){
                            sendTextMsg("Own Attacker dead");
                            units.remove(unit);
                            break;

                        }

                        if(unit.SpringUnit==null){
                            sendTextMsg("Own Attacker sp unit dead");
                            units.remove(unit);
                            break;

                        }

                        if(unit.IsAttacker&&unit.Attaking==null){
                                 try{
                                    enemy.BeingAttackedBy.add(unit);
                                    unit.Attaking = enemy.SpringUnit;
                                    unit.FrameCommand=frame;
                                    sendTextMsg("creating attack command");
                                    AICommand command = new AttackUnitAICommand(unit.SpringUnit, 0,new ArrayList(), 1000, enemy.SpringUnit );
                                
                                    this.m_Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
                                    //sendTextMsg("ATTACKING: " +enemy.getDef().getName());
                                    sendTextMsg("attack command sent");
                                    attackers++;
                                }catch(Exception ex){
                                    sendTextMsg("attack command failed: "+ex.getMessage());
                                    //enemies.remove(enemy);
                                }
                        }else if(!unit.IsAttacker){

                            sendTextMsg("not attacker");
                        }else if(unit.Attaking!=null){
                            sendTextMsg("already on a job");

                        }
                    }
                    sendTextMsg("Left Attacker Loop");
             }
  }

  @Override public int update(int frame) {
        if (frame % 300 == 0) {
            sendTextMsg("Update Time");
             try{
                 distributeAttackers(frame);
             }catch(Exception ex){
                 sendTextMsg("attack distribution failed: "+ex.getMessage());
             }

             sendTextMsg("Update Complete");
        }

   return 0;
}

    private void loadModInfo() {
        Mod modInfo = m_Callback.getMod();



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
        enemies.add(unit);
        sendTextMsg("registered enemy");

    }

    private void removeEnemy(Unit enemy){
        clearUnitsAttacking(enemy);
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
