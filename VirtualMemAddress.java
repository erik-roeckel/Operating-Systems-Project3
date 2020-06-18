/**
  Object that stores the access type (load or store) and
  page number calculated from hex virtual addresses in trace file.
*/

public class VirtualMemAddress{
  private char accessType;
  private int pageNum;

  /**
    @param char acces type taken from current line of trace file
    @param int page number calculated from first 5 digits of hex address
  */
  public VirtualMemAddress(char access, int page)
  {
    accessType = access;
    pageNum = page;
  }

  public char getAccessType()
  {
    return this.accessType;
  }

  public int getPageNum()
  {
    return this.pageNum;
  }
}
