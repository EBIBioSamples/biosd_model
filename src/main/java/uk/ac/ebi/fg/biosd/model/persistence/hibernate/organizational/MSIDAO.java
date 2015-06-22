package uk.ac.ebi.fg.biosd.model.persistence.hibernate.organizational;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jun 2015</dd>
 *
 */
public class MSIDAO extends AccessibleDAO<MSI>
{
	public MSIDAO ( EntityManager entityManager ) {
		super ( MSI.class, entityManager );
	}

	public List<BioSampleGroup> getSampleGroupRefs ( MSI msi )
	{
		Validate.notNull ( msi, "Internal error: cannot run MSIDAO.getSampleGroupRefs() over a null MSI" );
		
		return this.getEntityManager ().createNamedQuery ( "getSampleGroupRefs", BioSampleGroup.class )
			.setParameter ( "msiId", msi.getId () )
			.getResultList ();
	}

	
	public List<BioSampleGroup> getSampleGroupRefsFromList ( Collection<String> sgAccs )
	{
		if ( sgAccs == null || sgAccs.isEmpty () ) return Collections.emptyList ();
		
		return this.getEntityManager ().createNamedQuery ( "getSampleGroupRefsFromList", BioSampleGroup.class )
			.setParameter ( "sgAccs", sgAccs )
			.getResultList ();
	}
	

	public List<BioSample> getSampleRefs ( MSI msi )
	{
		Validate.notNull ( msi, "Internal error: cannot run MSIDAO.getSampleRefs() over a null MSI" );
		
		return this.getEntityManager ().createNamedQuery ( "getSampleRefs", BioSample.class )
			.setParameter ( "msiId", msi.getId () )
			.getResultList ();
	}


	public List<BioSample> getSampleRefsFromList ( Collection<String> smpAccs )
	{
		if ( smpAccs == null || smpAccs.isEmpty () ) return Collections.emptyList ();
		
		return this.getEntityManager ().createNamedQuery ( "getSampleRefsFromList", BioSample.class )
			.setParameter ( "smpAccs", smpAccs )
			.getResultList ();
	}

}
