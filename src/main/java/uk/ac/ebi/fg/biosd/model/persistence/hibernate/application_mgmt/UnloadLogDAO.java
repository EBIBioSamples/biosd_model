package uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.application_mgmt.UnloadLogEntry;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.IdentifiableDAO;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;

/**
 * DAO to manage {@link UnloadLogEntry}.
 *
 * <dl><dt>date</dt><dd>Apr 24, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnloadLogDAO extends IdentifiableDAO<UnloadLogEntry>
{
	public UnloadLogDAO ( EntityManager entityManager ) {
		super ( UnloadLogEntry.class, entityManager );
	}

	/**
	 * All the entries deleted in this time window (extremes included).
	 */
	@SuppressWarnings ( "unchecked" )
	public List<UnloadLogEntry> find ( Date from, Date to )
	{
		String hql = 
			"FROM " + UnloadLogEntry.class.getCanonicalName () + " log WHERE log.timestamp BETWEEN :from AND :to";
		return this.getEntityManager ().createQuery ( hql )
		.setParameter ( "from", from )
		.setParameter ( "to", to )
		.getResultList ();
	}

	/**
	 * All the entries deleted in the last daysAgo. If you send in a negative number, this will become positive and you'll
	 * search things in the future, presumably getting a null result.
	 */
	public List<UnloadLogEntry> find ( int daysAgo )
	{
		Calendar cal = Calendar.getInstance ();
		cal.add ( Calendar.DAY_OF_YEAR, - daysAgo );
		return find ( cal.getTime (), new Date () );
	}
	
	/**
	 * Invokes {@link #wasDeleted(String, String, Date, Date)} with entity.getClass ().getSimpleName () and
	 * entity.getAcc().
	 */
	public boolean wasDeleted ( Accessible entity, Date from, Date to ) {
		return wasDeleted ( entity.getClass ().getSimpleName (), entity.getAcc (), from, to );
	}
	
	/**
	 * Tells you if this entity was deleted in the time window.
	 */
	public boolean wasDeleted ( String entityType, String acc, Date from, Date to )
	{
		String hql = 
			"SELECT log.id FROM " + UnloadLogEntry.class.getCanonicalName () + " log" +
			"  WHERE entityType = :type AND acc = :acc AND log.timestamp BETWEEN :from AND :to ";
		
		return !this.getEntityManager ().createQuery ( hql )
		.setParameter ( "type", entityType )
		.setParameter ( "acc", acc )
		.setParameter ( "from", from )
		.setParameter ( "to", to )
		.getResultList ().isEmpty ();
	}

	
	/**
	 * Invokes {@link #wasDeleted(String, String, int))} with entity.getClass ().getSimpleName () and
	 * entity.getAcc().
	 */
	public boolean wasDeleted ( Accessible entity, int daysAgo ) {
		return wasDeleted ( entity.getClass ().getSimpleName (), entity.getAcc (), daysAgo );
	}

	/**
	 * Tells if an entity was deleted within daysAgo days and now. If you send in a negative number, it will search in 
	 * the future, presumably returning an empty result.
	 */
	public boolean wasDeleted ( String entityType, String acc, int daysAgo )
	{
		Calendar cal = Calendar.getInstance ();
		cal.add ( Calendar.DAY_OF_YEAR, - daysAgo );
		
		return wasDeleted ( entityType, acc, cal.getTime (), new Date () );
	}

	/**
	 * Deletes all unload log entries included in the time window. 
	 */
	public long delete ( Date from, Date to )
	{
		String hql = 
			"DELETE FROM " + UnloadLogEntry.class.getCanonicalName () + " log WHERE log.timestamp BETWEEN :from AND :to";
		return this.getEntityManager ().createQuery ( hql )
		.setParameter ( "from", from )
		.setParameter ( "to", to )
		.executeUpdate ();
	}
	
	/**
	 * Deletes all unload log entries older than ageDays days. This is useful for keeping the log not too big by flushing
	 * away older stuff in it. It is actually invoked by the BioSD unloader.  
	 */
	public long delete ( int ageDays ) 
	{
		Calendar cal = Calendar.getInstance ();
		cal.add ( Calendar.DAY_OF_YEAR, - Math.abs ( ageDays ) );
		
		String hql = 
			"DELETE FROM " + UnloadLogEntry.class.getCanonicalName () + " log WHERE log.timestamp < :from";
		
		return this.getEntityManager ().createQuery ( hql )
		.setParameter ( "from", cal.getTime () )
		.executeUpdate ();
	}

}
