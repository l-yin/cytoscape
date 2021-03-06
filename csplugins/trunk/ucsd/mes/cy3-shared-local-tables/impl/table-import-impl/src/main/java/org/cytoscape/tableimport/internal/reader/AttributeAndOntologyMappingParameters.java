
/*
 Copyright (c) 2006, 2007, 2009, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.tableimport.internal.reader;

//import cytoscape.Cytoscape;

import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;

//import org.biojava.ontology.Ontology;

import java.util.List;


/**
 *
 */
public class AttributeAndOntologyMappingParameters extends AttributeMappingParameters {
	private final int ontologyIndex;
	private final String ontologyName;

	/**
	 * Creates a new AttributeAndOntologyMappingParameters object.
	 *
	 * @param objectType  DOCUMENT ME!
	 * @param delimiters  DOCUMENT ME!
	 * @param listDelimiter  DOCUMENT ME!
	 * @param keyIndex  DOCUMENT ME!
	 * @param mappingAttribute  DOCUMENT ME!
	 * @param aliasIndex  DOCUMENT ME!
	 * @param attributeNames  DOCUMENT ME!
	 * @param attributeTypes  DOCUMENT ME!
	 * @param listAttributeTypes  DOCUMENT ME!
	 * @param importFlag  DOCUMENT ME!
	 * @param ontologyIndex  DOCUMENT ME!
	 * @param ontologyName  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 */
	public AttributeAndOntologyMappingParameters(ObjectType objectType, List<String> delimiters,
	                                             String listDelimiter, int keyIndex,
	                                             String mappingAttribute, List<Integer> aliasIndex,
	                                             String[] attributeNames, Byte[] attributeTypes,
	                                             Byte[] listAttributeTypes, boolean[] importFlag,
	                                             int ontologyIndex, final String ontologyName)
	    throws Exception {
		this(objectType, delimiters, listDelimiter, keyIndex, mappingAttribute,
			 aliasIndex, attributeNames, attributeTypes, listAttributeTypes,
			 importFlag, ontologyIndex, ontologyName, true);
	}

	/**
	 * Creates a new AttributeAndOntologyMappingParameters object.
	 * This constructor takes an additional parameter to allow case sensitivity
	 * to be specified.
	 *
	 * @param objectType  DOCUMENT ME!
	 * @param delimiters  DOCUMENT ME!
	 * @param listDelimiter  DOCUMENT ME!
	 * @param keyIndex  DOCUMENT ME!
	 * @param mappingAttribute  DOCUMENT ME!
	 * @param aliasIndex  DOCUMENT ME!
	 * @param attributeNames  DOCUMENT ME!
	 * @param attributeTypes  DOCUMENT ME!
	 * @param listAttributeTypes  DOCUMENT ME!
	 * @param importFlag  DOCUMENT ME!
	 * @param ontologyIndex  DOCUMENT ME!
	 * @param ontologyName  DOCUMENT ME!
	 * @param caseSensitive  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 */
	public AttributeAndOntologyMappingParameters(ObjectType objectType, List<String> delimiters,
	                                             String listDelimiter, int keyIndex,
	                                             String mappingAttribute, List<Integer> aliasIndex,
	                                             String[] attributeNames, Byte[] attributeTypes,
	                                             Byte[] listAttributeTypes, boolean[] importFlag,
	                                             int ontologyIndex, final String ontologyName,
												 boolean caseSensitive)
	    throws Exception {
		super( delimiters, listDelimiter, keyIndex,
		      attributeNames, attributeTypes, listAttributeTypes, importFlag, caseSensitive);
		this.ontologyName = ontologyName;
		this.ontologyIndex = ontologyIndex;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getOntologyIndex() {
		return ontologyIndex;
	}

}
