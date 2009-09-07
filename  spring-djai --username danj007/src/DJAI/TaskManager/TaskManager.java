/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.TaskManager;

import DJAI.DJAI;
import DJAI.DJAIUnit;
import DJAI.Resources.ResourceHandler;
import com.springrts.ai.AICommand;
import com.springrts.ai.AICommandWrapper;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.command.BuildUnitAICommand;
import com.springrts.ai.command.GuardUnitAICommand;
import com.springrts.ai.command.MoveUnitAICommand;
import com.springrts.ai.command.WaitUnitAICommand;
import com.springrts.ai.oo.Resource;
import com.springrts.ai.oo.Unit;
import com.springrts.ai.oo.UnitDef;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author deej
 */
public class TaskManager {

    private Random m_Rand = new Random();

    private int allocateUnitToNextTask(DJAIUnit unit, ResourceHandler resourceHandler, DJAI ai) {
        ai.sendTextMsg("finding unit to build");
        List<UnitDef> unitDefs = ai.Callback.getUnitDefs();
        UnitDef toBuild = null;
        String[] list = getTaskListForBuilder(unit.SpringUnit, ai);
        String buildID = list[unit.BuildIndex];

        unit.BuildIndex++;
        if(unit.BuildIndex==list.length){
            if(unit.IsCommander){
                AICommand command = new GuardUnitAICommand(unit.SpringUnit,0,new ArrayList(), 1000, ai.FirstFactoryUnit );
                try{
                    return ai.Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
                }catch(Exception ex){
                    ai.sendTextMsg("command failed: "+ex.getMessage());

                }
                return 0;

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

        buildUnit(unit, toBuild, resourceHandler ,ai);
        return 0;
    }

    private void buildUnit(DJAIUnit unit, UnitDef toBuild, ResourceHandler resourceHandler, DJAI ai) {
        ai.sendTextMsg("looking for build spot");
        AIFloat3 buildPos = resourceHandler.getSpotforUnit(toBuild, ai.Callback, unit.SpringUnit.getPos(),ai);

       if(buildPos==null) return;
       AICommand command = new BuildUnitAICommand(unit.SpringUnit, -1,
           new ArrayList<AICommand.Option>(), 10000, toBuild,
           buildPos, 0);
       ai.Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE,
           -1, command);
    }

    public enum UnitNames{
        armcom,armlab,armck;
    }

    public int allocateTaskToUnit(DJAIUnit unit, ResourceHandler resourceHandler, DJAI ai){
        ai.sendTextMsg("task needed for:" + unit.SpringUnit.getDef().getName());
        if(unit.IsAttacker) return 0; //leave to attack handler

        if(unit.IsScouter){
            ai.sendTextMsg("scout job for:" + unit.SpringUnit.getDef().getName());
            try{
               int rand = m_Rand.nextInt(resourceHandler.MexSpots().size());
               AIFloat3 pos = resourceHandler.MexSpots().get(rand).ExactLocation;

               AICommand command = new MoveUnitAICommand(unit.SpringUnit, -1, new ArrayList(), 1000, pos);
                ai.Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
                return 0;
           }catch(Exception ex){
                ai.sendTextMsg("scout command failed: "+ex.getMessage());

           }
        }else if(unit.IsBuilder){
            ai.sendTextMsg("builder job for:" + unit.SpringUnit.getDef().getName());
            if(resourceHandler.shortOnResource(ai)){
                Resource mostNeeded = resourceHandler.getMostNeededResource(ai);
                UnitDef bestUnit = null;
                for(UnitDef def:unit.SpringUnit.getDef().getBuildOptions()){
                    ai.sendTextMsg("checking: "+def.getName());
                    if(def.getExtractsResource(mostNeeded)>0){
                        ai.sendTextMsg(def.getName()+" is extractor");
                        if( bestUnit==null|| def.getExtractsResource(mostNeeded)>bestUnit.getExtractsResource(mostNeeded) ){
                            bestUnit=def;
                             ai.sendTextMsg("current best unit for resource prob: "+def.getName());
                        }
                    }else if(def.getUpkeep(mostNeeded)<0){
                        ai.sendTextMsg(def.getName()+" is producer");
                        if( bestUnit==null|| def.getUpkeep(mostNeeded)<bestUnit.getUpkeep(mostNeeded) ){
                            ai.sendTextMsg("current best unit for resource prob: "+def.getName());
                            bestUnit=def;
                        }
                    }
                }
                if(bestUnit!=null){
                    ai.sendTextMsg("best unit for resource prob is: "+bestUnit.getName());
                    buildUnit(unit, bestUnit, resourceHandler ,ai);
                }else{
                    ai.sendTextMsg("current best unit is null");
                }
            }else{
                return allocateUnitToNextTask(unit, resourceHandler, ai);
            }
        }else if(unit.IsFactory){
            ai.sendTextMsg("factory job for:" + unit.SpringUnit.getDef().getName());
            if(resourceHandler.shortOnResource(ai)){
                //make factory wait. Update frame in DJAI will wake it
                ai.sendTextMsg("factory waiting...");
                unit.IsFactoryOnWait=true;
                //AICommand command = new WaitUnitAICommand(unit.SpringUnit, -1, new ArrayList(), 1000);
                //try{
                //    ai.Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE, -1, command);
               // }catch(Exception ex){
                //    ai.sendTextMsg("factory wait command failed: "+ex.getMessage());
               // }
            }else{
                return allocateUnitToNextTask(unit, resourceHandler, ai);
            }
        }else{
            ai.sendTextMsg("not task found!!" + unit.SpringUnit.getDef().getName());
        }

        return 0;

    }


    public String[] getTaskListForBuilder(Unit unit, DJAI ai){

        String name = unit.getDef().getName();
        String[] list = null;

        if(unit.getDef().isBuilder()){
            for(UnitDef def: unit.getDef().getBuildOptions()){
               //ai.sendTextMsg("unit: " + unit.getDef().getName()+ " can build: "+ def.getName());
            }
        }

        switch(UnitNames.valueOf(name)){
            case armcom: case armck:
                String[] ret = {"armmex","armsolar","armmex","armmex","armsolar","armlab","armrad","armmex","armsolar","armmex","armmex","armsolar","armrad","armmex","armsolar","armmex"};
                return ret;
            case armlab:
                String[] ret2 = {"armck","armpw","armjeth","armpw","armwar","armpw","armpw","armwar","armpw","armpw","armpw"};
                return ret2;
            default:
                ai.sendTextMsg("no list found for: "+name);
                break;
        }

        return list;


    }


}
