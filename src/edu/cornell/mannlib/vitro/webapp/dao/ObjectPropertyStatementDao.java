/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * Created by IntelliJ IDEA.
 * User: bdc34
 * Date: Apr 18, 2007
 * Time: 7:08:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ObjectPropertyStatementDao {
	
    void deleteObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt);
    
    List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectProperty objectProperty);
    
    List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectProperty objectProperty, int startIndex, int endIndex);

    List<ObjectPropertyStatement> getObjectPropertyStatements(ObjectPropertyStatement objPropertyStmt);
    
    Individual fillExistingObjectPropertyStatements( Individual entity );

    int insertNewObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt );        

//    public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
//			String subjectUri, 
//			String propertyUri, 
//			String objectKey, 
//			String queryString,
//			Set<String> constructQueryStrings);
    
    public Map<String, String> getMostSpecificTypesInClassgroupsForIndividual(String subjectUri);

	List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
			String subjectUri, String propertyUri, String objectKey, String domainUri, 
			String rangeUri, String queryString, Set<String> constructQueryStrings,
			String sortDirection);

	/**
	 * Returns Model representing RDF for object property
	 */
	 public Model getRDFForIndividualByProperty(
	            String subjectUri, 
	            String propertyUri,             
	            String objectKey, String domainUri, String rangeUri,
	            String queryString, 
	            Set<String> constructQueryStrings,
	            String sortDirection);
  
}
