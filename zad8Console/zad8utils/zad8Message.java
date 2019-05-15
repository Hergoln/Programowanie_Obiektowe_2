package zad8Utils;
import java.util.*;
import java.io.Serializable;

public class zad8Message implements Serializable {
  private String content;
  private Date returnTime;

  public zad8Message(){
    this.content = "";
    this.returnTime = new Date();
  }

  public zad8Message(String _content, Date _returnDate){
    this.content = _content;
    this.returnTime = _returnDate;
  }
  
  public String getMessage(){
    return this.content;
  }

  public Date getReturnTime(){
    return this.returnTime;
  }
}