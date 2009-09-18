/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Units;

import DJAI.DJAI;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.oo.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author deej
 */
public class UnitManager {

    public List<DJAIUnit> Attackers = new ArrayList();
    public List<DJAIUnit> Buildings = new ArrayList();
    public List<DJAIUnit> Builders = new ArrayList();
    public List<DJAIUnit> Extractors = new ArrayList();
    public List<DJAIUnit> Factories = new ArrayList();
    public List<DJAIUnit> Commanders = new ArrayList();
    public List<DJAIUnit> Scouters = new ArrayList();

    public List<DJAIUnitDef> CurrentlyBuilding = new ArrayList();


    public void UnitBuildingStarted(DJAIUnitDef unit){
        CurrentlyBuilding.add(unit);
    }

    public void UnitBuildingNotCompleted(DJAIUnitDef unit, DJAI ai){
        if(CurrentlyBuilding.contains(unit)) {
            for(int i=0;i<CurrentlyBuilding.size();i++){
                if(CurrentlyBuilding.get(i).SpringDefID==unit.SpringDefID){
                    CurrentlyBuilding.remove(i);
                    ai.sendTextMsg("removed incomplete build");
                    break;
                }

            }
        }
    }

    public boolean amIBuilding(DJAIUnitDef def,DJAI ai){
        if(ai.Debug){
            boolean building = false;
            for(DJAIUnitDef unitDef: CurrentlyBuilding){
                ai.sendTextMsg("Currently building: " + unitDef.SpringName);
                if(unitDef.SpringDefID==def.SpringDefID){
                    ai.sendTextMsg("Currently building: returned true");
                    building= true;
                }
            }
            return building;
        }else{
            for(DJAIUnitDef unitDef: CurrentlyBuilding){
                if(unitDef.SpringDefID==def.SpringDefID) return true;
            }
            return false;
        }
    }

    public void UnitBuildingCompleted(DJAIUnit unit, DJAI ai){

        ai.sendTextMsg("Unit Completed");
        
        for(int i=0;i<CurrentlyBuilding.size();i++){
            ai.sendTextMsg("Checking current builds");
            if(CurrentlyBuilding.get(i).SpringDefID==unit.DJUnitDef.SpringDefID){
                ai.sendTextMsg("Found Build - removing");
                CurrentlyBuilding.remove(i);
            }

        }
        
        if(unit.DJUnitDef.IsAttacker) Attackers.add(unit);
        if(unit.DJUnitDef.IsExtractor) Extractors.add(unit);
        if(unit.DJUnitDef.IsFactory) Factories.add(unit);
        if(unit.DJUnitDef.IsCommander) Commanders.add(unit);
        if(unit.DJUnitDef.IsScouter) Scouters.add(unit);
        if(unit.DJUnitDef.IsBuilder) Builders.add(unit);

    }

    public int CurrentUnitCount(DJAIUnitDef def){
        int count=0;
        for(DJAIUnitDef d:CurrentlyBuilding){
            if(d==def)count++;
        }

        if(def.IsAttacker) count+=getCountFromCollection(Attackers, def);
        if(def.IsExtractor) count+=getCountFromCollection(Extractors, def);
        if(def.IsFactory) count+=getCountFromCollection(Factories, def);
        if(def.IsCommander) count+=getCountFromCollection(Commanders, def);
        if(def.IsScouter) count+=getCountFromCollection(Scouters, def);
        if(def.IsBuilder) count+=getCountFromCollection(Builders, def);

        return count;
    }

    private int getCountFromCollection(List<DJAIUnit> collection, DJAIUnitDef def){
        int count=0;
        for(DJAIUnit unit : collection){
            if(unit.DJUnitDef.SpringDefID==def.SpringDefID) count++;
        }

        return count;

    }
    
    private int getUnitRefFromCollection(Unit unit, List<DJAIUnit> collection, boolean remove,DJAI ai){

        int index=0;
        for(DJAIUnit djU: collection){

            if(djU.SpringUnit.getUnitId()==unit.getUnitId()){
                if(remove){
                    if(djU.CurrentlyBuildingDef!=null){
                        UnitBuildingNotCompleted(djU.CurrentlyBuildingDef, ai);
                    }
                    collection.remove(djU);
                    ai.sendTextMsg("UnitManager->UnitDestroyed: getUnitRefFromCollection : unit deleted");
                    return -1;
                }
                
                return index;
            }
            index++;
        }
        
        ai.sendTextMsg("UnitManager->UnitDestroyed: getUnitRefFromCollection : UNIT NOT FOUND");

        return -1;
    }

    public AIFloat3 RandomExtractorPos(){
        Random rand = new Random();
        int i = rand.nextInt(Extractors.size()-1);
        return Extractors.get(i).SpringUnit.getPos();
    }
    
    public void UnitDestroyed(Unit unit, DJAI ai){
        
        ai.sendTextMsg("UnitManager->UnitDestroyed");
        
        DJAIUnitDef def = ai.DefManager.getUnitDefForUnit(unit.getDef());

        if(def==null) return;

        if(unit.isBeingBuilt()){
            ai.sendTextMsg("UnitManager->UnitDestroyed: unit is being built");
            for(int i=0;i<CurrentlyBuilding.size();i++){
                    if(CurrentlyBuilding.get(i).SpringDefID==def.SpringDefID){
                    CurrentlyBuilding.remove(i);
                    ai.sendTextMsg("UnitManager->UnitDestroyed: found and removed unfinished unit");
                    return;
                    }

            }
        }

        if(def.IsAttacker) getUnitRefFromCollection(unit, Attackers, true, ai);
        if(def.IsExtractor) getUnitRefFromCollection(unit, Extractors, true, ai);
        if(def.IsFactory) getUnitRefFromCollection(unit, Factories, true, ai);
        if(def.IsCommander) getUnitRefFromCollection(unit, Commanders, true, ai);
        if(def.IsScouter) getUnitRefFromCollection(unit, Scouters, true, ai);
        if(def.IsBuilder) getUnitRefFromCollection(unit, Builders, true, ai);
        
    }

    public void AllocateUnitToBuild(DJAIUnit unit, DJAIUnitDef toBuild,DJAI ai){
        int index=-1;
        if(unit.DJUnitDef.IsCommander){
            index = getUnitRefFromCollection(unit.SpringUnit, Commanders, false, ai);
            if(index!=-1){
                Commanders.get(index).CurrentlyBuildingDef=toBuild;
            }
        }else if(unit.DJUnitDef.IsBuilder){
            index = getUnitRefFromCollection(unit.SpringUnit, Builders, false, ai);
            if(index!=-1){
                Builders.get(index).CurrentlyBuildingDef=toBuild;
            }
        }
    }

    public void UnitDestroyed(DJAIUnit unit){

        if(unit.SpringUnit.isBeingBuilt()){
            if(CurrentlyBuilding.contains(unit.DJUnitDef)) {
            for(int i=0;i<CurrentlyBuilding.size();i++){
                    if(CurrentlyBuilding.get(i).SpringDefID==unit.DJUnitDef.SpringDefID){
                    CurrentlyBuilding.remove(i);
                    return;
                    }

                }
            }

        }
        
        if(unit.DJUnitDef.IsAttacker) {
            if(Attackers.contains(unit))
                Attackers.remove(unit);
        }
        if(unit.DJUnitDef.IsExtractor){
            if(Extractors.contains(unit))
                Extractors.remove(unit);
        }
        if(unit.DJUnitDef.IsFactory) {
            if(Factories.contains(unit))
                Factories.remove(unit);
        }
        if(unit.DJUnitDef.IsCommander) {
            if(Commanders.contains(unit))
                Commanders.remove(unit);
        }
        if(unit.DJUnitDef.IsScouter) {
            if(Scouters.contains(unit))
                Scouters.remove(unit);
        }
        if(unit.DJUnitDef.IsBuilder) {
            if(Builders.contains(unit))
                Builders.remove(unit);
        }
    }



}
