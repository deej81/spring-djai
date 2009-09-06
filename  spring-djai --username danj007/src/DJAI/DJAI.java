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
import com.springrts.ai.command.SendTextMessageAICommand;
import com.springrts.ai.oo.*;
import java.util.ArrayList;
import java.util.List;

public class DJAI extends com.springrts.ai.oo.AbstractOOAI {

    private OOAICallback m_Callback;
    private Unit commander;
   private ResourceHandler m_ResourceHandler = new ResourceHandler();
    private TaskManager m_TaskManager = new TaskManager();
    private static final int DEFAULT_ZONE = 0;
    private List<DJAIUnit> units = new ArrayList();
    private List<Unit> enemies = new ArrayList();

    public int handleEngineCommand(AICommand command) {
		return m_Callback.getEngine().handleCommand(
				com.springrts.ai.AICommandWrapper.COMMAND_TO_ID_ENGINE,
				-1, command);
	}
	public int sendTextMsg(String msg) {

		SendTextMessageAICommand msgCmd
				= new SendTextMessageAICommand(msg, DEFAULT_ZONE);
		return handleEngineCommand(msgCmd);
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
    if (unit.getDef().getName().equals("armcom"))
       this.commander = unit;

    DJAIUnit newUnit = new DJAIUnit(unit);
    units.add(newUnit);
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
      List<UnitDef> unitDefs = this.m_Callback.getUnitDefs();
        UnitDef toBuild = null;
        String[] list = this.m_TaskManager.getTaskForUnit(unit.SpringUnit, this);
        String buildID = list[unit.BuildIndex];

        unit.BuildIndex++;
        if(unit.BuildIndex==list.length) unit.BuildIndex=0;

        for (UnitDef def : unitDefs)

            if (def.getName().equals(buildID))
            {
                 toBuild = def;
                    break;
            }
            sendTextMsg("looking for build spot");
        AIFloat3 buildPos = m_ResourceHandler.getSpotforUnit(toBuild, m_Callback, this.commander.getPos(),this);

//        if(buildID.equals("armmex")){
//            List<Resource> resourceList = m_Callback.getResources();
//            Resource metal=null;
//            for(Resource r: resourceList){
//                if(r.getName().equalsIgnoreCase("metal")){
//
//                    metal = r;
//                }
//
//            }
//
//            buildPos = m_Callback.getMap().getResourceMapSpotsPositions(metal).get(0);
//
//        }
        if(buildPos==null) return;
       AICommand command = new BuildUnitAICommand(unit.SpringUnit, -1,
           new ArrayList<AICommand.Option>(), 10000, toBuild,
           buildPos, 0);
       int retVal = this.m_Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE,
           -1, command);
  }

  @Override public int update(int frame) {
        if (frame % 300 == 0) {
             double botherDistance=1500;

                for(Unit enemy:enemies){
                 
                    for(DJAIUnit unit : units){
                        if(unit.IsAttacker){

                            if(VectorUtils.CalcDistance(enemy.getPos(), unit.SpringUnit.getPos())<botherDistance){

                                AICommand command = new AttackUnitAICommand(unit.SpringUnit, 0,new ArrayList(), 1000, enemy );
                                try{
                                    this.m_Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
                                    sendTextMsg("ATTACKING: " +enemy.getDef().getName());
                                }catch(Exception ex){
                                    sendTextMsg("command failed: "+ex.getMessage());
                                    enemies.remove(enemy);
                                }
                            }
                        }

                    }
             }

            
        }

   return 0;
}

    private void loadModInfo() {
        Mod modInfo = m_Callback.getMod();



    }

    @Override
	public int enemyEnterLOS(Unit enemy) {

        enemies.add(enemy);
       
		return 0; // signaling: OK
	}
    
    @Override
	public int enemyLeaveLOS(Unit enemy) {
        enemies.remove(enemy);
		return 0; // signaling: OK
	}

	@Override
	public int enemyEnterRadar(Unit enemy) {
		return 0; // signaling: OK
	}

	@Override
	public int enemyLeaveRadar(Unit enemy) {
		return 0; // signaling: OK
	}

	@Override
	public int enemyDamaged(Unit enemy, Unit attacker, float damage, AIFloat3 dir, WeaponDef weaponDef, boolean paralyzed) {
		return 0; // signaling: OK
	}

	@Override
	public int enemyDestroyed(Unit enemy, Unit attacker) {
        enemies.remove(enemy);
		return 0; // signaling: OK
	}



}
