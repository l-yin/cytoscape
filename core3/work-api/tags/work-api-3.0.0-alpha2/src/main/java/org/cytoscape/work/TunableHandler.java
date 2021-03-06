package org.cytoscape.work;


import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;


/** Interface for classes that deal with reading out and writing back <code>Tunable</code>s and their properties.
 */
public interface TunableHandler {
	/**
	 * Returns an object describing a field annotated with @Tunable or null if no field has been associated with this handler.
	 * @return an object describing a field annotated with @Tunable or null if no field has been associated with this handler
	 */
	Object getValue() throws IllegalAccessException, InvocationTargetException;

	/** Attempts to set the value "newValue" on the associated Tunable.
	 *  @param newValue the value to be written into the tunable property
	 */
	void setValue(final Object newValue) throws IllegalAccessException, InvocationTargetException;

	/**
	 *  Returns the associated <code>Tunable</code>'s description.
	 *  @return the associated <code>Tunable</code>'s description
	 */
	String getDescription();

	/**
	 *  Returns the associated <code>Tunable</code>'s groups or nesting hierarchy.
	 *  @return the associated <code>Tunable</code>'s groups or nesting hierarchy
	 */
	String[] getGroups();

	/**
	 *  Returns true if the associated <code>Tunable</code> allows switching of mutually exclusive nested children, else false.
	 *  @return true if the associated <code>Tunable</code> allows switching of mutually exclusive nested children, else false
	 */
	boolean controlsMutuallyExclusiveNestedChildren();

	/**
	 *  Returns the name of the key that determines the selection of which controlled
	 *          nested child is currently presented, or the empty string if
	 *          controlsMutuallyExclusiveNestedChildren() returns false.
	 *  @return the name of the key that determines the selection of which controlled
	 *          nested child is currently presented, or the empty string if
	 *          controlsMutuallyExclusiveNestedChildren() returns false.
	 */
	String getChildKey();

	/**
	 *  Returns the dependsOn property of the associated <code>Tunable</code>.
	 *  @return the dependsOn property of the tunable
	 */
	String dependsOn();

	/**
	 *  Returns a name representing a tunable property.
	 *  @return a name representing a tunable property
	 */
	String getName();

	/**
	 *  Returns the name of the underlying class of the tunable followed by a dot and the name of the tunable field or getter/setter root name.
	 *  @return the name of the underlying class of the tunable followed by a dot and the name of the tunable field or getter/setter root name.
	 *
	 *  Please note that the returned String will always contain a single embedded dot.
	 */
	String getQualifiedName();

	/**
	 *  Returns the parsed result from <code>Tunable.getParams()</code>.
	 *  @return the parsed result from <code>Tunable.getParams()</code>
	 */
	Properties getParams();
}
