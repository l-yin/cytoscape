package cytoscape.data.attr;

public interface CyDataListener
{

  /**
   * @param keyIntoValue don't modify this array; this array will be
   *   null if attributeName has a zero-dimensional keyspace.
   * @param oldAttributeValue the previous attribute value that was bound
   *   at specified key sequence, or null if no previous attribute value was
   *   bound.
   * @param newAttributeValue the new attribute value that is now bound
   *   at specified key sequence.
   */
  public void attributeValueAssigned(String objectKey,
                                     String attributeName,
                                     Object[] keyIntoValue,
                                     Object oldAttributeValue,
                                     Object newAttributeValue);

  /**
   * @param keyIntoValue don't modify this array; this array will be
   *   null if attributeName has a zero-dimensional keyspace.
   */
  public void attributeValueRemoved(String objectKey,
                                    String attributeName,
                                    Object[] keyIntoValue,
                                    Object attributeValue);

}
