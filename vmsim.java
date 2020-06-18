import java.io.*;
import java.util.*;

/**
  Main program to simulate virtual memory allocation
  Parses the input from the command line to select the number of frames in RAM
  The page replacement algorithm to run
  The trace file to read in virtual memory addresses from and allocate to RAM
*/
public class vmsim {

    static int numFrames;
    static String algorithmSelection;
    static String traceFile;

    public static void main(String[] args) throws Exception
    {
      numFrames = Integer.parseInt(args[1]);
      algorithmSelection = args[3];
      traceFile = args[4];
      PageReplacementLogic replacementAlgo = new PageReplacementLogic(numFrames, algorithmSelection, traceFile);
      if(algorithmSelection.equals("opt"))
      {
        replacementAlgo.OPT();
      }
      else if(algorithmSelection.equals("lru"))
      {
        replacementAlgo.LRU();
      }
      else if(algorithmSelection.equals("second"))
      {
        replacementAlgo.secondChance();
      }
    }

    /*
      Simulates running one of the three algorthims with varying numbers of frames: 8, 16, 32, 64
      Used for part one of writeup
    */
    public static void testAlgorithmPerformance() throws FileNotFoundException
    {
      int[] testFrames = new int[4];
      testFrames[0] = 8;
      testFrames[1] = 16;
      testFrames[2] = 32;
      testFrames[3] = 64;

      PageReplacementLogic replacementAlgo;

      for(int i =0; i < testFrames.length; i++)
      {
        replacementAlgo = new PageReplacementLogic(testFrames[i], algorithmSelection, traceFile);
        if(algorithmSelection.equals("opt"))
        {
          replacementAlgo.OPT();
        }
        else if(algorithmSelection.equals("lru"))
        {
          replacementAlgo.LRU();
        }
        else if(algorithmSelection.equals("second"))
        {
          replacementAlgo.secondChance();
        }
      }
    }

    /*
      Method to check second chance algorithm for belady's anomaly
    */
    public static void testSecondChance() throws FileNotFoundException
    {
      int[] testFrames = new int[100];
      PageReplacementLogic replacementAlgo;
      for(int i = 0; i < testFrames.length; i++)
      {
        testFrames[i] = i + 1;
      }

      for(int i = 0; i < testFrames.length; i++)
      {
        replacementAlgo = new PageReplacementLogic(testFrames[i], algorithmSelection, traceFile);
        replacementAlgo.secondChance();
      }

    }

}
