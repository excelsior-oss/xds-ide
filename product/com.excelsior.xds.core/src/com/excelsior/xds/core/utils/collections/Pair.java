package com.excelsior.xds.core.utils.collections;

/**
 * Utility class representing pair of objects
 * 
 * @param <K>
 * @param <V>
 */
public class Pair<K, V> {
    
  private K first;
  private V second;

  public Pair(K first, V second) {
    super();
    this.first = first;
    this.second = second;
  }
  
  public static <K, V> Pair<K,V> create(K first, V second){
      return new Pair<K, V>(first, second);
  }
  
  @SuppressWarnings("unchecked")
  public static <K, V> Pair<K,V>[] createArray(int size) {
	  return new Pair[size];
  }

  public K getFirst() {
    return first;
  }

  public void setFirst(K first) {
    this.first = first;
  }

  public V getSecond() {
    return second;
  }

  public void setSecond(V second) {
    this.second = second;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pair<?, ?> other = (Pair<?, ?>) obj;
    if (first == null) {
      if (other.first != null)
        return false;
    } else if (!first.equals(other.first))
      return false;
    if (second == null) {
      if (other.second != null)
        return false;
    } else if (!second.equals(other.second))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Pair [First=" + first + ", Second=" + second + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
