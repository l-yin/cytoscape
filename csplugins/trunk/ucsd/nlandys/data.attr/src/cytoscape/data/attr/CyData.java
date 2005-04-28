package cytoscape.data.attr;

/**
 * This interface consists of the API specification to bind attribute
 * values to objects.  A sibling API is CyDataDefinition, which is used
 * to define attribute spaces in which attribute values can be bound
 * to objects; attribute space definition is the first thing that happens.
 */
public interface CyData
{

  /**
   * @param objectKey the object to which to bind a new attribute value.
   * @param attributeName the attribute definition in which to assign an
   *   attribute value.
   * @param attributeValue the attribute value to bind;
   *   the type of this object must be of the appropriate type based on
   *   the value type of specified attribute definition.
   * @param keyIntoValue an array of length equal to the dimensionality of
   *   the key space of specified attribute definition; entry at index i
   *   is a "representative" from dimension i + 1 of the key space; if
   *   specified attribute definition has a zero-dimensional key space (this
   *   is perhaps the most common scenario) then
   *   this array may either be null or the empty array.
   * @return previous attribute value bound at specified key sequence, or
   *   null if no attribute value was previously bound.
   * @exception IllegalStateException if attributeName is not an existing
   *   attribute definition; see CyDataDefinition.
   * @exception NullPointerException if objectKey, attributeName, or
   *   attributeValue is null, or if keyIntoValue is [not null and]
   *   of positive length and any one of its entries is null.
   * @exception ClassCastException if attributeValue is not of the
   *   appropriate object type or if any one of keyIntoValue's representatives
   *   is not of the appropriate object type; see CyDataDefinition.
   * @exception IllegalArgumentException if keyIntoValue's length does not
   *   match the key space dimensionality of attributeName.
   */
  public Object setAttributeValue(String objectKey,
                                  String attributeName,
                                  Object attributeValue,
                                  Object[] keyIntoValue);

  /**
   * @param objectKey the object from which to retrieve a bound attribute
   *   value.
   * @param attributeName the attribute definition in which to assign an
   *   attribute value.
   * @param keyIntoValue an array of length equal to the dimensionality of
   *   the key space of specified attribute definition; entry at index i
   *   is a "representative" from dimension i + 1 of the key space; if
   *   specified attribute definition has a zero-dimensional key space (this
   *   is perhaps the most commen scenario) then this array may either
   *   be null or the empty array.
   * @return the same value that was set with setAttributeValue() with
   *   parameters specified or null if no such value is bound.
   * @exception IllegalStateException if attributeName is not an existing
   *   attribute definition; see CyDataDefinition.
   * @exception NullPointerException if objectKey or attributeName is null,
   *   or if keyIntoValue is [not null and] of positive length and any one
   *   of its entries is null.
   * @exception ClassCastException if any one of keyIntoValue's representatives
   *   is not of the appropriate object type; see CyDataDefinition.
   * @exception IllegalArgumentException if keyIntoValue's length does not
   *   match the key space dimensionality of attributeName.
   */
  public Object getAttributeValue(String objectKey, String attributeName,
                                  Object[] keyIntoValue);


  /**
   * This method is the same as getAttributeValue(), only the retrieved
   * attribute value is also deleted.<p>
   * @see #getAttributeValue(String, String, Object[])
   */
  public Object removeAttributeValue(String objectKey, String attributeName,
                                     Object[] keyIntoValue);

  /**
   * Removes all values bound to objectKey in attributeName.  Most attribute
   * definitions will have no key space, and such attribute definitions will
   * bind at most one attribute value to any give objectKey; this method is
   * useful with attribute definitions that have nonzero key spaces.
   * @param objectKey the object from which to delete all bound attribute
   *   values.
   * @param attributeName the attribute definition in which to delete
   *   attribute values.
   * @return true if and only if objectKey had at least one attribute value
   *   bound in attributeName prior to this method invocation.
   * @exception IllegalStateException if attributeName is not an existing
   *   attribute definition; see CyDataDefinition.
   * @exception NullPointerException if objectKey or attributeName is null.
   */
  public boolean removeAllAttributeValues(String objectKey,
                                          String attributeName);

  /**
   * For all key sequences, having specified prefix, that map into bound
   * values on objectKey in attributeName, returns the [unique] representatives
   * from dimension keyPrefix.length + 1.  This method
   * only makes sense for attributeNames that have nonzero key space
   * dimensionality.<p>
   * NOTE: The returned iterator does not support the remove() operation.<p>
   * IMPORTANT: The returned iterator becomes invalid as soon as any
   * attribute value is set or removed for some objectKey in attributeName.
   * Calling methods on an invalid iterator will result in undefined
   * behavior of that iterator.
   * @param objectKey the object to query.
   * @param attributeName the attribute definition to query.
   * @param keyPrefix an array of length K, where K is strictly less than
   *   the dimensionality of key space of attributeName;
   *   entry at index i contains a "representative" from dimension i + 1 of
   *   the key space of attributeName; keyPrefix may
   *   be either null or the empty array, in which case the iterator
   *   returned consists of the representatives in the first dimension of
   *   key space.
   * @return an iterator of keys in key space dimension K + 1 along
   *   specified keyPrefix; the iterator returned is never null;
   *   the order of the returned keys is arbitrary.
   * @exception IllegalStateException if attributeName is not an existing
   *   attribute definition; see CyDataDefinition.
   * @exception NullPointerException if objectKey or attributeName is null,
   *   or if keyPrefix is [not null and] of positive length and any one
   *   of its entries is null.
   * @exception ClassCastException if any one of keyPrefix's representatives
   *   is not of the appropriate object type; see CyDataDefinition.
   * @exception IllegalArgumentException if keyPrefix's length is not
   *   strictly less than the dimensionality of attributeName's key space.
   */
  public CountedIterator getAttributeKeyspan(String objectKey,
                                             String attributeName,
                                             Object[] keyPrefix);

  /**
   * Returns all objectKeys that have at least one attribute value assigned
   * in attributeName.<p>
   * NOTE: The returned iterator does not support the remove() operation.<p>
   * NOTE: To quickly determine whether or not a given objectKey has at least
   * one attribute value bound to it under attributeName, test for a null
   * return value in getAttributeValue(objectKey, attributeName, null)
   * if attributeName has zero key space dimensionality, or test for an empty
   * iterator return value in
   * getAttributeKeyspan(objectKey, attributeName, null) if attributeName has
   * nonzero key space dimensionality.<p>
   * IMPORTANT: The returned iterator becomes invalid as soon as any
   * attribute value is set or removed for some objectKey in attributeName.
   * Calling methods on an invalid iterator will result in undefined
   * behavior of that iterator.
   * @param attributeName the attribute definition to query.
   * @return an iterator of objectKey strings (java.lang.String) that
   *   currently have value[s] assigned to them in the specified attribute
   *   definition; the order of the returned strings is arbitrary; null is
   *   never returned.
   * @exception IllegalStateException if attributeName is not an existing
   *   attribute definition; see CyDataDefinition.
   * @exception NullPointerException if attributeName is null.
   */
  public CountedIterator getObjectKeys(String attributeName);

  /*
   * I'm not including this method in the API for good reasons.
   * For one, removal is the opposite of insertion, and even though
   * we could potentially remove many entries at once for free, because
   * we inserted them one-by-one, the time complexity for a complete
   * insert-remove cycle of many elements is governed by the insertion
   * time complexity.  Therefore we don't lose in overall time complexity if
   * we omit this removal optimization.  Second, I want listeners to hear
   * all removals that happen.  If I include this method, I either have
   * to iterate over all values that have been removed to fire appropriate
   * listener events (which defeats the purpose of this optimization)
   * or I have to define a separate listener method that
   * says "hey I've removed a keyspan", but then the listener would not
   * get a list of values (corresponding to all keys, recursively, in
   * keyspan) that were removed.
   */
//   public int removeAttributeKeyspan(String objectKey,
//                                     String attributeName,
//                                     Object[] keyPrefix);

  public void addDataListener(CyDataListener listener);

  public void removeDataListener(CyDataListener listener);

}
