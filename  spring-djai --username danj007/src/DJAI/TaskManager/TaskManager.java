/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.TaskManager;

import DJAI.DJAI;
import DJAI.Units.DJAIUnit;
import DJAI.Resources.ResourceHandler;
import DJAI.Units.DJAIUnitDef;
import DJAI.Utilities.VectorUtils;
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
        String buildID ="";

        DJAIUnitDef djDef= ai.DefManager.getUnitDefForUnit(list[unit.BuildIndex]);
        ai.sendTextMsg("checking resource reqs for: "+djDef.SpringName);

        if(ai.ResourceHandler.checkResourceRequirements(djDef.ResourceRequirements, ai)
                && (( ai.DJUnitManager.amIBuilding(djDef) && ! djDef.SoloBuild) || ! ai.DJUnitManager.amIBuilding(djDef)) ){
                ai.sendTextMsg("building is ok to go: "+djDef.SpringName);
                unit.CurrentlyBuildingDef = djDef;
                ai.DJUnitManager.UnitBuildingStarted(djDef);
                buildID = list[unit.BuildIndex];
                unit.BuildIndex++;
        }else{
            ai.sendTextMsg("building failed check: "+djDef.SpringName);
            //buildResource(unit, resourceHandler, ai);
            if(unit.DJUnitDef.IsFactory) unit.IsFactoryOnWait=true;
            if(unit.DJUnitDef.IsBuilder) unit.IsBuilderDoingGuard=true;
            if(unit.DJUnitDef.IsBuilder)
                findNearestFactoryAndAssist(unit, ai);
            return 0;
        }

        //boolean found=false;
        //for(int i=unit.BuildIndex;i<list.length;i++){
          //  DJAIUnitDef djDef= ai.DefManager.getUnitDefForUnit(list[i]);
          //  if(ai.ResourceHandler.checkResourceRequirements(djDef.ResourceRequirements, ai)){
           //     ai.sendTextMsg("building: "+djDef.SpringName);
           //     found=true;
           //     unit.BuildIndex=i;
           //     break;
          //  }else{
          //      ai.sendTextMsg("not building: "+djDef.SpringName);
          //  }
        //}
        
        //if(found){
            
         //   unit.BuildIndex++;
       // }else{
         //   unit.IsBuilderDoingGuard=true;
         //   return 0;
       // }
        

        if(unit.BuildIndex==list.length){
            if(unit.DJUnitDef.IsCommander){
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
    
    private boolean findNearestFactoryAndAssist(DJAIUnit unit, DJAI ai){
        DJAIUnit fact=null;
        double distance=-1;
        for(DJAIUnit poss: ai.units){
            if(poss.DJUnitDef.IsFactory){
                double pDist= VectorUtils.CalcDistance(unit.SpringUnit.getPos(), poss.SpringUnit.getPos());
                if(distance==-1||pDist  <distance){
                    fact=poss;
                    distance=pDist;
                }
            }
        }
        
       if(distance==-1) return false;
        try{
           AICommand command = new GuardUnitAICommand(unit.SpringUnit,0,new ArrayList(), 1000, fact.SpringUnit);
           ai.Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE,
           -1, command);
           unit.IsBuilderDoingGuard=true;
           return true;
        }catch(Exception ex){
            ai.sendTextMsg("error creating guard: "+ex.getMessage());
            return false;
        }

    }

    private void buildUnit(DJAIUnit unit, UnitDef toBuild, ResourceHandler resourceHandler, DJAI ai) {
        ai.sendTextMsg("looking for build spot");
        if(unit.DJUnitDef.IsFactory) unit.IsFactoryOnWait=false;
        if(unit.DJUnitDef.IsBuilder) unit.IsBuilderDoingGuard=false;
        DJAIUnitDef toB =null;
        try{

            toB = ai.DefManager.getUnitDefForUnit(toBuild);
        }catch(Exception ex){
            ai.sendTextMsg("error getting def: "+ex.getMessage());
        }
        

        AIFloat3 buildPos = resourceHandler.getSpotforUnit(unit,toB,toBuild, ai.Callback, unit.SpringUnit.getPos(),ai);

       if(buildPos==null) return;
       AICommand command = new BuildUnitAICommand(unit.SpringUnit, -1,
           new ArrayList<AICommand.Option>(), 10000, toBuild,
           buildPos, 0);
       ai.Callback.getEngine().handleCommand(AICommandWrapper.COMMAND_TO_ID_ENGINE,
           -1, command);
    }

    public enum UnitNames{
        armcom,armlab,armck,armvp,armcv,armalab,armack;
    }

    public int allocateTaskToUnit(DJAIUnit unit, ResourceHandler resourceHandler, DJAI ai){
        ai.sendTextMsg("task needed for:" + unit.SpringUnit.getDef().getName());
        if(unit.DJUnitDef.IsAttacker) return 0; //leave to attack handler

        if(unit.DJUnitDef.IsScouter){
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
        }else if(unit.DJUnitDef.IsBuilder){
            ai.sendTextMsg("builder job for:" + unit.SpringUnit.getDef().getName());
            unit.CurrentlyBuildingDef = null;
            if(resourceHandler.resourcesArePlentifull(ai)){
                ai.sendTextMsg("finding guard pos for:" + unit.SpringUnit.getDef().getName());
                if(findNearestFactoryAndAssist(unit,ai)){
                    return 0;
                }
            }
            
            if(resourceHandler.shortOnResource(ai)){
                ai.sendTextMsg("short on resource");
                buildResource(unit, resourceHandler, ai);
            }else{
                return allocateUnitToNextTask(unit, resourceHandler, ai);
            }
        }else if(unit.DJUnitDef.IsFactory){
            ai.sendTextMsg("factory job for:" + unit.SpringUnit.getDef().getName());
            unit.CurrentlyBuildingDef = null;
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

    private void buildResource(DJAIUnit unit, ResourceHandler resourceHandler, DJAI ai){
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
    }

    public String[] getTaskListForBuilder(Unit unit, DJAI ai){

        String name = unit.getDef().getName();
        String[] list = null;

        if(unit.getDef().isBuilder()){

            for(UnitDef def: unit.getDef().getBuildOptions()){
               ai.sendTextMsg("unit: " + unit.getDef().getName()+ " can build: "+ def.getName());

            }
        }

        switch(UnitNames.valueOf(name)){
            case armcom:
                String[] ret = {"armmex","armsolar","armsolar","armmex","armlab","armrad","armmex","armmex","armsolar","armmex","armsolar","armsolar","armvp","armsolar"};
                return ret;
            case armck:
                String[] ret3 = {"armmex","armsolar","armmex","armmex","armsolar","armlab","armrad","armmex","armsolar","armmex","armalab"};
                return ret3;
            case armlab:
                String[] ret2 = {"armck","armpw","armjeth","armpw","armwar","armham","armham","armwar","armck","armpw","armwar","armham","armwar","armwar"};
                return ret2;
            case armvp:
                String[] ret4 =  {"armflash","armflash","armflash","armstump","armcv","armflash","armflash","armflash","armstump"};
                return ret4;
            case armcv:
                String[] ret5 = {"armmex","armsolar","armmex","armmex","armsolar","armrad","armmex","armsolar","armmex","armmex","armsolar","armrad","armmex","armsolar","armmex"};
                return ret5;
            case armalab:
                String[] ret6 = {"armack","armzeus","armfido","armzeus","armzeus","armfido","armzeus","armzeus","armfido"};
                return ret6;
            case armack:
                String[] ret7 = {"armarad","armfus","armmmkr","armmmkr"};
                return ret7;
            default:
                ai.sendTextMsg("no list found for: "+name);
                break;
        }

        return list;


    }


}
