/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.organizational;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
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
@Entity
@Table ( name = "msi" )
@AssociationOverrides ({
  @AssociationOverride ( name = "contacts", 
    joinTable = @JoinTable ( name = "msi_contact", joinColumns = @JoinColumn ( name = "msi_id" ) ) ),
  @AssociationOverride ( name = "organizations", 
  	joinTable = @JoinTable ( name = "msi_organization", joinColumns = @JoinColumn ( name = "msi_id" ) ) ),
  @AssociationOverride ( name = "organizations", 
	  joinTable = @JoinTable ( name = "msi_organization", joinColumns = @JoinColumn ( name = "msi_id" ) ) ),
  @AssociationOverride ( name = "referenceSources", 
  	joinTable = @JoinTable ( name = "msi_ref_source", joinColumns = @JoinColumn ( name = "msi_id" ) ) )
})
public class MSI extends Submission
{
	private Set<DatabaseRefSource> databases = new HashSet<DatabaseRefSource> ();
	private Set<BioSampleGroup> sampleGroups = new HashSet<BioSampleGroup> ();
	private Set<BioSample> samples = new HashSet<BioSample> ();
	
	
	protected MSI () {
		super ();
	}

	public MSI ( String acc ) {
		super ( acc );
	}

	@ManyToMany ( cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable ( name = "msi_database", 
    joinColumns = @JoinColumn ( name = "msi_id" ), inverseJoinColumns = @JoinColumn ( name = "database_id" ) )
	public Set<DatabaseRefSource> getDatabases () {
		return databases;
	}

	public void setDatabases ( Set<DatabaseRefSource> databases ) {
		this.databases = databases;
	}
	
	public boolean addDatabase ( DatabaseRefSource db ) {
		return this.databases.add ( db );
	}

	
	@ManyToMany ( cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable ( name = "msi_sample_group", 
    joinColumns = @JoinColumn ( name = "msi_id" ), inverseJoinColumns = @JoinColumn ( name = "group_id" ) )
	public Set<BioSampleGroup> getSampleGroups () {
		return sampleGroups;
	}

	protected void setSampleGroups ( Set<BioSampleGroup> sampleGroups ) {
		this.sampleGroups = sampleGroups;
	}

	public boolean addSampleGroup ( BioSampleGroup sg )
	{
		if ( !this.sampleGroups.add ( sg ) ) return false;
		sg.addMSI ( this );
		return true;
	}

	public boolean deleteSampleGroup ( BioSampleGroup sg )
	{
		if ( !this.sampleGroups.remove ( sg ) ) return false;
		sg.deleteMSI ( this );
		return true;
	}
	
	
	
	
	@ManyToMany ( cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable ( name = "msi_sample", 
    joinColumns = @JoinColumn ( name = "msi_id" ), inverseJoinColumns = @JoinColumn ( name = "sample_id" ) )
	public Set<BioSample> getSamples () {
		return samples;
	}

	protected void setSamples ( Set<BioSample> samples ) {
		this.samples = samples;
	}

	public boolean addSample ( BioSample smp ) 
	{
		if ( !this.samples.add ( smp ) ) return false;
		smp.addMSI ( this );
		return true;
	}
	
	public boolean deleteSample ( BioSample smp )
	{
		if ( !this.samples.remove ( smp ) ) return false;
		smp.deleteMSI ( this );
		return true;
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
