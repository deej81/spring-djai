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

/**
 *
 * @author deej
 */
public class ResourceFinder {

   public static int MaxSpots = 5000;
   public static int MinMetalForSpot = 30;

   public static List<ResourceSquare> SearchResourceSpots(Resource resource, DJAI ai)
   {	
       
      ai.sendTextMsg( "SearchMetalSpots() >>>");
 
      boolean isMetalMap = false;
 
      ArrayList<ResourceSquare> metalspotsal = new ArrayList<ResourceSquare>();
 
      Map map = ai.Callback.getMap();
      int mapheight = map.getHeight() / 2; //metal map has 1/2 resolution of normal map
      int mapwidth = map.getWidth() / 2;
      double mapmaxmetal = map.getMaxResource(resource);
      int totalcells = mapheight * mapwidth;
 
      ai.sendTextMsg( "mapwidth: " + mapwidth + " mapheight " + mapheight + " maxmetal:" + mapmaxmetal );
 
      List<Byte> metalmap = map.getResourceMapRaw(resource); // original metal map
      int[][] metalremaining = new int[ mapwidth][ mapheight ];  // actual metal available at that point. we remove metal from this as we add spots to MetalSpots
      int[][] SpotAvailableMetal = new int [ mapwidth][ mapheight ]; // amount of metal an extractor on this spot could make
      int[][] NormalizedSpotAvailableMetal = new int [ mapwidth][ mapheight ]; // SpotAvailableMetal, normalized to 0-255 range
 
      int totalmetal = 0;
      ArrayIndexer arrayindexer = new ArrayIndexer( mapwidth, mapheight );
      //Load up the metal Values in each pixel
      ai.sendTextMsg( "width: " + mapwidth + " height: " + mapheight );
      for (int y = 0; y < mapheight; y++)
      {
         //String logline = "";
         for( int x = 0; x < mapwidth; x++ )
         {
            metalremaining[ x][ y ] = (int)metalmap.get( arrayindexer.GetIndex( x, y ) );
            totalmetal += metalremaining[ x][ y ];		// Count the total metal so you can work out an average of the whole map
            //logline += metalremaining[ x, y ].toString() + " ";
             // logline += metalremaining[ x, y ] + " ";
         }
          //ai.sendTextMsg( logline );
      }
      ai.sendTextMsg ("*******************************************");
 
      double averagemetal = ((double)totalmetal) / ((double)totalcells);  //do the average
      // int maxmetal = 0;
 
      int ExtractorRadius = (int)( map.getExtractorRadius(resource)/ 16.0 );
            
      //int ExtractorRadius = 2;
      int DoubleExtractorRadius = ExtractorRadius * 2;
      int SquareExtractorRadius = ExtractorRadius * ExtractorRadius; //used to speed up loops so no recalculation needed
      int FourSquareExtractorRadius = 4 * SquareExtractorRadius; // same as above 
      double CellsInRadius = Math.PI * ExtractorRadius * ExtractorRadius;
 
      int maxmetalspotamount = 0;
      ai.sendTextMsg( "Calculating available metal for each spot..." );
      AvailableMetalResult availableMetalResult = CalculateAvailableMetalForEachSpot( metalremaining, ExtractorRadius, ai );
      SpotAvailableMetal = availableMetalResult.availableMetal;
      maxmetalspotamount = availableMetalResult.maxMetalAmount;
 
      ai.sendTextMsg( "Normalizing..." );
      // normalize the metal so any map will have values 0-255, no matter how much metal it has
      //int[][] NormalizedMetalRemaining = new int[ mapwidth][ mapheight ];
      for (int y = 0; y < mapheight; y++)
      {
         for (int x = 0; x < mapwidth; x++)
         {
            NormalizedSpotAvailableMetal[ x][ y ] = ( SpotAvailableMetal[ x][ y ] * 255 ) / maxmetalspotamount;
         }
      }
 
      ai.sendTextMsg( "maxmetalspotamount: " + maxmetalspotamount );
 
      boolean Stopme = false;
      int SpotsFound = 0;
      //ai.sendTextMsg( BuildTable.GetInstance().GetBiggestMexUnit().toString() );
      // UnitDef biggestmex = BuildTable.GetInstance().GetBiggestMexUnit();
      // ai.sendTextMsg( "biggestmex is " + biggestmex.name + " " + biggestmex.humanName );
      for (int spotindex = 0; spotindex < MaxSpots && !Stopme; spotindex++)
      {	                
         			ai.sendTextMsg( "spotindex: " + spotindex );
         int bestspotx = 0, bestspoty = 0;
         int actualmetalatbestspot = 0; // use to try to put extractors over spot itself
         //finds the best spot on the map and gets its coords
         int BestNormalizedAvailableSpotAmount = 0;
         for (int y = 0; y < mapheight; y++)
         {
            for (int x = 0; x < mapwidth; x++)
            {
               if( //NormalizedSpotAvailableMetal[ x ][ y ] > BestNormalizedAvailableSpotAmount ||
                     ( metalremaining[ x ][ y ] > actualmetalatbestspot ) )
               {
                  BestNormalizedAvailableSpotAmount = NormalizedSpotAvailableMetal[ x ][ y ];
                  bestspotx = x;
                  bestspoty = y;
                  actualmetalatbestspot = metalremaining[ x ][ y ];
               }
            }
         }		
         			ai.sendTextMsg( "actualmetalatbestspot: " + actualmetalatbestspot );
         if( actualmetalatbestspot < MinMetalForSpot )
         {
            Stopme = true; // if the spots get too crappy it will stop running the loops to speed it all up
            				ai.sendTextMsg( "Remaining spots too small; stopping search" );
         }
 
         if( !Stopme )
         {
            AIFloat3 pos = new AIFloat3();
            pos.x = bestspotx * 2 * 8;
            pos.z = bestspoty * 2 * 8;
            pos.y = map.getElevationAt( pos.x, pos.z );
 
            //pos = Map.PosToFinalBuildPos( pos, biggestmex );
 
            				ai.sendTextMsg( "Metal spot: " + pos + " " + actualmetalatbestspot );
            ResourceSquare thismetalspot = new ResourceSquare( pos );
 
            //    if (aicallback.CanBuildAt(biggestmex, pos) )
            //  {
            // pos = Map.PosToBuildMapPos( pos, biggestmex );
            // ai.sendTextMsg( "Metal spot: " + pos + " " + BestNormalizedAvailableSpotAmount );
 
            //     if(pos.z >= 2 && pos.x >= 2 && pos.x < mapwidth -2 && pos.z < mapheight -2)
            //      {
            //  if(CanBuildAt(pos.x, pos.z, biggestmex.xsize, biggestmex.ysize))
            // {
            metalspotsal.add( thismetalspot );			
            SpotsFound++;


 
            //if(pos.y >= 0)
            //{
            // SetBuildMap(pos.x-2, pos.z-2, biggestmex.xsize+4, biggestmex.ysize+4, 1);
            //}
            //else
            //{
            //SetBuildMap(pos.x-2, pos.z-2, biggestmex.xsize+4, biggestmex.ysize+4, 5);
            //}
            //  }
            //   }
            //   }
 
            for (int myx = bestspotx - (int)DoubleExtractorRadius; myx < bestspotx + (int)DoubleExtractorRadius; myx++)
            {
               if (myx >= 0 && myx < mapwidth )
               {
                  for (int myy = bestspoty - (int)DoubleExtractorRadius; myy < bestspoty + (int)DoubleExtractorRadius; myy++)
                  {
                     if ( myy >= 0 && myy < mapheight)// &&
                           //( ( bestspotx - myx ) * ( bestspotx - myx ) + ( bestspoty - myy ) * ( bestspoty - myy ) ) <= (int)SquareExtractorRadius )
                     {
                        metalremaining[ myx ][ myy ] = 0; //wipes the metal around the spot so its not counted twice
                        NormalizedSpotAvailableMetal[ myx ][ myy ] = 0;
                     }
                  }
               }
            }
//
////            // Redo the whole averaging process around the picked spot so other spots can be found around it
//            for (int y = bestspoty - (int)DoubleExtractorRadius; y < bestspoty + (int)DoubleExtractorRadius; y++)
//            {
//               if(y >=0 && y < mapheight)
//               {
//                  for (int x = bestspotx - (int)DoubleExtractorRadius; x < bestspotx + (int)DoubleExtractorRadius; x++)
//                  {
//                     //funcion below is optimized so it will only update spots between r and 2r, greatly speeding it up
//                     if((bestspotx - x)*(bestspotx - x) + (bestspoty - y)*(bestspoty - y) <= (int)FourSquareExtractorRadius &&
//                           x >=0 && x < mapwidth &&
//                           NormalizedSpotAvailableMetal[ x ][ y ] > 0 )
//                     {
//                        totalmetal = 0;
//                        for (int myx = x - (int)ExtractorRadius; myx < x + (int)ExtractorRadius; myx++)
//                        {
//                           if (myx >= 0 && myx < mapwidth )
//                           {
//                              for (int myy = y - (int)ExtractorRadius; myy < y + (int)ExtractorRadius; myy++)
//                              {
//                                 if (myy >= 0 && myy < mapheight &&
//                                       ((x - myx)*(x - myx) + (y - myy)*(y - myy)) <= (int)SquareExtractorRadius )
//                                 {
//                                    totalmetal += metalremaining[ myx ][ myy ]; //recalculate nearby spots to account for deleted metal from chosen spot
//                                 }
//                              }
//                           }
//                        }
//                        NormalizedSpotAvailableMetal[ x ][ y ] = totalmetal * 255 / maxmetalspotamount; //set that spots metal amount
//                     }
//                  }
//               }
//            }
         }
      }
 
      if(SpotsFound > 500)
      {
         isMetalMap = true;
         metalspotsal.clear();
         ai.sendTextMsg( "Map is considered to be a metal map" );
      }
      else
      {
         isMetalMap = false;
 
         // debug
         //for(list<AAIMetalSpot>::iterator spot = metal_spots.begin(); spot != metal_spots.end(); spot++)
      }
 
      //SaveCache();
      ai.sendTextMsg( "SearchMetalSpots() <<<");

      return metalspotsal;
   }

   static AvailableMetalResult CalculateAvailableMetalForEachSpot( int[][] metalremaining, int ExtractorRadius, DJAI ai )
   {
      int mapwidth = metalremaining.length;
      int mapheight = metalremaining[0].length;
      int SquareExtractorRadius = (int)( ExtractorRadius * ExtractorRadius );
      int [][]SpotAvailableMetal = new int[ mapwidth][ mapheight ];

      ai.sendTextMsg( "mapwidth: " + mapwidth + " mapheight: " + mapheight + " ExtractorRadius: " + ExtractorRadius + " SquareExtractorRadius: " + SquareExtractorRadius );

      // Now work out how much metal each spot can make by adding up the metal from nearby spots
      // we scan each point on map, and for each point, we scan from + to - ExtractorRadius, eliminating anything where
      // the actual straight line distance is more than the ExtractorRadius
      int maxmetalspotamount = 0;
      for (int spoty = 0; spoty < mapheight; spoty++)
      {
         for (int spotx = 0; spotx < mapwidth; spotx++)
         {
            int metalthisspot = 0;


            metalthisspot = metalremaining[spotx][spoty];
            if(metalthisspot>maxmetalspotamount) maxmetalspotamount=metalthisspot;

            //get the metal from all pixels around the extractor radius
//            for (int deltax = - ExtractorRadius; deltax <= ExtractorRadius; deltax++)
//            {
//               int thisx = spotx + deltax;
//               if ( thisx >= 0 && thisx < mapwidth )
//               {
//                  for (int deltay = - ExtractorRadius; deltay <= ExtractorRadius; deltay++)
//                  {
//                     int thisy = spoty + deltay;
//                     if ( thisy >= 0 && thisy < mapheight )
//                     {
//                        if( ( deltax * deltax + deltay * deltay ) <= SquareExtractorRadius )
//                        {
//                           metalthisspot += metalremaining[ thisx][ thisy ];
//                           if(metalthisspot>maxmetalspotamount) maxmetalspotamount=metalthisspot;
//                        }
//                     }
//                  }
//               }
            //}
            SpotAvailableMetal[ spotx][ spoty ] = metalthisspot; //set that spots metal making ability (divide by cells to values are small)
            //maxmetalspotamount = Math.max( metalthisspot, maxmetalspotamount ); //find the spot with the highest metal to set as the map's max
         }
      }

      //  logfile.WriteLine ("*******************************************");
      return new AvailableMetalResult( SpotAvailableMetal, maxmetalspotamount );
   }

  
}

class ArrayIndexer
{
    int width;
    public ArrayIndexer( int width, int height )
    {
        this.width = width;
    }
    
    // gets an array index for 2d data stored in a 1d array
    public int GetIndex( int x, int y )
    {
        return y * width + x;
    }
}

 class AvailableMetalResult {
      public int [][] availableMetal;
      public int maxMetalAmount;
      public AvailableMetalResult( int[][] availableMetal, int maxMetalAmount ) {
         this.availableMetal = availableMetal;
         this.maxMetalAmount = maxMetalAmount;
      }
   }