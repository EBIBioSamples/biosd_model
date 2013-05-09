package uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry;
import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.IdentifiableDAO;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;

/**
 * DAO to manage {@link JobRegisterEntry}.
 *
 * <dl><dt>date</dt><dd>Apr 24, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class JobRegisterDAO extends IdentifiableDAO<JobRegisterEntry>
{
	public JobRegisterDAO ( EntityManager entityManager ) {
		super ( JobRegisterEntry.class, entityManager );
	}
	
	
	/**
	 * Create an entry to track this entity.
	 */
	public void create ( Accessible entity, Operation operation, Date date )
	{
		super.create ( new JobRegisterEntry ( entity, operation, date ) );
	}

	/**
	 * Create an entry to track this entity.
	 */
	public void create ( Accessible entity, Operation operation )
	{
		create ( entity, operation, new Date () );
	}



	/**
	 * All the entries deleted in this time window (extremes included). Filters by operation if this is non-null.
	 */
	@SuppressWarnings ( "unchecked" )
	public List<JobRegisterEntry> find ( Date from, Date to, Operation operation )
	{
		String hql = 
			"FROM " + JobRegisterEntry.class.getCanonicalName () + " jr WHERE jr.timestamp BETWEEN :from AND :to" + 
			( operation == null ? "" : " AND operation = :operation" );
		
		Query q = this.getEntityManager ().createQuery ( hql )
		.setParameter ( "from", from )
		.setParameter ( "to", to );
		if ( operation != null ) q.setParameter ( "operation", operation );

		return q.getResultList ();
	}

	/**
	 * A wrapper of {@link #find(Date, Date, Operation) find (from, to, null)} 
	 */
	public List<JobRegisterEntry> find ( Date from, Date to ) {
		return find ( from, to , null );
	}

	
	/**
	 * All the entries deleted in the last daysAgo. If you send in a negative number, this will become positive and you'll
	 * search things in the future, presumably getting a null result. Filters by operation if this is non-null.
	 */
	public List<JobRegisterEntry> find ( int daysAgo, Operation operation )
	{
		Calendar cal = Calendar.getInstance ();
		cal.add ( Calendar.DAY_OF_YEAR, - daysAgo );
		return find ( cal.getTime (), new Date (), operation );
	}
	
	/** A wrapper of {@link #find(int, Operation) find ( daysAgo, null )} */
	public List<JobRegisterEntry> find ( int daysAgo )
	{
		return find ( daysAgo, (Operation) null );
	}

	/**
	 * Invokes {@link #hasEntry(String, String, Date, Date, Operation)} with entity.getClass ().getSimpleName () and
	 * entity.getAcc().
	 */
	public boolean hasEntry ( Accessible entity, Date from, Date to, Operation operation ) {
		return hasEntry ( entity.getClass ().getSimpleName (), entity.getAcc (), from, to, operation );
	}

	/**
	 * Invokes {@link #hasEntry(Accessible, Date, Date, Operation)}.
	 */
	public boolean hasEntry ( Accessible entity, Date from, Date to ) {
		return hasEntry ( entity, from, to, null );
	}

	
	/**
	 * Tells you if this entry exists in the time window. Operation is only considered when non-null.
	 */
	public boolean hasEntry ( String entityType, String acc, Date from, Date to, Operation operation )
	{
		String hql = 
			"SELECT jr.id FROM " + JobRegisterEntry.class.getCanonicalName () + " jr " +
			"WHERE entityType = :type AND acc = :acc AND jr.timestamp BETWEEN :from AND :to" 
			+ ( operation == null ? "" : " AND operation = :operation" );
		
		Query q = this.getEntityManager ().createQuery ( hql )
		.setParameter ( "type", entityType )
		.setParameter ( "acc", acc )
		.setParameter ( "from", from )
		.setParameter ( "to", to );
		if ( operation != null ) q.setParameter ( "operation", operation );

		return !q.getResultList ().isEmpty ();
	}

	/** Invokes {@link #hasEntry(String, String, Date, Date, Operation)} with operation = null */
	public boolean hasEntry ( String entityType, String acc, Date from, Date to )
	{
		return hasEntry ( entityType, acc, from, to, null );
	}

	
	/**
	 * Invokes {@link #hasEntry(String, String, int, Operation))} with entity.getClass ().getSimpleName () and
	 * entity.getAcc().
	 */
	public boolean hasEntry ( Accessible entity, int daysAgo, Operation operation ) {
		return hasEntry ( entity.getClass ().getSimpleName (), entity.getAcc (), daysAgo, operation );
	}

	/** Invokes {@link #hasEntry(Accessible, int, Operation)} with operation = null */
	public boolean hasEntry ( Accessible entity, int daysAgo ) {
		return hasEntry ( entity, daysAgo, null );
	}

	/**
	 * Tells if an event occurred within daysAgo days and now. If you send in a negative number, it will search in 
	 * the future, presumably returning an empty result.
	 */
	public boolean hasEntry ( String entityType, String acc, int daysAgo, Operation operation )
	{
		Calendar cal = Calendar.getInstance ();
		cal.add ( Calendar.DAY_OF_YEAR, - daysAgo );
		
		return hasEntry ( entityType, acc, cal.getTime (), new Date (), operation );
	}

	/** Invokes {@link #hasEntry(String, String, int, Operation)} with operation = null */
	public boolean hasEntry ( String entityType, String acc, int daysAgo ) {
		return hasEntry ( entityType, acc, daysAgo, null );
	}

	/**
	 * Deletes all job register entries included in the time window. 
	 */
	public long clean ( Date from, Date to )
	{
		String hql = 
			"DELETE FROM " + JobRegisterEntry.class.getCanonicalName () + " jr WHERE jr.timestamp BETWEEN :from AND :to";
		return this.getEntityManager ().createQuery ( hql )
		.setParameter ( "from", from )
		.setParameter ( "to", to )
		.executeUpdate ();
	}
	
	/**
	 * Deletes all job register entries older than ageDays days. This is useful for keeping the log not too big by flushing
	 * away older stuff in it. It is actually invoked by the BioSD unloader.  
	 */
	public long clean ( int ageDays ) 
	{
		Calendar cal = Calendar.getInstance ();
		cal.add ( Calendar.DAY_OF_YEAR, - Math.abs ( ageDays ) );
		
		String hql = "DELETE FROM " + JobRegisterEntry.class.getCanonicalName () + " jr WHERE jr.timestamp < :from";
		
		return this.getEntityManager ().createQuery ( hql )
		.setParameter ( "from", cal.getTime () )
		.executeUpdate ();
	}

}
