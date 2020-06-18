
/**
  Object to store and retrieve information about page table entries that are currently in RAM.
  Sets diry bit to true if address has access type of store (s)
  Sets referenced bit to true if address is referenced while in RAM
  Sets page frame number based on the index of the current frame in RAM where this page exists
*/
public class PTE {
  private boolean dirtyBit;
  private boolean referencedBit;
  private boolean validBit;
  private int pageFrameNum;

  public PTE(){
    dirtyBit = false;
    referencedBit = false;
    validBit = false;
    pageFrameNum = -1;
  }

  public void setDirtyBit(boolean dirty)
  {
    dirtyBit = dirty;
  }

  public void setReferencedBit(boolean referenced)
  {
    referencedBit = referenced;
  }

  public void setValidBit(boolean valid)
  {
      validBit = valid;
  }

  public void setPageFrameNum(int f)
  {
    pageFrameNum = f;
  }

  public boolean getDirtyBit()
  {
    return dirtyBit;
  }

  public boolean getReferencedBit()
  {
    return referencedBit;
  }

  public boolean getValidBit()
  {
    return validBit;
  }

  public int getPageFrameNum()
  {
    return pageFrameNum;
  }

  /**
    To string method used for debugging
    @return String of page table entry information
  */
  public String toString()
  {
    String pteString = "page frame num: " + this.pageFrameNum + "\n"
                    + "dirty Bit: " + this.dirtyBit + "\n"
                    + "referenced Bit: " + this.referencedBit + "\n"
                    + "valid bit" + this.validBit + "\n";
    return pteString;
  }
}
