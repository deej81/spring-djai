/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI;

import com.springrts.ai.oo.Mod;
import com.springrts.ai.oo.Unit;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author deej
 */

public class ModuleData {

    public String m_sModName="";
    public UnitData[] Units;


    public Boolean load(Mod mod){

        String modName = mod.getShortName();

        File modData = new File("config\\"+modName+"\\modData.json");
        if(modData.exists()){

            return true;
        }else{
            createNewModDataFile(mod);
            return false;
        }

    }

    private void createNewModDataFile(Mod mod) {
       this.m_sModName = mod.getShortName();
       //this.Units = new UnitData[mod.g]



    }

}
