/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Units;

import DJAI.Resources.ResourceRequirement;
import com.springrts.ai.oo.Resource;
import com.springrts.ai.oo.UnitDef;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author deej
 */
public class DJAIUnitDef {

    public Boolean IsScouter=false;
    public Boolean IsBuilder=false;
    public Boolean IsFactory=false;
    public Boolean IsCommander=false;
    public Boolean IsAttacker=false;
    public Boolean IsExtractor=false;

    //max build 0=dont build, -1=infinate
    public int MaxBuildNumber=-1;
    public boolean SoloBuild=false;
    public List<ResourceRequirement> ResourceRequirements = new ArrayList();

    public UnitDef SpringDef;

    //possible unitdef id to ease loading?

    public String[] BuildList;

    public void createFromSpringDef(UnitDef springDef, List<Resource> gameResources){

        IsCommander = springDef.isCommander();
        IsFactory = springDef.getSpeed()==0&&springDef.getBuildOptions().size()>0;
        IsBuilder = springDef.isBuilder()&&!IsFactory;
        IsAttacker = springDef.isAbleToFight()&&!IsCommander&&!IsFactory&&!IsBuilder;

        for(Resource resource:gameResources){
            if(springDef.getExtractsResource(resource)>0) {
                IsExtractor=true;
                break;
            }
        }



    }

}

