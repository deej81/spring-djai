/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author deej
 */

package DJAI;

import com.springrts.ai.oo.*;

public class DJAIFactory extends com.springrts.ai.oo.OOAIFactory{

    public OOAI createAI(int teamId, OOAICallback callback){

        DJAI aiInstance = new DJAI();
        return aiInstance;
    }

}
