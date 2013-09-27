package uk.ac.ebi.fg.biosd.model.expgraph;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.biosd.model.access_control.SecureEntityDelegate;
import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
import uk.ac.ebi.fg.core_model.expgraph.BioMaterial;
import uk.ac.ebi.fg.core_model.expgraph.Node;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.utils.orm.Many2ManyUtils;

/**
 * A biological sample, corresponding to the entity identified by 'Sample Name' in BioSD. This is essentially a specific 
 * type of {@link BioMaterial}, where you're supposed to use the 'direct-derivation model' (see {@link Node}).
 *
 * TODO: ensure many-many relationships are not changed from the outside, use getters in add/remove methods, comment them.
 * TODO: User {@link Many2ManyUtils} for all relations.
 * 
 * <dl><dt>date</dt><dd>Jul 17, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@DiscriminatorValue ( "bio_sample" )
@SuppressWarnings ( "rawtypes" )
public class BioSample extends BioMaterial<ExperimentalPropertyValue>
{
	private Set<BioSampleGroup> groups = new HashSet<BioSampleGroup> (); 
	private Set<DatabaseRefSource> databases = new HashSet<DatabaseRefSource> ();
	private Set<MSI> msis = new HashSet<MSI> ();

	private Date updateDate;

	private boolean isInReferenceLayer;
	
	
	/** This entities have an owner and a visibility status, @see {@link SecureEntityDelegate} */
	private final SecureEntityDelegate securityDelegate = new SecureEntityDelegate ();
	
	public BioSample () {
		super ();
	}

	public BioSample ( String acc ) {
		super ( acc );
	}
	

	@ManyToMany ( mappedBy = "samples" )
	public Set<BioSampleGroup> getGroups ()
	{
		return groups;
	}

	protected void setGroups ( Set<BioSampleGroup> groups )
	{
		this.groups = groups;
	}

	public boolean addGroup ( BioSampleGroup sg ) 
	{
		if ( !this.getGroups ().add ( sg ) ) return false;
		sg.addSample ( this );
		return true;
	}

	public boolean deleteGroup ( BioSampleGroup sg ) 
	{
		if ( !this.getGroups ().remove ( sg ) ) return false;
		sg.deleteSample ( this );
		return true;
	}
	
	
	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	@JoinTable ( name = "sample_database", 
    joinColumns = @JoinColumn ( name = "sample_id" ), inverseJoinColumns = @JoinColumn ( name = "database_id" ) )
	public Set<DatabaseRefSource> getDatabases () {
		return databases;
	}

	public void setDatabases ( Set<DatabaseRefSource> databases ) {
		this.databases = databases;
	}
	
	public boolean addDatabase ( DatabaseRefSource db ) {
		return this.getDatabases ().add ( db );
	}
	
	
	@ManyToMany ( mappedBy = "samples" )
	public Set<MSI> getMSIs () {
		return msis;
	}

	protected void setMSIs ( Set<MSI> msis )
	{
		this.msis = msis;
	}

	public boolean addMSI ( MSI msi ) 
	{
		if ( !this.getMSIs ().add ( msi ) ) return false;
		msi.addSample ( this );
		return true;
	}
	
	public boolean deleteMSI ( MSI msi ) 
	{
		if ( !this.getMSIs ().remove ( msi ) ) return false;
		msi.deleteSample ( this );
		return true;
	}
	
	
	/** @see SecureEntityDelegate */
	@ManyToMany ( mappedBy = "bioSamples", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	public Set<User> getUsers ()
	{
		return securityDelegate.getUsers ();
	}

	protected void setUsers ( Set<User> users ) {
		securityDelegate.setUsers ( users );
	}

	/** It's symmetric, {@link User#getBioSamples()} will be updated. @see {@link SecureEntityDelegate}. */
	public boolean addUser ( User user )
	{
		return securityDelegate.addUser ( this, user, "addBioSample" );
	}

	/** It's symmetric, {@link User#getBioSamples()} will be updated. @see {@link SecureEntityDelegate}. */
	public boolean deleteUser ( User user )
	{
		return securityDelegate.deleteUser ( this, user, "deleteBioSample" );
	}

	
	/**
	 * Reference Layer entities are pre-loaded into BioSD to support future data generation and linking from other
	 * repositories.
	 */
	@Column ( name = "is_ref_layer" )
	public boolean isInReferenceLayer ()
	{
		return isInReferenceLayer;
	}

	public void setInReferenceLayer ( boolean isInReferenceLayer )
	{
		this.isInReferenceLayer = isInReferenceLayer;
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
	@Column ( name = "release_date", nullable = true )
	@Index ( name = "sample_rel_date" )
	public Date getReleaseDate ()
	{
		return securityDelegate.getReleaseDate ();
	}

	public void setReleaseDate ( Date releaseDate )
	{
		securityDelegate.setReleaseDate ( releaseDate );
	}
	
	@Column ( name = "update_date" )
	@Index ( name = "sample_up_date" )
	public Date getUpdateDate ()
	{
		return updateDate;
	}

	public void setUpdateDate ( Date updateDate )
	{
		this.updateDate = updateDate;
	}

	@Transient
	public boolean isPublic ()
	{
		return securityDelegate.isPublic ();
	}
	
}
