/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author deej
 */
package DJAI;

import DJAI.Units.DJAIEnemyUnit;
import DJAI.Units.DJAIUnit;
import DJAI.Resources.ResourceHandler;
import DJAI.TaskManager.TaskManager;
import DJAI.Units.DJAIUnitDef;
import DJAI.Units.DJAIUnitDefManager;
import DJAI.Units.UnitManager;
import DJAI.Utilities.VectorUtils;
import com.springrts.ai.AICommand;
import com.springrts.ai.AICommandWrapper;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.command.AddPointDrawAICommand;
import com.springrts.ai.command.AttackAreaUnitAICommand;
import com.springrts.ai.command.AttackUnitAICommand;
import com.springrts.ai.command.BuildUnitAICommand;
import com.springrts.ai.command.CallLuaRulesAICommand;
import com.springrts.ai.command.GuardUnitAICommand;
import com.springrts.ai.command.MoveUnitAICommand;
import com.springrts.ai.command.SendTextMessageAICommand;
import com.springrts.ai.oo.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class DJAI extends com.springrts.ai.oo.AbstractOOAI {

    public OOAICallback Callback;
    private Unit commander;
    public ResourceHandler ResourceHandler = new ResourceHandler();
    private TaskManager m_TaskManager = new TaskManager();
    private static final int DEFAULT_ZONE = 0;
    private List<DJAIEnemyUnit> enemies = new ArrayList();
    private List<DJAIEnemyUnit> buildingKillers = new ArrayList<DJAIEnemyUnit>();
    public AIFloat3 basePos;
    private Boolean m_FirstFactory=true;
    public Unit FirstFactoryUnit;
    private Random m_Rand = new Random();
    public DJAIUnitDefManager DefManager;
    public UnitManager DJUnitManager;

    public boolean Debug=false;
    public boolean DoMapPings=false;

    public int handleEngineCommand(AICommand command) {
		return Callback.getEngine().handleCommand(
				com.springrts.ai.AICommandWrapper.COMMAND_TO_ID_ENGINE,
				-1, command);
	}

    public void sendTextMsg(String msg, boolean noDebugCheck){
        SendTextMessageAICommand msgCmd
			= new SendTextMessageAICommand(msg, DEFAULT_ZONE);
		handleEngineCommand(msgCmd);
    }

	public int sendTextMsg(String msg) {
            if(Debug){
		SendTextMessageAICommand msgCmd
			= new SendTextMessageAICommand(msg, DEFAULT_ZONE);
		return handleEngineCommand(msgCmd);
            }else{
                return 0;
            }
	}

        public void MapPing(AIFloat3 pos, String message){
            AICommand command = new AddPointDrawAICommand(pos, message);
            handleEngineCommand(command);
        }

  @Override public int init(int teamId, OOAICallback callback){
      Callback = callback;
      DefManager = new DJAIUnitDefManager(this);
      DJUnitManager = new UnitManager();
      loadModInfo();
      sendTextMsg("creating resource handler");
      ResourceHandler.initializeResources(Callback,this);
      sendTextMsg("resource handler created");
      return 0;
  }

  @Override public int unitFinished(Unit unit) {

    DJAIUnitDef def = DefManager.getUnitDefForUnit(unit.getDef());

    DJAIUnit newUnit = new DJAIUnit(unit, def);
    sendTextMsg("unit IsFactory: "+String.valueOf(newUnit.DJUnitDef.IsFactory));
    
    DJUnitManager.UnitBuildingCompleted(newUnit,this);

    
    if (unit.getDef().getName().equals("armcom")){
       this.commander = unit;
       basePos = unit.getPos();
    }

    else if(m_FirstFactory&&newUnit.DJUnitDef.IsFactory){
        m_FirstFactory = false;
        basePos = unit.getPos();
        FirstFactoryUnit = unit;
    }

    //if(def.IsBuilder&&!def.IsCommander){
     //   AIFloat3 moveTo = unit.getPos();
     //   moveTo.x= moveTo.x-15;
     //   AICommand command = new MoveUnitAICommand(unit, 0,new ArrayList(), 1000, moveTo);
      //  handleEngineCommand(command);
    //}

    
    m_TaskManager.allocateTaskToUnit(newUnit, ResourceHandler, this);
    return 0;
}

  @Override
	public int unitIdle(Unit unit) {
            try{
                
                
                DJAIUnitDef def = DefManager.getUnitDefForUnit(unit.getDef());
                
                List<DJAIUnit> collection = null;
                if(def.IsScouter) collection = DJUnitManager.Scouters;
                else if(def.IsCommander) collection = DJUnitManager.Commanders;
                else if(def.IsBuilder) collection = DJUnitManager.Builders;
                else if(def.IsFactory) collection = DJUnitManager.Factories;
                else if(def.IsAttacker) collection = DJUnitManager.Attackers;
                else{
                    return 0;
                }
                
                for(DJAIUnit djUn: collection){
                    if(unit.equals(djUn.SpringUnit)){
                        if(def.IsAttacker){
                            //idle attacker - add back to pool
                            djUn.FrameCommand=0;
                            djUn.Attaking=null;
                        }else{
                            m_TaskManager.allocateTaskToUnit(djUn, ResourceHandler, this);
                        }
                        break;
                    }
                }
            }catch(Exception ex){
                sendTextMsg("Failed to do next build: "+ex);

            }



            return 0; // signaling: OK
	}

    private void checkForEnableWaitingFactories() {
        for(DJAIUnit unit: DJUnitManager.Factories){
            if(unit.DJUnitDef.IsFactory) {
                sendTextMsg("found factory");
                if(unit.IsFactoryOnWait){
                    sendTextMsg("waiting factory unleashed");
                    unit.IsFactoryOnWait=false;
                    m_TaskManager.allocateTaskToUnit(unit, ResourceHandler, this);
                }
            }

        }
    }

    private void checkGuardingBuilders(){
        for(DJAIUnit unit:DJUnitManager.Builders){


            if(unit.DJUnitDef.IsBuilder){
                if(unit.IsBuilderDoingGuard){
                    unit.IsBuilderDoingGuard=false;
                    m_TaskManager.allocateTaskToUnit(unit, ResourceHandler, this);
                }
            }
        }

    }

  private void distributeAttackers(int frame){
      int boost=frame/18000;
      int maxCommitAttackers = 2+boost;
      int maxAttackers=DJUnitManager.Attackers.size()/(enemies.size()+1);
      int maxUpdateAmount=100;
      int currentUpdateAmount=0;
      int commandExecuteTime=1000;
      if(frame>10000) commandExecuteTime = 6000;
      if(frame>50000) commandExecuteTime = 12000;

      if(maxAttackers<1) maxAttackers=3+boost;
             int attackers=0;
             sendTextMsg("Enemy Count: "+String.valueOf(enemies.size()));
                for(DJAIEnemyUnit enemy:enemies){
                    if(enemy.SpringUnit.getTeam()==commander.getTeam()){
                        //AICommand command = new AddPointDrawAICommand(enemy.SpringUnit.getPos(), "ENEMY WRONG");
                       // handleEngineCommand(command);
                        //enemies.remove(enemy);
                        continue;
                    }
                    //if(currentUpdateAmount>maxUpdateAmount) break;
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
                     sendTextMsg("current number of units: "+String.valueOf(DJUnitManager.Attackers.size()));

                    for(int i=DJUnitManager.Attackers.size()-1;i>=0;i--){
                        currentUpdateAmount++;
                        DJAIUnit unit = DJUnitManager.Attackers.get(i);
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

                        if(unit.DJUnitDef.IsAttacker&&frame-unit.FrameCommand>12000){
                                 try{
                                    
                                    sendTextMsg("creating attack command");
                                    AICommand command;
                                    if(enemy.SpringUnit.getDef()==null){
                                        //no idea about this move here
                                        if(enemy.SpringUnit.getPos().x==0){
                                            //enemies.remove(enemy);
                                            sendTextMsg("no enemy position");
                                            break;

                                        }else{
                                            if(DoMapPings)
                                                MapPing(enemy.SpringUnit.getPos(), "Null Enemy");
                                            //command = new MoveUnitAICommand(unit.SpringUnit, 0,new ArrayList(), 1000, enemy.SpringUnit.getPos());
                                            command = new AttackUnitAICommand(unit.SpringUnit, 0,new ArrayList(), 1000, enemy.SpringUnit);
                                        }

                                    }else{
                                        if(DoMapPings)
                                            MapPing(enemy.SpringUnit.getPos(), "Attacking Enemy");
                                            command = new AttackUnitAICommand(unit.SpringUnit, 0,new ArrayList(), 1000, enemy.SpringUnit);
                                    }
                                    
                                    int retVal = this.Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
                                    //sendTextMsg("ATTACKING: " +enemy.getDef().getName());
                                    sendTextMsg("attack command sent");
                                    if(retVal==0){
                                        enemy.BeingAttackedBy.add(unit);
                                        unit.Attaking = enemy.SpringUnit;
                                        
                                        //don't commit too many, this way can call them back
                                        if(attackers<=maxCommitAttackers){
                                            unit.FrameCommand=frame;
                                            sendTextMsg("attacker committed");
                                        }
                                            

                                        attackers++;
                                        sendTextMsg("attack command ok");
                                    }else{

                                        sendTextMsg("attack command failed");
                                        //unit.FrameCommand=frame;
                                    }
                                    
                                }catch(Exception ex){
                                    sendTextMsg("attack command failed: "+ex.getMessage());
                                    //enemies.remove(enemy);
                                }
                        }else if(!unit.DJUnitDef.IsAttacker){

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
        if (frame % 200 == 0) {

            checkForEnableWaitingFactories();
        }
        //if early game don't wait with guarding get on with it
        if (frame % 650 == 0 || (frame<36000)&&(frame%60==0)) {

            checkGuardingBuilders();
        }
        
        if (frame % 500 == 0) {

            Collections.sort(enemies, new DJAIEnemyUnit());
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
        //removeEnemy(enemy);
        //clearUnitsAttacking(enemy);
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
        DJAIUnitDef def= DefManager.getUnitDefForUnit(enemy.getDef());
        if(def.IsExtractor){
            ResourceHandler.freeUpMexSpot(enemy.getPos(), this);
        }
        sendTextMsg("enemyDestroyed OK");
		return 0; // signaling: OK
	}

    @Override
	public int unitDestroyed(Unit unit, Unit attacker) {


        sendTextMsg("unitDestroyed");
        if(DefManager.getUnitDefForUnit(unit.getDef()).IsExtractor){
            sendTextMsg("freed mex spot");
            ResourceHandler.freeUpMexSpot(unit.getPos(), this);
            upgradeEnemyStatus(attacker);

        }

        try{

            DJUnitManager.UnitDestroyed(unit,this);
            updateEnemies(unit);
        }catch(Exception ex){
            sendTextMsg("unit destoyed prob: "+ex.getMessage());
        }


        sendTextMsg("unitDestroyed OK");
		return 0; // signaling: OK
	}

    private void clearUnitsAttacking(Unit enemy){
        try{
            for(DJAIUnit unit : DJUnitManager.Attackers){
               if(unit.DJUnitDef.IsAttacker){
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

        //don't go chasing aircraft around for now
        if(enemy.getDef()!=null){
            if(enemy.getDef().isAbleToFly()) return;
        }

        DJAIEnemyUnit unit = new DJAIEnemyUnit(enemy);
        if(!enemies.contains(unit))
            enemies.add(unit);
        sendTextMsg("registered enemy");

    }

    private void upgradeEnemyStatus(Unit enemy){
        for(DJAIEnemyUnit de: enemies){
            if(de.SpringUnit.equals(enemy)){
                de.BuildingsKilled++;
                break;
            }
        }
    }

    private void removeEnemy(Unit enemy){
        clearUnitsAttacking(enemy);
        //if(enemy.getDef()!=null){
            //if(enemy.getHealth()!=0&&!enemy.getDef().isAbleToMove()){
                //building keep it in list
             //   return;
            //}
        //}
        List<DJAIEnemyUnit> nullEnemies = new ArrayList();
        for(DJAIEnemyUnit de: enemies){
            if(de.SpringUnit==null){
                sendTextMsg("Cleared null enemy");
                nullEnemies.add(de);
            }else if(de.SpringUnit.getTeam()==commander.getTeam()){
                sendTextMsg("Cleared queer enemy");
                nullEnemies.add(de);
            }
            if(de.SpringUnit.equals(enemy)){
                enemies.remove(de);
                sendTextMsg("enemy removed");
                break;
            }
        }
        enemies.removeAll(nullEnemies);
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
