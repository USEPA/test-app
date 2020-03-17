package ToxPredictor.misc;

import java.io.*;
import java.util.*;

public class sortable
implements Comparable 
{
private  Comparable key = "";
private  Object       o = null;

/************************************************/
public sortable ( Comparable key , Object o )
  {
  this.key = key;
  this.o = o;
  }

/************************************************/
public void setKey ( Comparable key )
  {
  this.key = key;
  }

/************************************************/
public Comparable getKey()
  {
  return ( key );
  }

/************************************************/
public void setObject ( Object o )
  {
  this.o = o;
  }

/************************************************/
public Object getObject()
  {
  return ( o );
  }

/************************************************/
public int compareTo ( Object o )
  {
  return ( key.compareTo ( ((sortable) o).getKey() ) );
  }  
}


