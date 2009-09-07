/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Resources;

import DJAI.DJAI;
import DJAI.Utilities.VectorUtils;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.oo.Map;
import com.springrts.ai.oo.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author deej
 * added to google code 4th september
 */
public class DJAIResource {
    public Vector m_vUnitDefs = new Vector();
    public Resource m_Resource;
    public ResourceSquare[][] m_LocationsMap;
    public List<ResourceSquare> m_Squares = new ArrayList<ResourceSquare>();

    
    public void initializeMap(List<AIFloat3> locations, Map map, DJAI ai){
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();

        //ai.sendTextMsg("map dimensions: "+String.valueOf(mapWidth)+" X "+String.valueOf(mapHeight));
        
        //int djMapWidth = mapWidth;
        //int djMapHeight = mapHeight;

        //ai.sendTextMsg("DJ map dimensions: "+String.valueOf(djMapWidth)+" X "+String.valueOf(djMapHeight));

        //m_LocationsMap = new ResourceSquare[djMapWidth][djMapHeight];
        
        for(AIFloat3 pos: locations){
            //ai.sendTextMsg("resource at: "+String.valueOf((int)pos.x)+" X "+String.valueOf((int)pos.z));
            //ai.sendTextMsg("allocating resource to: "+String.valueOf((int)pos.x/8)+" X "+String.valueOf((int)pos.z/8));


            //m_LocationsMap[(int)pos.x/8][(int)pos.z/8] = new ResourceSquare();
            //m_LocationsMap[(int)pos.x/8][(int)pos.z/8].ExactLocation = pos;

            ResourceSquare sq = new ResourceSquare();
            sq.ExactLocation = pos;
            m_Squares.add(sq);
            //m_LocationsMap[(int)pos.x/8][(int)pos.z/8] = new ResourceSquare();
            //m_LocationsMap[(int)pos.x/8][(int)pos.z/8].ExactLocation = pos;
            
        }
        
    }
    private enum search{
            left,up,right,down
           
    }

    public void freeUpMexSpot(AIFloat3 location, DJAI ai){
        getNearestLocation(location, ai, false);
    }


    public ResourceSquare getNearestLocation(AIFloat3 currPos, DJAI ai, Boolean occupy){
        
       
        
        double currBest=-1;
        int currIndex=0;
        int selIndex=0;
        ResourceSquare currentChoice=null;

        for(ResourceSquare square: m_Squares){
            
            if(!square.Occupied){
                double sqDist = VectorUtils.CalcDistance(currPos, square.ExactLocation);
                if(sqDist<currBest||currBest==-1){
                    currBest = sqDist;
                    currentChoice=square;
                    selIndex=currIndex;
                }
            }

            currIndex++;

        }

        if(currentChoice==null){
            return null;
        }else{
            m_Squares.get(selIndex).Occupied=occupy;
            return currentChoice;

        }

        
    }

    
}
