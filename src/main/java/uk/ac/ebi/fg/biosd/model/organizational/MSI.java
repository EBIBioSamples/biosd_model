/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.organizational;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
import uk.ac.ebi.fg.core_model.organizational.Submission;

/**
 * Contains submission meta-data, usually coming from the MSI section of the SampleTAB format. This is treated as 
 * an extension of the general {@link Submission} concept, defined in the core model. 
 *
 * <dl><dt>date</dt><dd>Jul 18, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MSI extends Submission
{
	private Set<DatabaseRefSource> databases = new HashSet<DatabaseRefSource> ();
	
	
	protected MSI () {
		super ();
	}

	public MSI ( String acc ) {
		super ( acc );
	}

	
	public Set<DatabaseRefSource> getDatabases ()
	{
		return databases;
	}

	public void setDatabases ( Set<DatabaseRefSource> databases )
	{
		this.databases = databases;
	}
	
	public boolean addDatabase ( DatabaseRefSource db ) {
		return this.databases.add ( db );
	}
	
	@Override
	public String toString ()
	{
		return String.format ( 
			"%s { id: %s, acc: '%s', title: '%s', description: '%s', version: '%s', sub. date: '%s', rel. date: '%s', " +
			"update date: '%s', format ver.: '%s', contacts:\n  %s,\n organizations:\n  %s,\n databases:\n  %s, " +
			"\n ref sources:\n  %s}",
			this.getClass ().getSimpleName (), this.getId (), this.getAcc (), this.getTitle (), 
			StringUtils.abbreviate ( this.getDescription (), 20 ), this.getVersion (), this.getSubmissionDate (),
			this.getReleaseDate (), this.getUpdateDate (), this.getFormatVersion (), this.getContacts (), 
			this.getOrganizations (), this.getDatabases (), this.getReferenceSources ()
		);
	}

}
