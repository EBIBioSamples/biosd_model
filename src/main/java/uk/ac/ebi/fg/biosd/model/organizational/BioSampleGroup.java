package uk.ac.ebi.fg.biosd.model.organizational;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.biosd.model.access_control.SecureEntityDelegate;
import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRecordRef;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;

/**
 * A collection of samples, put together for whatever criteria (e.g., coming from the same experiment, prepared for the 
 * same project, other organisational reasons). This class corresponds to the concept with the same name in SampleTAB. 
 *
 * <dl><dt>date</dt><dd>Jul 17, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table( name = "bio_smp_grp" )
@SuppressWarnings ( "rawtypes" )
public class BioSampleGroup extends Accessible
{
	private Set<BioSample> samples = new HashSet<BioSample> ();
	private Collection<ExperimentalPropertyValue> propertyValues = new ArrayList<ExperimentalPropertyValue> ();
	private Set<DatabaseRecordRef> databaseRecordRefs = new HashSet<DatabaseRecordRef> ();
	private Set<MSI> msis = new HashSet<MSI> ();
	private Date updateDate;

	private final SecureEntityDelegate securityDelegate = new SecureEntityDelegate ();
	private boolean isInReferenceLayer;
	
	protected BioSampleGroup () {
		super ();
	}

	public BioSampleGroup ( String acc ) {
		super ( acc );
	}

	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	@JoinTable ( name = "bio_sample_sample_group", 
		joinColumns = @JoinColumn ( name = "group_id" ), inverseJoinColumns = @JoinColumn ( name = "sample_id" ) )
	public Set<BioSample> getSamples ()
	{
		return samples;
	}

	protected void setSamples ( Set<BioSample> samples )
	{
		this.samples = samples;
	}
	
	public boolean addSample ( BioSample smp ) 
	{
		if ( !this.getSamples ().add ( smp ) ) return false;
		smp.addGroup ( this );
		return true;
	}
	
	public boolean deleteSample ( BioSample smp ) 
	{
		if ( !this.getSamples ().remove ( smp ) ) return false;
		smp.deleteGroup ( this );
		return true;
	}
	
	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	@JoinTable ( name = "sg_db_rec_ref", 
    joinColumns = @JoinColumn ( name = "sg_id" ), inverseJoinColumns = @JoinColumn ( name = "db_rec_id" ) )
	public Set<DatabaseRecordRef> getDatabaseRecordRefs () {
		return databaseRecordRefs;
	}

	public void setDatabaseRecordRefs ( Set<DatabaseRecordRef> dbRecRefs ) {
		this.databaseRecordRefs = dbRecRefs;
	}
	
	public boolean addDatabaseRecordRef ( DatabaseRecordRef dbRecRef ) {
		return this.getDatabaseRecordRefs ().add ( dbRecRef );
	}

	/**
	 * @see {@link MSI#getSampleGroups()}.
	 */
	@ManyToMany ( mappedBy = "sampleGroups" )
	public Set<MSI> getMSIs ()
	{
		return msis;
	}

	protected void setMSIs ( Set<MSI> msis )
	{
		this.msis = msis;
	}

	/**
	 * @see {@link MSI#addSampleGroup(BioSampleGroup)}.
	 */
	public boolean addMSI ( MSI msi ) 
	{
		if ( !this.getMSIs ().add ( msi ) ) return false;
		msi.addSampleGroup ( this );
		return true;
	}
	
	/**
	 * @see {@link MSI#deleteSampleGroup(BioSampleGroup)}.
	 */
	public boolean deleteMSI ( MSI msi ) 
	{
		if ( !this.getMSIs ().remove ( msi ) ) return false;
		msi.deleteSampleGroup ( this );
		return true;
	}

		
	
	/**
	 * A sample group can have {@link BioCharacteristicValue characteristic values} attached in certain case, e.g., when
	 * you have a cohort without any description of the single samples in it, in such a case you want to list common properties
	 * by means of this relationship.  
	 *
	 */
	@OneToMany ( targetEntity = ExperimentalPropertyValue.class, cascade = CascadeType.ALL, orphanRemoval = true )
	@JoinTable ( name = "biosample_group_pv", 
		joinColumns = @JoinColumn ( name = "owner_id" ), inverseJoinColumns = @JoinColumn ( name = "pv_id" ) )
	public Collection<ExperimentalPropertyValue> getPropertyValues ()
	{
		return propertyValues;
	}

	/**
	 * @see #getPropertyValues().
	 */
	public void setPropertyValues ( Collection<ExperimentalPropertyValue> propertyValues )
	{
		this.propertyValues = propertyValues;
	}

	/**
	 * @see #getPropertyValues().
	 */
	public boolean addPropertyValue ( ExperimentalPropertyValue pval ) {
		return this.getPropertyValues ().add ( pval );
	}
	

	/** It's symmetric, {@link User#getBioSampleGroups()} will be updated. @see {@link SecureEntityDelegate}. */
	@ManyToMany ( mappedBy = "bioSampleGroups", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	public Set<User> getUsers ()
	{
		return securityDelegate.getUsers ();
	}

	protected void setUsers ( Set<User> users ) {
		securityDelegate.setUsers ( users );
	}
	
	public boolean addUser ( User user )
	{
		return securityDelegate.addUser ( this, user, "addBioSampleGroup" );
	}

	public boolean deleteUser ( User user )
	{
		return securityDelegate.deleteUser ( this, user, "deleteBioSampleGroup" );
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
	@Index ( name = "sg_rel_date" )
	public Date getReleaseDate ()
	{
		return securityDelegate.getReleaseDate ();
	}

	public void setReleaseDate ( Date releaseDate )
	{
		securityDelegate.setReleaseDate ( releaseDate );
	}
	
	@Column ( name = "update_date" )
	@Index ( name = "sg_up_date" )
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

}
