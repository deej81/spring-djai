/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Resources;

import DJAI.DJAI;
import DJAI.Units.DJAIUnit;
import DJAI.Units.DJAIUnitDef;
import com.springrts.ai.AICommand;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.command.AddPointDrawAICommand;
import com.springrts.ai.command.CreateLineFigureDrawerAICommand;
import com.springrts.ai.command.CreateSplineFigureDrawerAICommand;
import com.springrts.ai.oo.OOAICallback;
import com.springrts.ai.oo.Resource;
import com.springrts.ai.oo.UnitDef;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author deej
 */
public class ResourceHandler {

    private DJAIResource[] m_Resources;

    public Resource getMostNeededResource(DJAI ai) {
        Resource mostNeeded=null;
        double currMaxPercent=0;
        List<Resource> resources = ai.Callback.getResources();
			for (Resource resource : resources) {
                float currentUsage = ai.Callback.getEconomy().getUsage(resource)-ai.Callback.getEconomy().getIncome(resource);

                if(currentUsage>0){
                    double percentUse = currentUsage/ai.Callback.getEconomy().getCurrent(resource);
                     ai.sendTextMsg("resource use for "+resource.getName()+ " is "+String.valueOf(percentUse));
                    if(percentUse>currMaxPercent){
                        currMaxPercent = percentUse;
                        mostNeeded = resource;
                    }
                }

			}
            ai.sendTextMsg("most needed resource: "+mostNeeded.getName());
            return mostNeeded;
    }

    public Boolean initializeResources(OOAICallback m_Callback, DJAI ai){

        m_Resources = new DJAIResource[m_Callback.getResources().size()];
        ai.sendTextMsg("created resource array");
        int i=0;
        for(Resource r : m_Callback.getResources()){

            m_Resources[i] = new DJAIResource();
            m_Resources[i].m_Resource = r;
            ai.sendTextMsg("created resource for: "+ r.getName());
            i++;
        }

        ai.sendTextMsg("populated resource array");
        
        for(int y=0;y<m_Resources.length;y++){
             ai.sendTextMsg("creating locations list for: "+ (m_Resources[y].m_Resource).getName());
            //List<AIFloat3> locations = m_Callback.getMap().getResourceMapSpotsPositions(m_Resources[y].m_Resource);
             try{
                 List<ResourceSquare> locations = ResourceFinder.SearchResourceSpots(m_Resources[y].m_Resource, ai);
                ai.sendTextMsg("creating map for: "+ (m_Resources[y].m_Resource).getName());
                m_Resources[y].m_Squares = locations;
                //m_Resources[y].initializeMap(locations, m_Callback.getMap(), ai);
            }catch(Exception ex){
 
                ai.sendTextMsg("EXCEPTION: "+ ex.getMessage());

            }
            ai.sendTextMsg("map created for: "+ (m_Resources[y].m_Resource).getName());
            
            //for(UnitDef def: m_Callback.getUnitDefs()){
                //def.
           // }

        }

        return true;

    }

    public void freeUpMexSpot(AIFloat3 location, DJAI ai){
        for(int y=0;y<m_Resources.length;y++){
                if(m_Resources[y].m_Resource.getName().equals("Metal")){
                    m_Resources[y].freeUpMexSpot(location, ai);
                    break;
                }
            }
    }

    public List<ResourceSquare> MexSpots(){
         for(int y=0;y<m_Resources.length;y++){
                if(m_Resources[y].m_Resource.getName().equals("Metal")){
                    return m_Resources[y].m_Squares;
                }
            }
         return new ArrayList();

    }
    
    public AIFloat3 getSpotforUnit(DJAIUnit builder, DJAIUnitDef unitDef,UnitDef springDef, OOAICallback callback, AIFloat3 currPos, DJAI ai){
        ai.sendTextMsg("looking for spot for:" +springDef.getName());
        //is this a resource extractor
        if(unitDef.IsExtractor){
            for(int y=0;y<m_Resources.length;y++){
                if(springDef.getExtractsResource(m_Resources[y].m_Resource)>0){
                    ai.sendTextMsg("looking for resource:" +m_Resources[y].m_Resource.getName());

                    if(ai.DoMapPings){
                        AIFloat3 pos = m_Resources[y].getNearestLocation(currPos, ai, true).ExactLocation;
                        AICommand command = new AddPointDrawAICommand(pos, "MEX LOCATION");
                        int success = ai.handleEngineCommand(command);
                        if(success!=0) ai.sendTextMsg("failed to set marker");
                        return pos;
                    }
                    return m_Resources[y].getNearestLocation(currPos, ai, true).ExactLocation;

                    //return m_Resources[y].getNearestLocation(currPos, ai, true).ExactLocation;
                }
            }
            
           return null;
            
            
            
        }else{
            
//            if(!builder.DJUnitDef.IsCommander&& builder.SpringUnit.getDef().getTechLevel()<springDef.getTechLevel()){
//                return callback.getMap().findClosestBuildSite(springDef, ai.basePos, 3000, 30, 0);
//            }else{
                return callback.getMap().findClosestBuildSite(springDef, currPos, 1000, 10, 0);
//            }

            
        }
        
        
    }

    public boolean checkResourceRequirements(List<ResourceRequirement> requirements,DJAI ai){
        for(ResourceRequirement requirement: requirements){
            if(!checkResourceRequirement(requirement,ai)) return false;
        }

        return true;
    }

    public boolean shortOnResource(DJAI ai) {
        List<Resource> resources = ai.Callback.getResources();
			for (Resource resource : resources) {
                float currentUsage = ai.Callback.getEconomy().getUsage(resource)-ai.Callback.getEconomy().getIncome(resource);
                
                if(currentUsage>0){
                    double percentUse = currentUsage/ai.Callback.getEconomy().getCurrent(resource);
                    if(percentUse>0.1) {
                         ai.sendTextMsg("Resources Needed");
                        return true;
                    }
                }
                
			}
            return false;
    }

    public boolean resourcesArePlentifull(DJAI ai){
        List<Resource> resources = ai.Callback.getResources();
        for (Resource resource : resources) {
                //float currentUsage = ai.Callback.getEconomy().getUsage(resource)-ai.Callback.getEconomy().getIncome(resource);
                float storage = ai.Callback.getEconomy().getStorage(resource);
                float current = ai.Callback.getEconomy().getCurrent(resource);

                double percOfStor = current/storage;

                if(percOfStor<0.85){
                    return false;
                }

        }
        return true;

    }

    public boolean canWeGuard(DJAI ai){

        List<Resource> resources = ai.Callback.getResources();
        for (Resource resource : resources) {
                //float currentUsage = ai.Callback.getEconomy().getUsage(resource)-ai.Callback.getEconomy().getIncome(resource);
                float storage = ai.Callback.getEconomy().getStorage(resource);
                float current = ai.Callback.getEconomy().getCurrent(resource);

                double percOfStor = current/storage;

                if(percOfStor<0.35){
                    return false;
                }

        }
        return true;

    }

    public boolean checkResourceRequirement(ResourceRequirement requirement, DJAI ai) {
        List<Resource> resources = ai.Callback.getResources();
        for (Resource resource : resources) {

            if(resource.getName().equals(requirement.RequiredResource)){
                float income = ai.Callback.getEconomy().getIncome(resource);
                float surplus = income -ai.Callback.getEconomy().getUsage(resource);
                float current = ai.Callback.getEconomy().getCurrent(resource);

                if(income<requirement.RequiredIncome) return false;
                ai.sendTextMsg("Income OK");
                if(requirement.MaxIncome!=-1&&income>requirement.MaxIncome) return false;
                ai.sendTextMsg("Max Income OK");
                if(current<requirement.RequiredTotal){
                    //check we actually have the possibility of having enough!
                    if(ai.Callback.getEconomy().getStorage(resource)>requirement.RequiredTotal)
                        return false;
                }
                ai.sendTextMsg("Total OK");
                if(requirement.MaxTotal!=-1&&income>requirement.MaxTotal) return false;
                ai.sendTextMsg("Max Total OK");
                //if(surplus<requirement.RequiredSurplus) return false;
                //ai.sendTextMsg("Surplus OK");
            }

        }

        return true;
    }


}
