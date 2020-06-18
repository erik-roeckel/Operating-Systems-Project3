
/*
  Object to store the results of whichever algorithm is selected and run by the user.
  Used to increment memory accesses, page faults, disk writes for the current alogrithm
  and prints the results after the algorithm is done executing.
 */
public class VMsimResults{

  public int numFrames;
  public int memoryAccesses;
  public int pageFaults;
  public int diskWritesTotal;

  public VMsimResults(int frames, int memAccesses, int numFaults, int writesToDisk){
    this.numFrames = frames;
    this.memoryAccesses = memAccesses;
    this.pageFaults = numFaults;
    this.diskWritesTotal = writesToDisk;
  }

  public void incrementMemAccesses()
  {
    this.memoryAccesses++;
  }

  public void incrementPageFaults()
  {
    this.pageFaults++;
  }

  public void incrementDiskWrites()
  {
    this.diskWritesTotal++;
  }

  /**
    Prints the results of the algorithm
    @param String the name of algorithm that was just run by user
  */
  public void printResults(String algoSelection)
  {
    System.out.println("Algorithm: " + algoSelection);
    System.out.println("Number of frames: " + this.numFrames);
    System.out.println("Total memory accesses: " + this.memoryAccesses);
    System.out.println("Total page faults: " + this.pageFaults);
    System.out.println("Total writes to disk: " + this.diskWritesTotal);
  }

}
