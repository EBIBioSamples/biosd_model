package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class CopyOfMSIVisibilityGetParser extends CLIParser
{
	private AccessControlManager accMgr;
	private AccessibleDAO<MSI> msiDao; 

	public CopyOfMSIVisibilityGetParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	public List<MSI> run ( String cmd, boolean isSetOrGet )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null submission visibility specification)" );
		
		List<MSI> result = new ArrayList<MSI> ();
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = SAMPLE_GROUP_VISIBILITY_SET_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			
			String acc = specBits [ 2 ]; 
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? new Boolean ( true ) 
				: "-".equals ( specBits [ 1 ] ) ? new Boolean ( false ) 
				: null; // last case is --
			boolean isCascaded = "++".equals ( specBits [ 3 ] );
				
			MSI msi = isSetOrGet
				? accMgr.setMSIVisibility ( acc, publicFlag, isCascaded )
				: msiDao.find ( acc );
			
			result.add ( msi );
				
			out.format ( 
				"\n\n Submission: %s, submission date: %s, release date: %s, update date: %s, public flag: %s, is-public: %s\n",
				msi.getAcc (), msi.getSubmissionDate (), msi.getReleaseDate (), msi.getUpdateDate (), msi.getPublicFlag (), 
				msi.isPublic ()
			);
			if ( isCascaded ) continue;
			
			out.print ( "\n  Submission's users: " );
			for ( User user: msi.getUsers () )
				out.format ( "%s ", user.getEmail () );
			
			out.println ( "\n  Sample Groups for " + msi.getAcc () + ":" );
			for ( BioSampleGroup sg: msi.getSampleGroups () )
			{
				out.format ( "    acc: %s, release date: %s, public flag: %s, is-public: %s",
					sg.getAcc (), sg.getReleaseDate (), sg.getPublicFlag (), sg.isPublic ()
				);
				out.print ( ", users: " );
				for ( User user: sg.getUsers () ) out.format ( "%s ", user.getEmail () );
				out.println ();

				out.println ( "\n      Samples for " + sg.getAcc () + ":" );
				for ( BioSample smp: sg.getSamples () ) 
				{
					out.format ( "      acc: %s, release date: %s, public flag: %s, is-public: %s",
						smp.getAcc (), smp.getReleaseDate (), smp.getPublicFlag (), smp.isPublic ()
					);
					out.print ( ", users: " );
					for ( User user: smp.getUsers () ) out.format ( "%s ", user.getEmail () );
					out.println ();
				}
			}
			
			out.println ( "\n      Samples for " + msi.getAcc () + ":" );
			for ( BioSample smp: msi.getSamples () ) 
			{
				out.format ( "      acc: %s, release date: %s, public flag: %s, is-public: %s",
					smp.getAcc (), smp.getReleaseDate (), smp.getPublicFlag (), smp.isPublic ()
				);
				out.print ( ", users: " );
				for ( User user: smp.getUsers () ) out.format ( "%s ", user.getEmail () );
				out.println ();
			}
			
		}
		ts.commit ();
		return result;
	}

	
	@Override
	public EntityManager getEntityManager ()
	{
		return accMgr.getEntityManager ();
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		this.accMgr = new AccessControlManager ( entityManager );
		this.msiDao = new AccessibleDAO<MSI> ( MSI.class, entityManager );
	}

}
