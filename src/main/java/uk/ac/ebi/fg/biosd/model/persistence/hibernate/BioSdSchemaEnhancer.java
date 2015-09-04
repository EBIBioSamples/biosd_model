package uk.ac.ebi.fg.biosd.model.persistence.hibernate;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import uk.ac.ebi.fg.persistence.hibernate.schema_enhancer.DbSchemaEnhancer;
import uk.ac.ebi.fg.persistence.hibernate.schema_enhancer.DefaultDbSchemaEnhancer;

/**
 * Adds some indices specific to biosd model, in addition to what {@link DefaultDbSchemaEnhancer} already does 
 * automatically.
 *
 * <dl><dt>date</dt><dd>24 Sep 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BioSdSchemaEnhancer extends DbSchemaEnhancer
{
	public void enhance ( EntityManagerFactory emf )
	{
		super.enhance ( emf );
				
		// More join tables, that cannot easily be achieved from annotations
		Set<String> alreadyDone = new HashSet<String> ();
		
		indexJoinTable ( "msi_contact", "msi_id", "contacts_id", alreadyDone );
		indexJoinTable ( "msi_organization", "msi_id", "organizations_id", alreadyDone );
		indexJoinTable ( "msi_publication", "msi_id", "publications_id", alreadyDone );
		indexJoinTable ( "msi_reference_source", "msi_id", "referencesources_id", alreadyDone );
		indexJoinTable ( "msi_annotation", "owner_id", "annotation_id", alreadyDone );
		
		indexJoinTable ( "bio_smp_grp_annotation", "owner_id", "annotation_id", alreadyDone );
	}
}
