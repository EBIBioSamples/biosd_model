package uk.ac.ebi.fg.biosd.model.xref;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * A reference about a biological repository, such as ArrayExpress or dbGap. This corresponds to the Database element
 * in the SampleTAB format. 
 *
 * <dl><dt>date</dt><dd>Jul 17, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table ( name = "db_ref_src", uniqueConstraints = @UniqueConstraint ( columnNames = { "acc", "version" } ) )
public class DatabaseRefSource extends ReferenceSource
{
	protected DatabaseRefSource () {
		super ();
	}

	public DatabaseRefSource ( String acc, String version ) {
		super ( acc, version );
	}
	
}
