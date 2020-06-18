import java.io.*;
import java.util.*;
import java.lang.*;

/**
  Class that contains all the logic for executing the OPT, LRU, and second chance algorithms
  based on the number of frames, algorithm, and trace file selected by user
*/
public class PageReplacementLogic
{
  private int numFrames;
  private String algoSelection;
  private String traceFile;
  private final int pageSize = 4096;

  public PageReplacementLogic(int frames, String algo, String file)
  {
    this.numFrames = frames;
    this.algoSelection = algo;
    this.traceFile = file;
  }

  /**
    Takes memory addresses from file and stores them in arraylist of virtual memory address objects
    @param file tracefile selected by used
    @throws FileNotFoundException
    @return arraylist of virtual memory addresses for each virtual memory adddress in trace file
  */
  public ArrayList<VirtualMemAddress> parseFile(String file) throws FileNotFoundException
  {
    File memFile = new File(file);
    Scanner scan = new Scanner(memFile);
    ArrayList<VirtualMemAddress> vmAddresses = new ArrayList<VirtualMemAddress>();

    while(scan.hasNext())
    {
      String line = scan.nextLine();
      char accessType = line.charAt(0);
      String hexString = line.substring(4, 9);
      int hexAddress = Integer.parseInt(hexString, 16);
      VirtualMemAddress vmAddress = new VirtualMemAddress(accessType, hexAddress);
      vmAddresses.add(vmAddress);
    }
    return vmAddresses;
  }

  /**
    Utilizes a HashMap where keys are the page numbers and values are linked lists
    that store the order of each pageNumber done by preprocessing in order to evict the
    page from RAM that is used farthest in future
    @throws FileNotFoundException
  */
  public void OPT() throws FileNotFoundException
  {
    ArrayList<VirtualMemAddress> addressList = parseFile(this.traceFile); // parses tracefile into arraylist of virtual addresses
    VMsimResults simResults = new VMsimResults(numFrames, 0, 0, 0);
    if(addressList.size() == 0)
    {
      simResults.printResults("OPT");
      return;
    }
    ArrayList<Integer> RAM = new ArrayList<Integer>(numFrames); // frames in physical memory
    PTE[] pageTableEntries = new PTE[(int)(Math.pow(2, 32)/pageSize)]; // page table managed by MMU
    HashMap<Integer, LinkedList<Integer>> futureUse = new HashMap<Integer, LinkedList<Integer>>(); // stores future allocations for specific page numbers
    int victimFrame = 0;
    int usedFrames = 0;
    int victimNum = 0;

    // preprocess virtual addresses into hashmap for future use
    for(int i=0; i < addressList.size(); i++)
    {
      int pageNum = addressList.get(i).getPageNum();

      // if key doesn't exist in hashmap then add it with a new linked list
      if(futureUse.containsKey(pageNum) == false)
      {
        futureUse.put(pageNum, new LinkedList<Integer>());
      }
      futureUse.get(pageNum).add(i); // add the rank (most recent usage) of page number to linked list
    }

    for(int i=0; i < addressList.size(); i++)
    {
      int pageNum = addressList.get(i).getPageNum();
      char accessType = addressList.get(i).getAccessType();
      simResults.incrementMemAccesses();

      if(pageTableEntries[pageNum] == null)
      {
        pageTableEntries[pageNum] = new PTE();
      }
      if(accessType == 's')
      {
        pageTableEntries[pageNum].setDirtyBit(true);
      }

      if(RAM.contains(pageNum))
      {
        pageTableEntries[pageNum].setReferencedBit(true);
      }
      else
      {
        simResults.incrementPageFaults();
        // Enters control block is there is an open frame and thus no eviciton needed
        if(RAM.size() < numFrames)
        {
          pageTableEntries[pageNum].setPageFrameNum(usedFrames);
          RAM.add(usedFrames, pageNum);
          ++usedFrames;
        }
        else
        {
          int frameInfo[] = findVictimFrame(futureUse, RAM, i);
          victimNum = frameInfo[0]; // the page number of victim
          victimFrame = frameInfo[1]; // the current frame number in RAM of victim

          if(pageTableEntries[victimNum].getDirtyBit() == true)
          {
            simResults.incrementDiskWrites();
          }

          RAM.set(victimFrame, pageNum);
          pageTableEntries[pageNum].setPageFrameNum(victimFrame);
          // reset the victim page's info in the page table
          pageTableEntries[victimNum].setPageFrameNum(-1);
          pageTableEntries[victimNum].setDirtyBit(false);
          pageTableEntries[victimNum].setReferencedBit(false);
        }
      }
    }
    simResults.printResults("OPT");
  }

  /**
      Finds the victim page in RAM by finding the page number in RAM that has an empty linked list
      or has the farthest future allocation stored at the head of each linked list
      @param HashMap of future allocations to ram for preprocess address list
      @param ArrayList containg pages currently in RAM
      @param int line number of address in trace file
      @throws NoSuchElementException
      @return int array containing the victimFrame to be removed from RAM and the page number of that victim
  */
  public int[] findVictimFrame(HashMap<Integer, LinkedList<Integer>> futureAllocations, ArrayList<Integer> Frames, int lineNum) throws NoSuchElementException
  {
    int victimPageNum = 0;
    int furthestUse = 0;
    int rank = 0;
    int victimFrameNum = 0;
    int[] frameInfo = new int[2];

    // Iterate through all of RAM
    for(int i=0; i < Frames.size(); i++)
    {
      // if the linked list for this page number at Frame(i) is null then make this the victim frame
      if(futureAllocations.get(Frames.get(i)).peek() == null)
      {
        victimPageNum = Frames.get(i);
        victimFrameNum = i;
        break;
      }
      else
      {
        // find current page number in RAM that has farthese use in future hashmap
        rank = futureAllocations.get(Frames.get(i)).peek();

        // loop through the ranks in the linked list for this specific page number to make sure it is not larger than the current line number
        while(rank < lineNum)
        {
          futureAllocations.get(Frames.get(i)).poll();
          if(futureAllocations.get(Frames.get(i)).peek() != null)
          {
            rank = futureAllocations.get(Frames.get(i)).peek();
          }
          else
          {
            rank = -1;
            break;
          }
        }
        /*
          if there are still nodes in linked list for this page then determine if the head of the linked list
          for that page number is larger than the current furthest usage
        */
        if(rank != -1)
        {
          if (rank > furthestUse)
          {
            furthestUse = rank;
            victimPageNum = Frames.get(i);
            victimFrameNum = i;
          }
        }
        // if there are no more elements in linked list for this page then choose current page as victim
        else
        {
          victimPageNum = Frames.get(i);
          victimFrameNum = i;
          break;
        }
      }
    }

    frameInfo[0] = victimPageNum;
    frameInfo[1] = victimFrameNum;
    return frameInfo;
  }


  /**
    Utilizes an arraylist to store the page numbers that are currently in RAM
    To keep track of the least recently used if the current page number trying to load or
    store in RAM is already in RAM we remove it from RAM and add it to the back of RAM in order to maintain
    a queue in RAM. If there is a page fault then the first item in RAM is the victim and is removed, and
    the new page is added to the back of the RAM queue
    @throws FileNotFoundException
  */

  public void LRU() throws FileNotFoundException
  {
    ArrayList<VirtualMemAddress> addressList = parseFile(this.traceFile);
    VMsimResults simResults = new VMsimResults(numFrames, 0, 0, 0);
    if(addressList.size() == 0)
    {
      simResults.printResults("LRU");
      return;
    }
    PTE[] pageTableEntries = new PTE[(int)(Math.pow(2, 32)/pageSize)]; // pages in virtual memory
    ArrayList<Integer> RAM = new ArrayList<Integer>(numFrames); // pages loaded in physical memory
    int victimFrame = 0;
    int usedFrames = 0;
    int victimNum = 0;

    for(int i=0; i < addressList.size(); i++)
    {
      int pageNum = addressList.get(i).getPageNum();
      char accessType = addressList.get(i).getAccessType();
      simResults.incrementMemAccesses();

      if(pageTableEntries[pageNum] == null)
      {
        pageTableEntries[pageNum] = new PTE();
      }

      if(accessType == 's')
      {
        pageTableEntries[pageNum].setDirtyBit(true);
      }

      // if page is already in physical memory enter control block
      if(RAM.contains(pageNum))
      {
        RAM.remove((Integer)pageNum); // remove page number from current index in arraylist (essentially a queue)
        RAM.add(RAM.size(), pageNum); // add page number to back of physical memory
      }

      else
      {
        simResults.incrementPageFaults();

        // if physical memory is full victim will be at front of RAM queue
        if(RAM.size() == numFrames)
        {
          victimNum = RAM.remove(0); // remove the least recently used page from RAM
          RAM.add(numFrames - 1, pageNum); // add the new page being allocated to RAM at the back of the arraylist (queue)
          if(pageTableEntries[victimNum].getDirtyBit())
          {
            simResults.incrementDiskWrites();
            pageTableEntries[pageNum].setPageFrameNum(victimFrame);

            // reset the victim page's info in the page table
            pageTableEntries[victimNum].setPageFrameNum(-1);
            pageTableEntries[victimNum].setDirtyBit(false);
            pageTableEntries[victimNum].setReferencedBit(false);

          }
          else
          {
            pageTableEntries[pageNum].setPageFrameNum(victimFrame);

            // reset the victim page's info in the page table
            pageTableEntries[victimNum].setPageFrameNum(-1);
            pageTableEntries[victimNum].setDirtyBit(false);
            pageTableEntries[victimNum].setReferencedBit(false);

          }
        }
        // if physical memory is not full, add the new page into RAM at the next available slot
        else
        {
          RAM.add(usedFrames, pageNum);
          ++usedFrames;
        }
      }
    }
    simResults.printResults("LRU");
  }

  /**
    Utilizes circular arraylist of RAM that represents a clock containing each frame in RAM
    If a page fault occurs and RAM is full this algorithm iterates through the RAM circular array
    until it finds a page that hasn't been referenced while inside RAM
    @throws FileNotFoundException
  */
  public void secondChance() throws FileNotFoundException
  {
    ArrayList<VirtualMemAddress> addressList = parseFile(this.traceFile);
    VMsimResults simResults = new VMsimResults(numFrames, 0, 0, 0);
    if(addressList.size() == 0)
    {
      simResults.printResults("SECOND");
      return;
    }
    PTE[] pageTableEntries = new PTE[(int)(Math.pow(2, 32)/pageSize)]; // pages in virtual memory
    ArrayList<Integer> RAM = new ArrayList<Integer>(numFrames); // pages loaded in physical memory
    int index = 0;
    int victimNum = 0;

    for(int i=0; i < addressList.size(); i++)
    {
      int pageNum = addressList.get(i).getPageNum();
      char accessType= addressList.get(i).getAccessType();
      simResults.incrementMemAccesses();

      if(pageTableEntries[pageNum] == null)
      {
        pageTableEntries[pageNum] = new PTE();
      }

      if(accessType == 's')
      {
        pageTableEntries[pageNum].setDirtyBit(true);
      }

      // if page number is already in RAM then set its reference bit to true
      if(RAM.contains(pageNum))
      {
        pageTableEntries[pageNum].setReferencedBit(true);
      }
      // if page number is not in RAM then we have a page fault
      else
      {
        simResults.incrementPageFaults();

        //if RAM is not full then allocate this page to the next available slot in RAM
        if(RAM.size() < numFrames)
        {
          RAM.add(index, pageNum);
        }
        else
        {
          /*
            iterate through RAM until we find a page that has not been referenced while in RAM
            If the page has been referenced then set its reference bit to false and move the pointer to the next frame
          */
          while(pageTableEntries[RAM.get(index)].getReferencedBit() == true)
          {
            pageTableEntries[RAM.get(index)].setReferencedBit(false);
            index = (index + 1) % numFrames;
          }
          victimNum = RAM.get(index);
          if(pageTableEntries[victimNum].getDirtyBit() == true)
          {
            simResults.incrementDiskWrites();
          }
          // reset the victim page's info in the page table
          pageTableEntries[victimNum].setPageFrameNum(-1);
          pageTableEntries[victimNum].setDirtyBit(false);
          pageTableEntries[victimNum].setReferencedBit(false);
        }
          pageTableEntries[pageNum].setPageFrameNum(index);
          RAM.set(index, pageNum);
          index = (index + 1) % numFrames;

        }

    }
    simResults.printResults("SECOND");
  }
}
