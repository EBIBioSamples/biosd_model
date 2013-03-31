package uk.ac.ebi.fg.biosd.model.access_control;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.DatatypeConverter;

import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.core_model.resources.Const;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;
import uk.ac.ebi.utils.orm.Many2ManyUtils;

import com.google.common.collect.Sets;

/**
 * This is used to manage BioSD user accounts, access to BioSD and visibility of samples and sample groups. 
 *
 * <dl><dt>date</dt><dd>Mar 1, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table( name = "acc_ctrl_user" )
@AttributeOverride ( name = "acc", column = @Column( unique = true, nullable = false, length = Const.COL_LENGTH_M ) )
public class User extends Accessible
{
	private String name;
	private String surname;
	private String passwordHash;
	private String notes;
	
	private Set<BioSampleGroup> bioSampleGroups = new HashSet<BioSampleGroup> ();
	private Set<BioSample> bioSamples = new HashSet<BioSample> ();
	
	
	private static MessageDigest messageDigest = null;
	
	protected User () {
		super ();
	}

	public User ( String email )
	{
		super ( email );
	}
	
	public User ( String email, String name, String surname, String passwordHash, String notes )
	{
		this ( email );
		this.name = name;
		this.surname = surname;
		this.passwordHash = passwordHash;
		this.notes = notes;
	}

	
	@Index ( name = "user_n" )
	public String getName ()
	{
		return name;
	}

	public void setName ( String name )
	{
		this.name = name;
	}

	@Index ( name = "user_s" )
	public String getSurname ()
	{
		return surname;
	}

	public void setSurname ( String surname )
	{
		this.surname = surname;
	}

	@Column ( nullable = false, unique = true )
	public String getEmail ()
	{
		return this.getAcc ();
	}

	protected void setEmail ( String email )
	{
		this.setAcc ( email );
	}

	/**
	 * The hashed password. We don't want to store clear password. Use the facility {@link #hashPassword(String)} to 
	 * convert the clear value coming from the user. This same method will be used by TODO:DAO to perform logins.    
	 */
	@Column ( columnDefinition = "char(26)", nullable = false )
	public String getHashPassword ()
	{
		return passwordHash;
	}

	public void setHashPassword ( String hashPassword )
	{
		this.passwordHash = hashPassword;
	}

	@Lob
	public String getNotes ()
	{
		return notes;
	}

	public void setNotes ( String notes )
	{
		this.notes = notes;
	}

	// TODO: constraint that disallows to delete a user still owning something
	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	@JoinTable ( name = "user_sample_group", 
		joinColumns = @JoinColumn ( name = "user_id" ), inverseJoinColumns = @JoinColumn ( name = "sg_id" ) )
	public Set<BioSampleGroup> getBioSampleGroups ()
	{
		return bioSampleGroups;
	}

	protected void setBioSampleGroups ( Set<BioSampleGroup> bioSampleGroups ) {
		this.bioSampleGroups = bioSampleGroups;
	}
	
	/** It's symmetric, {@link BioSampleGroup#getUsers()} will be updated. @see {@link SecureEntityDelegate}. */
	public boolean addBioSampleGroup ( BioSampleGroup sg ) {
		return Many2ManyUtils.addMany2Many ( this, sg, "addUser", this.getBioSampleGroups () );
	}
	
	/** It's symmetric, {@link BioSampleGroup#getUsers()} will be updated. @see {@link SecureEntityDelegate}. */
	public boolean deleteBioSampleGroup ( BioSampleGroup sg ) {
		return Many2ManyUtils.deleteMany2Many ( this, sg, "deleteUser", this.getBioSampleGroups () );
	}


	@ManyToMany ( cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	@JoinTable ( name = "user_sample", 
		joinColumns = @JoinColumn ( name = "user_id" ), inverseJoinColumns = @JoinColumn ( name = "sample_id" ) )
	public Set<BioSample> getBioSamples ()
	{
		return bioSamples;
	}

	protected void setBioSamples ( Set<BioSample> bioSamples )
	{
		this.bioSamples = bioSamples;
	}
	
	/** It's symmetric, {@link BioSample#getUsers()} will be updated. @see {@link SecureEntityDelegate}. */
	public boolean addBioSample ( BioSample smp )  {
		return Many2ManyUtils.addMany2Many ( this, smp, "addUser", this.getBioSamples () );
	}
	
	/** It's symmetric, {@link BioSample#getUsers()} will be updated. @see {@link SecureEntityDelegate}. */
	public boolean deleteBioSample ( BioSample smp ) {
		return Many2ManyUtils.deleteMany2Many ( this, smp, "deleteUser", this.getBioSamples () );
	}

	/**
	 * Provides an immutable view (not a copy) over all the objects that a user might own, i.e., {@link BioSample} and
	 * {@link BioSampleGroup}. Uses {@link Sets#union(Set, Set)}.
	 */
	@Transient
	public Set<Accessible> getOwnedEntities ()
	{
		return Sets.union ( (Set<? extends Accessible>) this.getBioSampleGroups (), (Set<? extends Accessible>) this.getBioSamples () );
	}


	/**
	 * Encrypts a password using SHA1 and representing the encrypted code using BASE64. @see {@link #hashPassword(String)}.
	 */
	public static String hashPassword ( String clearPassword )
	{
		if ( messageDigest == null )
		{
			try {
				messageDigest = MessageDigest.getInstance ( "SHA1" );
			} 
			catch ( NoSuchAlgorithmException ex ) {
				throw new RuntimeException ( "Internal error, cannot get the SHA1 digester from the JVM", ex );
			}
		}
		
		// With 20 bytes as input, the BASE64 encoding is always a 27 character string, with the last character always equals
		// a padding '=', so we don't need the latter in this context  
		return 
			DatatypeConverter.printBase64Binary ( messageDigest.digest ( clearPassword.getBytes () ) ).substring (0, 26);
	}
	
	/** Uses the email as identity field */
	public boolean equals ( Object o )
	{
		if ( o == null ) return false;
		if ( this == o ) return true;
		if ( this.getClass () != o.getClass () ) return false;

		User that = (User) o;
		return this.getEmail ().equals ( that.getEmail () );
	}

	/** Uses the email as identity field */
	public int hashCode ()
	{
		return this.getEmail ().hashCode ();
	}
}
