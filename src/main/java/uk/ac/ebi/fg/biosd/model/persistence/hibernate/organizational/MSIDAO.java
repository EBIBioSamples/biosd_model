package uk.ac.ebi.fg.biosd.model.persistence.hibernate.organizational;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.Validate;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * Some specific functionality related to {@link MSI BioSD submissions}
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

	/**
	 * Gets the {@link BioSampleGroup}s referred by {@link MSI#getSampleGroupRefs()}. msi must be non null, 
	 * this method doesn't return null.
	 */
	public List<BioSampleGroup> getSampleGroupRefs ( MSI msi )
	{
		Validate.notNull ( msi, "Internal error: cannot run MSIDAO.getSampleGroupRefs() over a null MSI" );
		
		return this.getEntityManager ().createNamedQuery ( "getSampleGroupRefs", BioSampleGroup.class )
			.setParameter ( "msiId", msi.getId () )
			.getResultList ();
	}

	/**
	 * Returns the {@link BioSampleGroup}s pointed by sgAccs, if this is non empty (returns an empty list otherwise). 
	 * This method doesn't return null.
	 */
	public List<BioSampleGroup> getSampleGroupRefsFromList ( Collection<String> sgAccs )
	{
		if ( sgAccs == null || sgAccs.isEmpty () ) return Collections.emptyList ();
		
		return this.getEntityManager ().createNamedQuery ( "getSampleGroupRefsFromList", BioSampleGroup.class )
			.setParameter ( "sgAccs", sgAccs )
			.getResultList ();
	}
	
	/**
	 * Gets the {@link BioSample}s referred by {@link MSI#getSampleRefs()}. msi must be non null, 
	 * this method doesn't return null.
	 */
	public List<BioSample> getSampleRefs ( MSI msi )
	{
		Validate.notNull ( msi, "Internal error: cannot run MSIDAO.getSampleRefs() over a null MSI" );
		
		return this.getEntityManager ().createNamedQuery ( "getSampleRefs", BioSample.class )
			.setParameter ( "msiId", msi.getId () )
			.getResultList ();
	}

	/**
	 * Returns the {@link BioSample}s pointed by smpAccs, if this is non empty (returns an empty list otherwise). 
	 * This method doesn't return null.
	 */
	public List<BioSample> getSampleRefsFromList ( Collection<String> smpAccs )
	{
		if ( smpAccs == null || smpAccs.isEmpty () ) return Collections.emptyList ();
		
		return this.getEntityManager ().createNamedQuery ( "getSampleRefsFromList", BioSample.class )
			.setParameter ( "smpAccs", smpAccs )
			.getResultList ();
	}

}
