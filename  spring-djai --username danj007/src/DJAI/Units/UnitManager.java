/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Units;

import java.util.ArrayList;
import java.util.List;

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

    public void UnitBuildingNotCompleted(DJAIUnitDef unit){
        if(CurrentlyBuilding.contains(unit)) {
            for(int i=0;i<CurrentlyBuilding.size();i++){
                if(CurrentlyBuilding.get(i).SpringDefID==unit.SpringDefID){
                    CurrentlyBuilding.remove(i);
                    break;
                }

            }
        }
    }

    public boolean amIBuilding(DJAIUnitDef def){
        for(DJAIUnitDef unitDef: CurrentlyBuilding){
            if(unitDef.SpringDefID==def.SpringDefID) return true;
        }
        return false;
    }

    public void UnitBuildingCompleted(DJAIUnit unit){

        if(CurrentlyBuilding.contains(unit.DJUnitDef)) {
            for(int i=0;i<CurrentlyBuilding.size();i++){
                if(CurrentlyBuilding.get(i).SpringDefID==unit.DJUnitDef.SpringDefID){
                    CurrentlyBuilding.remove(i);
                    break;
                }

            }
        }

        if(unit.DJUnitDef.IsAttacker) Attackers.add(unit);
        if(unit.DJUnitDef.IsExtractor) Extractors.add(unit);
        if(unit.DJUnitDef.IsFactory) Factories.add(unit);
        if(unit.DJUnitDef.IsCommander) Commanders.add(unit);
        if(unit.DJUnitDef.IsScouter) Scouters.add(unit);
        if(unit.DJUnitDef.IsBuilder) Builders.add(unit);

    }

    public void UnitDestroyed(DJAIUnit unit){
        if(CurrentlyBuilding.contains(unit.DJUnitDef)) {
            for(int i=0;i<CurrentlyBuilding.size();i++){
                if(CurrentlyBuilding.get(i).SpringDefID==unit.DJUnitDef.SpringDefID){
                    CurrentlyBuilding.remove(i);
                    break;
                }

            }
        }
        
        if(unit.DJUnitDef.IsAttacker) {
            if(Attackers.contains(unit))
                Attackers.add(unit);
        }
        if(unit.DJUnitDef.IsExtractor){
            if(Extractors.contains(unit))
                Extractors.add(unit);
        }
        if(unit.DJUnitDef.IsFactory) {
            if(Factories.contains(unit))
                Factories.add(unit);
        }
        if(unit.DJUnitDef.IsCommander) {
            if(Commanders.contains(unit))
                Commanders.add(unit);
        }
        if(unit.DJUnitDef.IsScouter) {
            if(Scouters.contains(unit))
                Scouters.add(unit);
        }
        if(unit.DJUnitDef.IsBuilder) {
            if(Builders.contains(unit))
                Builders.add(unit);
        }
    }



}
