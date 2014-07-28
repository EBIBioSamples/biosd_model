package uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.IdentifiableDAO;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
/**
* DAO to manage {@link ExperimentalPropertyValue}.
* 
*
* <dl><dt>date</dt><dd>Jul 25, 2014</dd></dl>
* @author Adam Faulconbridge
*
*/
public class ExpPropValDAO extends IdentifiableDAO<ExperimentalPropertyValue> {
  //TODO: parameterize this type correctly without invalidating constructor

    public ExpPropValDAO(EntityManager entityManager) {
        super(ExperimentalPropertyValue.class, entityManager);
    }
        
    /**
     * Return @ExperimentalPropertyValue that do not have an ontology entry associated 
     * with them
     * 
     * @return
     */
    public List<ExperimentalPropertyValue<ExperimentalPropertyType>> getUnmapped(long startId, int chunkSize) {
        //TODO write this properly
        String hql = "SELECT pv" +
        	" FROM "+ExperimentalPropertyValue.class.getCanonicalName()+" As pv" +
        	" WHERE 1=1 " +
          	    " AND pv.id >= :startId" +
                " AND size(pv.ontologyTerms) = 0 " +
			" ORDER BY pv.id";
        Query q = this.getEntityManager().createQuery(hql);
        q.setParameter("startId", startId);
        q.setMaxResults(chunkSize);
        return q.getResultList();        
    }

}
