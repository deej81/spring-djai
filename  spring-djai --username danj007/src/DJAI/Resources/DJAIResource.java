/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Resources;

import DJAI.DJAI;
import com.springrts.ai.AIFloat3;
import com.springrts.ai.oo.Map;
import com.springrts.ai.oo.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author deej
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
    public AIFloat3 getNearestLocation(AIFloat3 currPos, DJAI ai){
        
        //int x = (int)currPos.x/8;
        //int y = (int)currPos.z/8;
        //ai.sendTextMsg("current position is: "+String.valueOf(x)+" X "+String.valueOf(y));

        //int maxRange = 100;
        //int shifts=1;
        //int doneShifts=0;
        //int fullPass=0;
       // search dir=search.left;
       // Boolean squareUsed = false;
        
        double currBest=-1;
        int currIndex=0;
        int selIndex=0;
        ResourceSquare currentChoice=null;

        for(ResourceSquare square: m_Squares){
            
            if(!square.Occupied){
                double sqDist = calcDistance(currPos, square.ExactLocation);
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
            m_Squares.get(selIndex).Occupied=true;
            return currentChoice.ExactLocation;

        }


        
//        while(m_LocationsMap[x][y]==null||squareUsed){
//            if(shifts>maxRange) break;
//
//            switch(dir){
//                case left:
//                    if(doneShifts==shifts){
//                        dir=search.up;
//                        doneShifts=0;
//                        break;
//                    }
//                    x-=1;
//                    doneShifts++;
//                    if(x<0) x=0;
//                    break;
//                case up:
//                     if(doneShifts==shifts){
//                        dir=search.right;
//                        doneShifts=0;
//                        break;
//                    }
//                    y-=1;
//                    doneShifts++;
//                    if(y<0) y=0;
//                    break;
//                case right:
//
//                     if(doneShifts==0) shifts++;
//                     if(doneShifts==shifts){
//                        dir=search.down;
//                        doneShifts=0;
//                        break;
//                    }
//
//                    x+=1;
//                    doneShifts++;
//                    if(x>=m_LocationsMap.length) x=m_LocationsMap.length-1;
//                    break;
//
//                case down:
//
//                     if(doneShifts==shifts){
//                        dir=search.left;
//                        doneShifts=0;
//                        fullPass++;
//                        shifts++;
//                        break;
//                    }
//
//                    y+=1;
//                    doneShifts++;
//                    if(y>=m_LocationsMap[x].length) y= m_LocationsMap[x].length-1;
//                    break;
//
//            }
//
//             ai.sendTextMsg("searching: "+String.valueOf(x)+" X "+String.valueOf(y));
//
//            if(m_LocationsMap[x][y]==null){
//                squareUsed=false;
//            }else{
//                ai.sendTextMsg("Found At: "+String.valueOf(x)+" X "+String.valueOf(y)+" is occupied: "+String.valueOf(m_LocationsMap[x][y].Occupied));
//                squareUsed=m_LocationsMap[x][y].Occupied;
//            }
//        }
//        ai.sendTextMsg("Returning: "+String.valueOf(x)+" X "+String.valueOf(y));
//        m_LocationsMap[x][y].Occupied = true;
//        return m_LocationsMap[x][y].ExactLocation;
        
    }

    private double calcDistance(AIFloat3 x, AIFloat3 y){

        float xAxis = x.x-y.x;
        float yAxis = x.z-y.z;

        xAxis = (xAxis*xAxis);
        yAxis = (yAxis*yAxis);

        double d = xAxis+yAxis;

        return Math.sqrt(d);

    }
    
}
