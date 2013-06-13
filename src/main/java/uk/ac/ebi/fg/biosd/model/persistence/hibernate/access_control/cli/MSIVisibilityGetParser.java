package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * Reports visibility information about the BioSample submissions and possibly the objects it contains.
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class MSIVisibilityGetParser extends CLIParser
{
	private AccessControlManager accMgr;
	private AccessibleDAO<MSI> msiDao; 

	public MSIVisibilityGetParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * Splits the input into spaced chunks (using \s+), then uses {@link VisibilityParser#SAMPLE_GROUP_VISIBILITY_GET_SPEC_RE}
	 * to match accession specifications. Prints out a list of submissions and their permissions.
	 * @return that same list.
	 */
	public List<MSI> run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null submission visibility specification)" );
		
		List<MSI> result = new ArrayList<MSI> ();
		
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = VisibilityParser.SAMPLE_GROUP_VISIBILITY_GET_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			
			String acc = specBits [ 1 ]; 
			boolean isCascaded = "++".equals ( specBits [ 2 ] );
				
			MSI msi = msiDao.findAndFail ( acc );
			
			result.add ( msi );
				
			out.format ( 
				"\n\n Submission: %s, submission date: %s, release date: %s, update date: %s, public flag: %s, is-public: %s",
				msi.getAcc (), msi.getSubmissionDate (), msi.getReleaseDate (), msi.getUpdateDate (), msi.getPublicFlag (), 
				msi.isPublic ()
			);
			
			out.print ( ", users: " );
			for ( User user: msi.getUsers () )
				out.format ( "%s ", user.getEmail () );
			out.println ();

			if ( !isCascaded ) continue;

			out.println ( "\n  Sample Groups for " + msi.getAcc () + ":\n" );
			for ( BioSampleGroup sg: msi.getSampleGroups () )
			{
				reportSampleGroup ( sg, true, 2 );
				out.println ();
			}
			
			out.println ( "\n  Samples for " + msi.getAcc () + ":" );
			for ( BioSample smp: msi.getSamples () ) 
				reportSample ( smp, 3 );
		}
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

	
	static void reportSampleGroup ( BioSampleGroup sg, boolean isCascaded, int indentLevel )
	{
		out.format ( "%sgroup acc: %s, release date: %s, public flag: %s, is-public: %s",
			StringUtils.repeat ( "  ", indentLevel ), sg.getAcc (), sg.getReleaseDate (), sg.getPublicFlag (), sg.isPublic ()
		);
		out.print ( ", users: " );
		for ( User user: sg.getUsers () ) out.format ( "%s ", user.getEmail () );
		out.println ();

		if ( !isCascaded ) return;
		
		indentLevel++;
		out.format ( "\n%sSamples for %s:\n", StringUtils.repeat ( "  ", indentLevel ), sg.getAcc () );
		for ( BioSample smp: sg.getSamples () ) 
			reportSample ( smp, indentLevel );
	}
	
	static void reportSample ( BioSample smp, int indentLevel )
	{
		out.format ( "%sacc: %s, release date: %s, public flag: %s, is-public: %s", 
			StringUtils.repeat ( "  ", indentLevel ), smp.getAcc (), smp.getReleaseDate (), smp.getPublicFlag (), 
			smp.isPublic ()
		);
		out.print ( ", users: " );
		for ( User user: smp.getUsers () ) out.format ( "%s ", user.getEmail () );
		out.println ();
	}
}
