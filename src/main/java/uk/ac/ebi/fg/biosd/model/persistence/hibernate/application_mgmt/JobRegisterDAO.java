package uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry;
import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.IdentifiableDAO;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;

/**
 * DAO to manage {@link JobRegisterEntry}.
 * 
 * TODO: review all these signatures and null cases.
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
	 * All the entries of type entityType, affected by the operation 'operation' in the parameter time window (extremes included). 
	 * if entityType is null, gets all entities, if operation is null, gets any operation. 
	 */
	@SuppressWarnings ( "unchecked" )
	public List<JobRegisterEntry> find ( Date from, Date to, String entityType, Operation operation )
	{
		entityType = StringUtils.trimToNull ( entityType );
		
		String hql = 
			"FROM " + JobRegisterEntry.class.getCanonicalName () + " jr WHERE 1=1\n" +
			( from != null 
			    ? ( to != null ? " AND jr.timestamp BETWEEN :from AND :to" : " AND jr.timestamp >= :from" )
			    : ( to != null ? " AND jr.timestamp <= :to" : "" ) // from is null here
			) + 
			( operation == null ? "" : " AND operation = :operation" ) + 
			( entityType == null ? "" : " AND entityType = :entityType" );
		
		Query q = this.getEntityManager ().createQuery ( hql );
		if ( from != null ) q.setParameter ( "from", from );
		if ( to != null ) q.setParameter ( "to", to );
		if ( operation != null ) q.setParameter ( "operation", operation );
		if ( entityType != null ) q.setParameter ( "entityType", entityType );
		
		return q.getResultList ();
	}

	/** 
	 * A wrapper of {@link #find(String, Date, Date, Operation)}, which uses {@link Class#getSimpleName()} as 
	 * entityType parameter. 
	 */
	public List<JobRegisterEntry> find ( Date from, Date to, Class<? extends Accessible> entityType, Operation operation )
	{
		return find ( from, to, entityType == null ? null : entityType.getSimpleName (), operation );
	}

	public List<JobRegisterEntry> find ( Date from, Date to, Class<? extends Accessible> entityType )
	{
		return find ( from, to, entityType, null );
	}
	
	/** A wrapper of {@link #find(Date, Date, String, Operation)} with operation = null (i.e., gets all operations) */
	public List<JobRegisterEntry> find ( Date from, Date to, String entityType )
	{
		return find ( from, to, entityType, null );
	}
	
	/** A wrapper of {@link #find(Date, Date, String, Operation)} with entityType = null (i.e., gets all entity types) */
	public List<JobRegisterEntry> find ( Date from, Date to, Operation operation )
	{
		return find ( from, to, (String) null, operation );
	}
	
	/** 
	 * A wrapper of {@link #find(Date, Date, String, Operation)} with entityType and operatin = null 
	 * (i.e., gets all entity types and all operations) 
	 */
	public List<JobRegisterEntry> find ( Date from, Date to ) {
		return find ( from, to , (String) null, null );
	}

	
	/**
	 * All the entries of type entityType, affected by the parameter operation in the last daysAgo.
	 * if entityType is null, you'll get all the entities, if operation is null, you'll get all the operations.
	 *  
	 * If you send in a negative number, you'll search things in the future, presumably getting a null result. 
	 */
	public List<JobRegisterEntry> find ( int daysAgo, String entityType, Operation operation )
	{
		Calendar cal = Calendar.getInstance ();
		cal.add ( Calendar.DAY_OF_YEAR, - daysAgo );
		return find ( cal.getTime (), new Date (), entityType, operation );
	}

	/**
	 * A wrapper of {@link #find(int, String, Operation)} that uses {@link #find(String, Date, Date, Operation)}.
	 */
	public List<JobRegisterEntry> find ( int daysAgo, Class<? extends Accessible> entityType, Operation operation )
	{
		return find ( daysAgo, entityType == null ? null : entityType.getSimpleName (), null );
	}
	
	/** A wrapper of {@link #find(int, String, Operation)} with operation = null (i.e., search all operations) */
	public List<JobRegisterEntry> find ( int daysAgo, String entityType )
	{
		return find ( daysAgo, entityType, null );
	}

	/** A wrapper of {@link #find(int, String, Operation)} with operation = null (i.e., search all operations) */
	public List<JobRegisterEntry> find ( int daysAgo, Class<? extends Accessible> entityType )
	{
		return find ( daysAgo, entityType, null );
	}

	/** A wrapper of {@link #find(int, String, Operation)} with entityType = null (i.e., search all entities) */
	public List<JobRegisterEntry> find ( int daysAgo, Operation operation )
	{
		return find ( daysAgo, (String) null, operation );
	}
	
	/** A wrapper of {@link #find(int, Operation) find ( daysAgo, null )} (i.e., search all entity types and operations). */
	public List<JobRegisterEntry> find ( int daysAgo )
	{
		return find ( daysAgo, (String) null, null );
	}

	/**
	 * TODO: comment me.
	 */
	public JobRegisterEntry findLast ( String entityType, String accession, Operation operation )
	{
		entityType = StringUtils.trimToNull ( entityType );
		accession = StringUtils.trimToNull ( accession );
		
		String hql = 
			"FROM " + JobRegisterEntry.class.getCanonicalName () + " jr\nWHERE 1 = 1" + 
			( operation == null ? "" : " AND operation = :operation" ) + 
			( entityType == null ? "" : " AND entityType = :entityType" ) +
			( accession == null ? "" : " AND accession = :accession" ) +
			"\nORDER BY timestamp DESC";
		
		Query q = this.getEntityManager ().createQuery ( hql );
		if ( operation != null ) q.setParameter ( "operation", operation );
		if ( entityType != null ) q.setParameter ( "entityType", entityType );
		if ( accession != null ) q.setParameter ( "accession", accession );
		
		List<JobRegisterEntry> result = (List<JobRegisterEntry>) q.getResultList ();
		return result == null || result.isEmpty () ? null : result.iterator ().next ();
	}


	public JobRegisterEntry findLast ( Class<? extends Accessible> entityType, String accession, Operation operation )
	{
		return findLast ( entityType == null ? null : entityType.getSimpleName (), accession, operation );
	}

	public JobRegisterEntry findLast ( Accessible entity, Operation operation )
	{
		return entity == null 
			? findLast ( (Class<Accessible>) null, null, operation )
			: findLast ( entity.getClass (), entity.getAcc (), operation );
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
	 * TODO: allow for null time bounds.
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
	 * TODO: comment me!
	 * TODO: allow for null time bounds.
	 */
	public long count ( Date from, Date to, String entityType, Operation operation )
	{
		entityType = StringUtils.trimToNull ( entityType );
		
		String hql = 
			"SELECT COUNT (*) AS ct FROM " + JobRegisterEntry.class.getCanonicalName () 
			  + " jr\nWHERE jr.timestamp BETWEEN :from AND :to" + 
			( operation == null ? "" : " AND operation = :operation" ) + 
			( entityType == null ? "" : " AND entityType = :entityType" );
		
		Query q = this.getEntityManager ().createQuery ( hql )
		.setParameter ( "from", from )
		.setParameter ( "to", to );
		if ( operation != null ) q.setParameter ( "operation", operation );
		if ( entityType != null ) q.setParameter ( "entityType", entityType );
		
		return (Long) q.getSingleResult ();
	}

	/** 
	 * A wrapper of {@link #count(String, Date, Date, Operation)}, which uses {@link Class#getSimpleName()} as 
	 * entityType parameter. 
	 */
	public long count ( Date from, Date to, Class<? extends Accessible> entityType, Operation operation )
	{
		return count ( from, to, entityType == null ? null : entityType.getSimpleName (), operation );
	}

	/** A wrapper of {@link #count(Date, Date, Class, Operation)} with null as operation */
	public long count ( Date from, Date to, Class<? extends Accessible> entityType )
	{
		return count ( from, to, entityType, null );
	}
	
	/** A wrapper of {@link #count(Date, Date, String, Operation)} with operation = null (i.e., gets all operations) */
	public long count ( Date from, Date to, String entityType )
	{
		return count ( from, to, entityType, null );
	}
	
	/** A wrapper of {@link #count(Date, Date, String, Operation)} with entityType = null (i.e., gets all entity types) */
	public long count ( Date from, Date to, Operation operation )
	{
		return count ( from, to, (String) null, operation );
	}
	
	/** 
	 * A wrapper of {@link #count(Date, Date, String, Operation)} with entityType and operatin = null 
	 * (i.e., gets all entity types and all operations) 
	 */
	public List<JobRegisterEntry> count ( Date from, Date to ) {
		return find ( from, to , (String) null, null );
	}

	
	/**
	 * All the entries of type entityType, affected by the parameter operation in the last daysAgo.
	 * if entityType is null, you'll get all the entities, if operation is null, you'll get all the operations.
	 *  
	 * If you send in a negative number, you'll search things in the future, presumably getting a null result. 
	 */
	public long count ( int daysAgo, String entityType, Operation operation )
	{
		Calendar cal = Calendar.getInstance ();
		cal.add ( Calendar.DAY_OF_YEAR, - daysAgo );
		return count ( cal.getTime (), new Date (), entityType, operation );
	}

	/**
	 * A wrapper of {@link #find(int, String, Operation)} that uses {@link #find(String, Date, Date, Operation)}.
	 */
	public long count ( int daysAgo, Class<? extends Accessible> entityType, Operation operation )
	{
		return count ( daysAgo, entityType == null ? null : entityType.getSimpleName (), null );
	}
	
	/** A wrapper of {@link #find(int, String, Operation)} with operation = null (i.e., search all operations) */
	public long count ( int daysAgo, String entityType )
	{
		return count ( daysAgo, entityType, null );
	}

	/** A wrapper of {@link #find(int, String, Operation)} with operation = null (i.e., search all operations) */
	public long count ( int daysAgo, Class<? extends Accessible> entityType )
	{
		return count ( daysAgo, entityType, null );
	}

	/** A wrapper of {@link #count(int, String, Operation)} with entityType = null (i.e., search all entities) */
	public long count ( int daysAgo, Operation operation )
	{
		return count ( daysAgo, (String) null, operation );
	}
	
	/** A wrapper of {@link #count(int, Operation) find ( daysAgo, null )} (i.e., search all entity types and operations). */
	public long count ( int daysAgo )
	{
		return count ( daysAgo, (String) null, null );
	}	
	

	/**
	 * Deletes all job register entries included in the time window. 
	 * TODO: allow for null time bounds
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
