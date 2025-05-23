package ToxPredictor.misc;

import java.util.*;

/**
sarray is a convenience class designed to make ArrayList methods consistent 
 with Vector.    Why did Sun not do this??
*/

/************************************************/
public class sarray extends ArrayList
{
private int last_index = 0;

/************************************************/
public int length ()
  {
  return ( size ( ) );
  }

/************************************************/
public Object elementAt ( int index )
  {
  last_index = index;
  return ( get ( index ) );
  }

/************************************************/
public void addElement ( Object o )
  {
  add ( o );
  }

/************************************************/
/**
Sets the last index requested to the supplied value.
*/

public void setLastIndex(int value)
  {
  last_index = value;
  }

/************************************************/
/**
Returns the last index requested by the elementAt method.
*/

public int getLastIndex()
  {
  return ( last_index );
  }
  
/************************************************/
/**
Make compatible with Vector.
*/
public void removeElementAt ( int index )
  {
  remove ( index );
  }

/************************************************/
/**
Make compatible with Vector.
*/
public void removeElement ( Object obj )
  {
  int index = indexOf ( obj );

  if ( index > -1 ) remove ( index );
  }
}
