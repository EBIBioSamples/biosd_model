package uk.ac.ebi.fg.biosd.model.organizational;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.biosd.model.access_control.SecureEntityDelegate;
import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRecordRef;
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
@NamedQueries ({
	@NamedQuery ( name = "getSampleRefs", query = 
			"SELECT smp FROM BioSample smp\n"
		+ "WHERE smp.acc IN ( SELECT smpRef FROM MSI msi JOIN msi.sampleRefs smpRef WHERE msi.id = :msiId )" 
	),
	@NamedQuery ( name = "getSampleRefsFromList", query = 
		"SELECT smp FROM BioSample smp\n"
		+ "WHERE smp.acc IN ( :smpAccs )"
	),
	@NamedQuery ( name = "getSampleGroupRefs", query = 
			"SELECT sg FROM BioSampleGroup sg\n"
		+ "WHERE sg.acc IN ( SELECT sgRef FROM MSI msi JOIN msi.sampleGroupRefs sgRef WHERE msi.id = :msiId )" 
	),
	@NamedQuery ( name = "getSampleGroupRefsFromList", query = 
			"SELECT sg FROM BioSampleGroup sg\n"
		+ "WHERE sg.acc IN ( :sgAccs )" 
	)
})
public class MSI extends Submission
{
	private Set<DatabaseRecordRef> databaseRecordRefs = new HashSet<DatabaseRecordRef> ();
	private Set<BioSampleGroup> sampleGroups = new HashSet<BioSampleGroup> ();
	private Set<BioSample> samples = new HashSet<BioSample> ();
	private Set<String> sampleGroupRefs = new HashSet<String> ();
	private Set<String> sampleRefs = new HashSet<String> ();
	private final SecureEntityDelegate securityDelegate = new SecureEntityDelegate ();
	
	protected MSI () {
		super ();
	}

	public MSI ( String acc ) {
		super ( acc );
	}

	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	@JoinTable ( name = "msi_db_rec_ref", 
    joinColumns = @JoinColumn ( name = "msi_id" ), inverseJoinColumns = @JoinColumn ( name = "db_rec_id" ) )
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
	 * This are the sample groups that were defined with the submission and belongs to the submission.
	 * Usually there is only one submission per each sample group having this role 
	 * (we're not making this relation 1-n for legacy reasons). 
	 * 
	 * @see {@link #getSampleGroupRefs()}
	 */
	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	@JoinTable ( name = "msi_sample_group", 
    joinColumns = @JoinColumn ( name = "msi_id" ), inverseJoinColumns = @JoinColumn ( name = "group_id" ) )
	public Set<BioSampleGroup> getSampleGroups () {
		return sampleGroups;
	}

	protected void setSampleGroups ( Set<BioSampleGroup> sampleGroups ) {
		this.sampleGroups = sampleGroups;
	}

	/**
	 * Use these methods to manipulate owned sample groups, it coordinates the symmetric side, by means
	 * of {@link BioSampleGroup#addMSI(MSI)}
	 */
	public boolean addSampleGroup ( BioSampleGroup sg )
	{
		if ( !this.getSampleGroups ().add ( sg ) ) return false;
		sg.addMSI ( this );
		return true;
	}

	/**
	 * Use these methods to manipulate owned sample groups, it coordinates the symmetric side, by means
	 * of {@link BioSampleGroup#deleteMSI(MSI)}
	 */
	public boolean deleteSampleGroup ( BioSampleGroup sg )
	{
		if ( !this.getSampleGroups ().remove ( sg ) ) return false;
		sg.deleteMSI ( this );
		return true;
	}
	

	/**
	 * These are sample groups that are referred by the submission, for any reason. This is different than 
	 * {@link #getSampleGroups()} and, conceptually, it can be a n-m relation.
	 * 
	 */
	@ElementCollection
	@CollectionTable ( 
		name = "msi_sample_group_ref", joinColumns = @JoinColumn ( name = "msi_id" ), 
		indexes = @Index ( name = "sg_ref", columnList = "sg_acc" )
	)
	@Column ( name = "sg_acc" )
	public Set<String> getSampleGroupRefs () {
		return sampleGroupRefs;
	}

	protected void setSampleGroupRefs ( Set<String> sampleGroupAccs ) {
		this.sampleGroupRefs = sampleGroupAccs;
	}

	public boolean addSampleGroupRef ( String sgAcc )
	{
		if ( !this.getSampleGroupRefs ().add ( sgAcc ) ) return false;
		return true;
	}

	public boolean deleteSampleGroupRef ( String sgAcc )
	{
		if ( !this.getSampleGroupRefs ().remove ( sgAcc ) ) return false;
		return true;
	}
	
	

	
	/**
	 * This are the samples that were defined with the submission and belongs to the submission.
	 * Usually there is only one submission per each sample having this role (we're not making this relation 1-n for
	 * legacy reasons). 
	 * 
	 * @see {@link #getSampleRefs()}
	 */	
	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.LAZY )
	@JoinTable ( name = "msi_sample", 
    joinColumns = @JoinColumn ( name = "msi_id" ), inverseJoinColumns = @JoinColumn ( name = "sample_id" ) )
	public Set<BioSample> getSamples () {
		return samples;
	}

	protected void setSamples ( Set<BioSample> samples ) {
		this.samples = samples;
	}

	/**
	 * Use these methods to manipulate owned samples , it coordinates the symmetric side, by means
	 * of {@link BioSample#addMSI(MSI)}
	 */
	public boolean addSample ( BioSample smp ) 
	{
		if ( !this.getSamples ().add ( smp ) ) return false;
		smp.addMSI ( this );
		return true;
	}

	/**
	 * Use these methods to manipulate owned samples , it coordinates the symmetric side, by means
	 * of {@link BioSample#deleteMSI(MSI)}
	 */
	public boolean deleteSample ( BioSample smp )
	{
		if ( !this.getSamples ().remove ( smp ) ) return false;
		smp.deleteMSI ( this );
		return true;
	}
	

	/**
	 * These are sample that are referred by the submission, for any reason. 
	 */
	@ElementCollection
	@CollectionTable ( name = "msi_sample_ref", 
		joinColumns = @JoinColumn ( name = "msi_id" ),
		indexes = @Index ( name = "smp_ref", columnList = "smp_acc" )
	)
	@Column ( name = "smp_acc" )
	public Set<String> getSampleRefs () {
		return sampleRefs;
	}

	protected void setSampleRefs ( Set<String> sampleAccs ) {
		this.sampleRefs = sampleAccs;
	}

	public boolean addSampleRef ( String sampleAcc ) 
	{
		if ( !this.getSampleRefs ().add ( sampleAcc ) ) return false;
		return true;
	}

	public boolean deleteSampleRef ( String sampleAcc )
	{
		if ( !this.getSampleRefs ().remove ( sampleAcc ) ) return false;
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
	@Override
	@Transient // Cause the damn Hibernate doesn't seem to get it was already defined by the parent. 
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
			"update date: '%s', format ver.: '%s', publicFlag: %s, contacts:\n  %s,\n organizations:\n  %s,\n databaseRecordRefs:\n  %s, " +
			"\n ref sources:\n  %s, users:\n %s}",
			this.getClass ().getSimpleName (), this.getId (), this.getAcc (), this.getTitle (), 
			StringUtils.abbreviate ( this.getDescription (), 20 ), this.getVersion (), this.getSubmissionDate (),
			this.getReleaseDate (), this.getUpdateDate (), this.getFormatVersion (), this.getPublicFlag (), this.getContacts (), 
			this.getOrganizations (), this.getDatabaseRecordRefs (), this.getReferenceSources (), this.getUsers ()
		);
	}
}
