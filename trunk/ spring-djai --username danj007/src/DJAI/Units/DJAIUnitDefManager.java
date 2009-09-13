/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Units;

import DJAI.DJAI;
import DJAI.Units.DJAIUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.springrts.ai.oo.UnitDef;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;


/**
 *
 * @author deej
 */
public class DJAIUnitDefManager {

    private List<DJAIUnitDef> m_htUnitDefs= new ArrayList<DJAIUnitDef>();

    public DJAIUnitDefManager(DJAI ai) {
        //load defs from file or create a new file for this mod

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        

        String path = ai.Callback.getDataDirs().getConfigDir();
        String modName = ai.Callback.getMod().getShortName();

        String config = path+"\\configs\\"+modName+"_UnitData.cfg";

        File file = new File(config);
        if(file.exists()){
            loadConfig(file,ai);
        }else{
            for(UnitDef def:ai.Callback.getUnitDefs()){
                DJAIUnitDef djDef = new DJAIUnitDef();
                djDef.createFromSpringDef(def, ai.Callback.getResources());
                //m_htUnitDefs.put(def.getName(), djDef);
                m_htUnitDefs.add(djDef);
            }

            try{
                file.createNewFile();
            }catch(IOException iox){
                ai.sendTextMsg("failed to create config file: "+iox.getMessage());
            }
            String json = gson.toJson(m_htUnitDefs);
            ai.sendTextMsg("json created");
            // Create output stream.
           try{
               FileWriter fos = new FileWriter(file);

               BufferedWriter writer = new BufferedWriter(fos);
               writer.write(json);
            
                 writer.close();
                 fos.flush();
                 fos.close();
            }catch(FileNotFoundException flnx){
                ai.sendTextMsg("failed to create config file: "+flnx.getMessage());

            }catch(IOException iox){
              ai.sendTextMsg("failed to create config file: "+iox.getMessage());
            }
            
           
        }
    }

    public DJAIUnitDef getUnitDefForUnit(String name) {
        for(DJAIUnitDef djDef:m_htUnitDefs){
            if(djDef.SpringName.equals(name)){
                return djDef;
            }
        }
        return null;
    }

    public DJAIUnitDef getUnitDefForUnit(UnitDef def){

        for(DJAIUnitDef djDef:m_htUnitDefs){
            if(djDef.SpringDefID==def.getUnitDefId()){
                return djDef;
            }
        }
        return null;
        //return (DJAIUnitDef)m_htUnitDefs.get(def.getName());
    }

    private void loadConfig(File file, DJAI ai) {
        ai.sendTextMsg("loading from file");
        Gson gson = new Gson();

        StringBuilder jsonBuilder = new StringBuilder();
        BufferedReader reader=null;
        try{
            FileReader fr = new FileReader(file);

            reader = new BufferedReader(fr);

            String line = null; //not declared within while loop
        /*
        * readLine is a bit quirky :
        * it returns the content of a line MINUS the newline.
        * it returns null only for the END of the stream.
        * it returns an empty String if two newlines appear in a row.
        */
            while (( line = reader.readLine()) != null){
                 jsonBuilder.append(line);
                jsonBuilder.append(System.getProperty("line.separator"));
            }
            reader.close();
        }catch(FileNotFoundException flnx){
            ai.sendTextMsg("failed to create config file: "+flnx.getMessage());

        }catch(IOException iox){
              ai.sendTextMsg("failed to create config file: "+iox.getMessage());
        }

        try{
               Type collectionType = new TypeToken<Collection<DJAIUnitDef>>(){}.getType();
               m_htUnitDefs=gson.fromJson(jsonBuilder.toString(), collectionType);
            }catch(Exception ex){
               ai.sendTextMsg("Failed to parse JSON: "+ex.getStackTrace());
            }


    }
    public DJAIUnitDef getDefForUnit(DJAIUnit unit){

        return new DJAIUnitDef();

    }

}
