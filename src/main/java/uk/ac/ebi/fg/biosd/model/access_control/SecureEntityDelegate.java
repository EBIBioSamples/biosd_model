package uk.ac.ebi.fg.biosd.model.access_control;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.utils.orm.Many2ManyUtils;

/**
 * <p>This is a delegate used inside objects like {@link BioSample} and {@link BioSampleGroup}. We put a few fields in here
 * and corresponding accessors, in order to factorise implementations. Delegating classes have to define an instance
 * of this class and use it in accessors wrapping this class's accessors (i.e., delegator pattern). Additionally, 
 * you have to work out JPA/Hibernate annotations, as specified in the comments below.</p>
 * 
 * <dl><dt>date</dt><dd>Mar 26, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SecureEntityDelegate
{
	public enum PublicStatus {
		PRIVATE(false), PUBLIC(true), UNKNOWN(false);

		private boolean isPublic;

		PublicStatus(boolean value) {
			this.isPublic = value;
		}

		public boolean getValue() {
			return this.isPublic;
		}

	};

	private Set<User> users = new HashSet<User> ();

	private Boolean publicFlag = null;
	private Date releaseDate = null;

	public void setUsers ( Set<User> users ) {
		this.users = users;
	}

	/**
	 * You've to re-map this in the delegator, typically with
	 * @ManyToMany ( mappedBy = "..." cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH } )
	 *
	 */
	public Set<User> getUsers ()
	{
		return users;
	}

	/**
	 * You need to tell me the delegator and the method it uses to add itself to the other end of the relation,
	 * eg, for BioSample: addUser ( this, user, "addBioSample" )
	 */
	public <D> boolean addUser ( D delegator, User user, String inverseRelationAddMethod ) {
		return Many2ManyUtils.addMany2Many ( delegator, user, inverseRelationAddMethod, this.getUsers () );
	}

	/**
	 * @see #addUser(Object, User, String), I use an analogous approach here.
	 */
	public <D> boolean deleteUser ( D delegator, User user, String inverseRelationDeleteMethod ) {
		return Many2ManyUtils.deleteMany2Many ( delegator, user, inverseRelationDeleteMethod, this.getUsers () );
	}

	/**
	 * @see #isPublic().
	 *
	 * Add this to the delegating's method: @Column ( name = "public_flag", nullable = true ) */
	public Boolean getPublicFlag () {
		return this.publicFlag;
	}

	public void setPublicFlag ( Boolean publicFlag ) {
		this.publicFlag = publicFlag;
	}

  /**
   * @see #isPublic().
   *
   * Add this to the delegating's method: @Column ( name = "release_date", nullable = true ) */
	public Date getReleaseDate ()
	{
		return releaseDate;
	}


	public void setReleaseDate ( Date releaseDate )
	{
		this.releaseDate = releaseDate;
	}

	/**
	 * An entity is public if {@link #getPublicFlag()} is non-null and true or {@link #getReleaseDate()} <= now().
	 *
	 * Add this to the delegating's method: @Transient */
	public PublicStatus isPublic ()
	{
		Date now = new Date ();


		if ( this.getPublicFlag() == null ) {

			if (this.getReleaseDate() == null ) {
				// In case both the public flag or the release dates are null
				// should rely on the MSI release date, not available here
				return PublicStatus.UNKNOWN;
			}

			if ( this.releaseDate.before (now) || this.releaseDate.equals(now) ) {
				return PublicStatus.PUBLIC;
			}

			return PublicStatus.PRIVATE;

		} else {

			if (this.publicFlag) {
				return PublicStatus.PUBLIC;
			}

			return PublicStatus.PRIVATE;
		}
	}
	
}
