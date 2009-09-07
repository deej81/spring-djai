/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Resources;

import DJAI.DJAI;
import com.springrts.ai.AIFloat3;
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
            List<AIFloat3> locations = m_Callback.getMap().getResourceMapSpotsPositions(m_Resources[y].m_Resource);
            ai.sendTextMsg("creating map for: "+ (m_Resources[y].m_Resource).getName());
            try{
                m_Resources[y].initializeMap(locations, m_Callback.getMap(), ai);
            }catch(Exception ex){
 
                ai.sendTextMsg("EXCEPTION: "+ ex.getMessage());

            }
            ai.sendTextMsg("map created for: "+ (m_Resources[y].m_Resource).getName());
            
            for(UnitDef def: m_Callback.getUnitDefs()){
                //def.
            }

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
    
    public AIFloat3 getSpotforUnit(UnitDef unitDef, OOAICallback callback, AIFloat3 currPos, DJAI ai){
        ai.sendTextMsg("looking for spot for:" +unitDef.getName());
        //is this a resource extractor
        if(unitDef.getName().equals("armmex")){
            for(int y=0;y<m_Resources.length;y++){
                if(unitDef.getExtractsResource(m_Resources[y].m_Resource)>0){
                    ai.sendTextMsg("looking for resource:" +m_Resources[y].m_Resource.getName());
                    return m_Resources[y].getNearestLocation(currPos, ai, true).ExactLocation;
                }
            }
            
           return null;
            
            
            
        }else{
            
            return callback.getMap().findClosestBuildSite(unitDef, currPos, 1000, 5, 0);
            
        }
        
        
    }


}