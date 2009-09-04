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
import com.springrts.ai.AICommand;
import com.springrts.ai.AICommandWrapper;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.command.BuildUnitAICommand;
import com.springrts.ai.command.SendTextMessageAICommand;
import com.springrts.ai.oo.*;
import java.util.ArrayList;
import java.util.List;

public class DJAI extends com.springrts.ai.oo.AbstractOOAI {

    private OOAICallback m_Callback;
    private Unit commander;
    private int m_iBuildID=0;
    private String[] m_saBuild = {"armsolar","armmex","armmex","armmex","armmex","armmex","armmex","armmex"};
    private ResourceHandler m_ResourceHandler = new ResourceHandler();
    private static final int DEFAULT_ZONE = 0;

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
    doNextBuild(unit);
   return 0;
}

  @Override
	public int unitIdle(Unit unit) {
            try{
                doNextBuild(unit);
            }catch(Exception ex){
                sendTextMsg("Failed to do next build: "+ex);

            }
            return 0; // signaling: OK
	}

  private void doNextBuild(Unit unit){

      List<UnitDef> unitDefs = this.m_Callback.getUnitDefs();
        UnitDef toBuild = null;
        String buildID = m_saBuild[m_iBuildID];
        m_iBuildID++;
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
       AICommand command = new BuildUnitAICommand(this.commander, -1,
           new ArrayList<AICommand.Option>(), 10000, toBuild,
           buildPos, 0);
       int retVal = this.m_Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE,
           -1, command);
  }

  @Override public int update(int frame) {
    

   return 0;
}

    private void loadModInfo() {
        Mod modInfo = m_Callback.getMod();



    }

    @Override public int



}
