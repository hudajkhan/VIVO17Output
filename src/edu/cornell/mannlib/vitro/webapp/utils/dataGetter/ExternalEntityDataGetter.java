/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;

//This data getter is used to get information about an external entity that is same AS or somehow linked
//with an entity in the local instance
//The data getter should have a query associated, retrieving all URIs that are same AS or equivalent to
//the URI in question
//Then information about the service for the external entity is retrieved and used to generate
//a request to the external service for that external entity
//The information is then parsed and presented back - potentially in multiple ways
//i.e. RDF ?  and/or a HashMap for the template
//Associated: Query to get associated external entity and the relevant associated information
//PropertyURI for properties that you want to get back specifically for display
public class ExternalEntityDataGetter extends DataGetterBase implements DataGetter{
	private final static Log log = LogFactory.getLog(SparqlQueryDataGetter.class);
	
    private static final String queryPropertyURI = "<" + DisplayVocabulary.QUERY + ">";
    //TODO: Add this to display tbox as actual property
    private static final String displayPropertyURI = "<" + DisplayVocabulary.DISPLAY_NS + "selectedPropertyForDisplay>";
    private static final String displayPropertyDomainURI = "<" + DisplayVocabulary.DISPLAY_NS + "selectedPropertyForDisplayDomain>";
    private static final String displayPropertyRangeURI = "<" + DisplayVocabulary.DISPLAY_NS + "selectedPropertyForDisplayRange>";

    
    
    private static final String saveToVarPropertyURI= "<" + DisplayVocabulary.SAVE_TO_VAR+ ">";

    public static final String defaultVarNameForResults = "results";
    private static final String defaultTemplate = "menupage--defaultSparql.ftl";

    String dataGetterURI;
    String queryText;
    String saveToVar;
    String selectedPropertyURI;
    String selectedPropertyDomainURI;
    String selectedPropertyRangeURI;
    VitroRequest vreq;
    ServletContext context;
    
    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public ExternalEntityDataGetter(VitroRequest vreq, Model displayModel, String dataGetterURI){
        this.configure(vreq, displayModel,dataGetterURI);
    }        
    
	/**
     * Configure this instance based on the URI and display model.
     */
    @SuppressWarnings("hiding")
	protected void configure(VitroRequest vreq, Model displayModel, String dataGetterURI) {
    	if( vreq == null ) 
    		throw new IllegalArgumentException("VitroRequest  may not be null.");
        if( displayModel == null ) 
            throw new IllegalArgumentException("Display Model may not be null.");
        if( dataGetterURI == null )
            throw new IllegalArgumentException("PageUri may not be null.");
                
        this.vreq = vreq;
        this.context = vreq.getSession().getServletContext();
        this.dataGetterURI = dataGetterURI;        
        
        QuerySolutionMap initBindings = new QuerySolutionMap();
        initBindings.add("dataGetterURI", ResourceFactory.createResource(this.dataGetterURI));
        
        Query dataGetterConfigurationQuery = QueryFactory.create(dataGetterQuery) ;               
        displayModel.enterCriticalSection(Lock.READ);
        try{
            QueryExecution qexec = QueryExecutionFactory.create(
                    dataGetterConfigurationQuery, displayModel, initBindings) ;        
            ResultSet res = qexec.execSelect();
            try{                
                while( res.hasNext() ){
                    QuerySolution soln = res.next();
                    
                    //query is NOT OPTIONAL
                    Literal value = soln.getLiteral("query");
                    if( dataGetterConfigurationQuery == null )
                        log.error("no query defined for page " + this.dataGetterURI);
                    else
                        this.queryText = value.getLexicalForm();                    
                    
                   
                    //savetovar - optional
                    Literal saveTo = soln.getLiteral("saveToVar");
                    if( saveTo != null && saveTo.isLiteral() ){
                        this.saveToVar = saveTo.asLiteral().getLexicalForm();                        
                    }else{
                        this.saveToVar = defaultVarNameForResults;
                    }
                    
                    //property information
                    RDFNode node = soln.get("selectedPropertyURI");
                    if( node != null && node.isURIResource() ){
                    	
                        this.selectedPropertyURI = node.asResource().getURI();                                         
                    }else{
                    	this.selectedPropertyURI = null;
                    }
                    
                    //domain and range are optional
                    node = soln.get("selectedPropertyDomainURI");
                    if( node != null && node.isURIResource() ){
                    	
                        this.selectedPropertyDomainURI = node.asResource().getURI();                                         
                    }else{
                    	this.selectedPropertyDomainURI = null;
                    }
                    
                    node = soln.get("selectedPropertyRangeURI");
                    if( node != null && node.isURIResource() ){
                    	
                        this.selectedPropertyRangeURI = node.asResource().getURI();                                         
                    }else{
                    	this.selectedPropertyRangeURI = null;
                    }
                        
                }
            }finally{ qexec.close(); }
        }finally{ displayModel.leaveCriticalSection(); }                
    }
    
    /**
     * Query to get the definition of the SparqlDataGetter for a given URI.
     */
    private static final String dataGetterQuery =
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n" +
        "SELECT ?query ?saveToVar ?selectedPropertyURI ?selectedPropertyDomainURI ?selectedPropertyRangeURI WHERE { \n" +
        "  ?dataGetterURI "+queryPropertyURI+" ?query . \n" +
        " ?dataGetterURI display:selectedPropertyForDisplay ?selectedPropertyURI . \n" + 
        " OPTIONAL { ?dataGetterURI display:selectedPropertyForDisplayDomain ?selectedPropertyDomainURI . } \n " + 
        " OPTIONAL { ?dataGetterURI display:selectedPropertyForDisplayRange ?selectedPropertyRangeURI . } \n" + 
        "  OPTIONAL{ ?dataGetterURI "+saveToVarPropertyURI+" ?saveToVar } \n " +
        "}";      

    //For now, combining data getter-specific info, e.g. which properties do you want to retrieve
    //with information about the external uri and where to access the information
   
    @Override
    public Map<String, Object> getData(Map<String, Object> pageData) { 
    	HashMap<String, Object> templateData = new HashMap<String, Object>();
    	Map<String, String> merged = mergeParameters(vreq.getParameterMap(), pageData);
    	
    	String boundQueryText = bindParameters(queryText, merged);
    	ResultSet rs = doQueryOnRDFService(boundQueryText);
    	List<ExternalURIInformation> externalURIInfo = new ArrayList<ExternalURIInformation>();
    	
    	//Query results should include the serviceURL and service name
    	//?externalURI ?externalServiceURI ?externalServiceName ?externalServiceURL
    	while(rs.hasNext()) {
    		QuerySolution qs = rs.nextSolution();
    		String externalURI = toCell(qs.get("externalURI"));
    		
    		
    		//What if there are multiple external URIs or results
    		if(StringUtils.isNotEmpty(externalURI)) {
    			String externalServiceURI = toCell(qs.get("externalServiceURI"));
        		String externalServiceName = toCell(qs.get("externalServiceName"));
        		String externalServiceURL = toCell(qs.get("externalServiceURL"));
        		
        		ExternalURIInformation eInfo = new ExternalURIInformation(externalURI, externalServiceURI, externalServiceName, externalServiceURL);
        		
        		//Put in the property information
            	//TODO: Use saveToVar instead of the above
        		eInfo.setPropertyURI(this.selectedPropertyURI);
        		if(StringUtils.isNotEmpty(this.selectedPropertyDomainURI)) {
        			eInfo.setPropertyDomainURI(this.selectedPropertyDomainURI);
        			
        		}
        		if(StringUtils.isNotEmpty(this.selectedPropertyRangeURI)) {
        			eInfo.setPropertyRangeURI(this.selectedPropertyRangeURI);
        		}
        		externalURIInfo.add(eInfo);
    			
    		}
    	}
    	
    	if(pageData.containsKey("externalURIInfo")) {
    		List<ExternalURIInformation> pageEInfoList = (List<ExternalURIInformation>)pageData.get("externalURIInfo");
    		externalURIInfo.addAll(pageEInfoList);
    	}
    	log.debug("Retrieved external data getter info and returning template data");
    	templateData.put("externalURIInfo", externalURIInfo);
    	
    	return templateData; 
    }

    /** Merge the pageData with the request parameters. PageData overrides. */
	private Map<String, String> mergeParameters(
			Map<String, String[]> parameterMap, Map<String, Object> pageData) {
		Map<String, String> merged = new HashMap<>();
		for (String key: parameterMap.keySet()) {
			merged.put(key, parameterMap.get(key)[0]);
		}
		for (String key: pageData.keySet()) {
			merged.put(key, String.valueOf(pageData.get(key)));
		}
		if (log.isDebugEnabled()) {
			log.debug("Merging request parameters " + parameterMap
					+ " with page data " + pageData + " results in " + merged);
		}
		return merged;
	}

	/**
	 * InitialBindings don't always work, and besides, RDFService doesn't accept
	 * them. So do a text-based substitution.
	 * 
	 * This assumes that every parameter is a URI. What if we want to substitute
	 * a string value?
	 */
	private String bindParameters(String text, Map<String, String> merged) {
		String bound = text;
		for (String key : merged.keySet()) {
			bound = bound.replace('?' + key, '<' + merged.get(key) + '>');
		}
		if (log.isDebugEnabled()) {
			log.debug("parameters: " + merged);
			log.debug("query before binding parameters:" + text);
			log.debug("query after binding parameters: " + bound);
		}
		return bound;
	}

	/**
	 * Do the query and return a result set
	 */
	protected ResultSet doQueryOnRDFService(String  q) {
		log.debug("Going to RDFService with " + q);
        ResultSet results = QueryUtils.getQueryResults(q, vreq);
       return results; 
	}

    
    
	private Query makeQuery(String q) {
		try {
			return QueryFactory.create(q);
		} catch (Exception e) {
			log.error("Failed to build a query from ''", e);
			return null;
		}
	}

	private List<Map<String, String>> executeQuery(Query query, Model model) {
        model.enterCriticalSection(Lock.READ);        
        try{            
            QueryExecution qexec= QueryExecutionFactory.create(query, model );
            ResultSet results = qexec.execSelect();
            try{                
            	return parseResults(results);
            }finally{ qexec.close(); }
        }finally{ model.leaveCriticalSection(); }
    }

    /**
     * Converts a ResultSet into a List of Maps.
	 */
	private List<Map<String, String>> parseResults(ResultSet results) {
        List<Map<String,String>> rows = new ArrayList<Map<String,String>>();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            rows.add( toRow( soln ) );
        }                   
        return rows;        
	}

	/**
     * Converts a row from a QuerySolution to a Map<String,String> 
     */
    private Map<String, String> toRow(QuerySolution soln) {
        HashMap<String,String> row = new HashMap<String,String>();        
        Iterator<String> varNames = soln.varNames();
        while( varNames.hasNext()){
            String varname = varNames.next();            
            row.put(varname, toCell( soln.get(varname)));            
        }
        return row;
    }
    
    private String toCell(RDFNode rdfNode) {
        if( rdfNode == null){
            return "";
        }else if( rdfNode.isLiteral() ){
            return rdfNode.asLiteral().getLexicalForm();
        }else if( rdfNode.isResource() ){
            Resource resource = (Resource)rdfNode;
            if( ! resource.isAnon() ){
                return resource.getURI();
            }else{
                return resource.getId().getLabelString();
            }                
        }else{
            return rdfNode.toString();
        }   
    }

	private Map<String, Object> assembleMap(List<Map<String, String>> results) {
		Map<String, Object> rmap = new HashMap<String,Object>();
        
        //put results in page data
        rmap.put(this.saveToVar, results);  
        //also store the variable name within which results will be returned
        rmap.put("variableName", this.saveToVar);
        //This will be overridden at page level in display model if template specified there
        rmap.put("bodyTemplate", defaultTemplate);
        
        return rmap;        
	}
	
	
	public class ExternalURIInformation {
		

		private String externalURI;
		private String externalServiceURI;
		private String externalServiceName;
		private String externalServiceURL;
		private String propertyURI;
		private String propertyDomainURI;
		private String propertyRangeURI;
		
		public ExternalURIInformation(String externalURI,
				String externalServiceURI, String externalServiceName,
				String externalServiceURL) {
			super();
			this.externalURI = externalURI;
			this.externalServiceURI = externalServiceURI;
			this.externalServiceName = externalServiceName;
			this.externalServiceURL = externalServiceURL;
		}
		
		/**
		 * @return the externalURI
		 */
		public String getExternalURI() {
			return externalURI;
		}

		/**
		 * @return the externalServiceURI
		 */
		public String getExternalServiceURI() {
			return externalServiceURI;
		}

		/**
		 * @return the externalServiceName
		 */
		public String getExternalServiceName() {
			return externalServiceName;
		}

		/**
		 * @return the externalServiceURL
		 */
		public String getExternalServiceURL() {
			return externalServiceURL;
		}
		
		//Property-specific information
		/**
		 * @return the propertyURI
		 */
		public String getPropertyURI() {
			return propertyURI;
		}

		/**
		 * @param propertyURI the propertyURI to set
		 */
		public void setPropertyURI(String propertyURI) {
			this.propertyURI = propertyURI;
		}

		/**
		 * @return the propertyDomainURI
		 */
		public String getPropertyDomainURI() {
			return propertyDomainURI;
		}

		/**
		 * @param propertyDomainURI the propertyDomainURI to set
		 */
		public void setPropertyDomainURI(String propertyDomainURI) {
			this.propertyDomainURI = propertyDomainURI;
		}

		/**
		 * @return the propertyRangeURI
		 */
		public String getPropertyRangeURI() {
			return propertyRangeURI;
		}

		/**
		 * @param propertyRangeURI the propertyRangeURI to set
		 */
		public void setPropertyRangeURI(String propertyRangeURI) {
			this.propertyRangeURI = propertyRangeURI;
		}
		
		
	}
	
}
