/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.organizational;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.access_control.SecureEntityDelegate;
import uk.ac.ebi.fg.biosd.model.access_control.User;
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
	private final SecureEntityDelegate securityDelegate = new SecureEntityDelegate ();
	
	protected MSI () {
		super ();
	}

	public MSI ( String acc ) {
		super ( acc );
	}

	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	@JoinTable ( name = "msi_database", 
    joinColumns = @JoinColumn ( name = "msi_id" ), inverseJoinColumns = @JoinColumn ( name = "database_id" ) )
	public Set<DatabaseRefSource> getDatabases () {
		return databases;
	}

	public void setDatabases ( Set<DatabaseRefSource> databases ) {
		this.databases = databases;
	}
	
	public boolean addDatabase ( DatabaseRefSource db ) {
		return this.getDatabases ().add ( db );
	}

	
	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
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
		if ( !this.getSampleGroups ().add ( sg ) ) return false;
		sg.addMSI ( this );
		return true;
	}

	public boolean deleteSampleGroup ( BioSampleGroup sg )
	{
		if ( !this.getSampleGroups ().remove ( sg ) ) return false;
		sg.deleteMSI ( this );
		return true;
	}
	
	
	
	
	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
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
		if ( !this.getSamples ().add ( smp ) ) return false;
		smp.addMSI ( this );
		return true;
	}
	
	public boolean deleteSample ( BioSample smp )
	{
		if ( !this.getSamples ().remove ( smp ) ) return false;
		smp.deleteMSI ( this );
		return true;
	}
	
	
	
	/** @see SecureEntityDelegate */
	@ManyToMany ( mappedBy = "MSIs", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	public Set<User> getUsers ()
	{
		return securityDelegate.getUsers ();
	}

	protected void setUsers ( Set<User> users ) {
		securityDelegate.setUsers ( users );
	}

	/** It's symmetric, {@link User#getMSIs()} will be updated. @see {@link SecureEntityDelegate}. */
	public boolean addUser ( User user )
	{
		return securityDelegate.addUser ( this, user, "addMSI" );
	}

	/** It's symmetric, {@link User#getMSIs()} will be updated. @see {@link SecureEntityDelegate}. */
	public boolean deleteUser ( User user )
	{
		return securityDelegate.deleteUser ( this, user, "deleteMSI" );
	}

	/** @see SecureEntityDelegate. */
	@Column ( name = "public_flag", nullable = true )
	public Boolean getPublicFlag ()
	{
		return securityDelegate.getPublicFlag ();
	}

	public void setPublicFlag ( Boolean publicFlag )
	{
		securityDelegate.setPublicFlag ( publicFlag );
	}

	/** @see SecureEntityDelegate. */
	public Date getReleaseDate ()
	{
		return securityDelegate.getReleaseDate ();
	}

	public void setReleaseDate ( Date releaseDate )
	{
		securityDelegate.setReleaseDate ( releaseDate );
	}
	
	@Transient
	public boolean isPublic ()
	{
		return securityDelegate.isPublic ();
	}

	
	@Override
	public String toString ()
	{
		return String.format ( 
			"%s { id: %s, acc: '%s', title: '%s', description: '%s', version: '%s', sub. date: '%s', rel. date: '%s', " +
			"update date: '%s', format ver.: '%s', publicFlag: %s, contacts:\n  %s,\n organizations:\n  %s,\n databases:\n  %s, " +
			"\n ref sources:\n  %s, users:\n %s}",
			this.getClass ().getSimpleName (), this.getId (), this.getAcc (), this.getTitle (), 
			StringUtils.abbreviate ( this.getDescription (), 20 ), this.getVersion (), this.getSubmissionDate (),
			this.getReleaseDate (), this.getUpdateDate (), this.getFormatVersion (), this.getPublicFlag (), this.getContacts (), 
			this.getOrganizations (), this.getDatabases (), this.getReferenceSources (), this.getUsers ()
		);
	}
}
