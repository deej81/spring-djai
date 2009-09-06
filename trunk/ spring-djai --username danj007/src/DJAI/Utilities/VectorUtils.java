/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DJAI.Utilities;

import com.springrts.ai.AIFloat3;

/**
 *
 * @author deej
 */
public class VectorUtils {
    public static double CalcDistance(AIFloat3 x, AIFloat3 y){

        float xAxis = x.x-y.x;
        float yAxis = x.z-y.z;

        xAxis = (xAxis*xAxis);
        yAxis = (yAxis*yAxis);

        double d = xAxis+yAxis;

        return Math.sqrt(d);

    }
}
