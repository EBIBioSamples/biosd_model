package uk.ac.ebi.fg.biosd.model.application_mgmt;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotEmpty;

import uk.ac.ebi.fg.core_model.toplevel.Accessible;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;

/**
 * This entity/table keeps track of relevant things that are deleted from the BioSD database. This is needed for operations
 * like incremental update of XML exports used for the BioSD web interface.
 *
 * <dl><dt>date</dt><dd>Apr 24, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table ( name = "unload_log" )
public class UnloadLogEntry extends Identifiable
{
	private String acc;
	private String entityType;
	private Date timestamp;
	
	protected UnloadLogEntry () {
		super ();
	}

	/**
	 * Identity is built on all these three properties.
	 */
	public UnloadLogEntry ( String acc, String entityType, Date timestamp )
	{
		super ();
		this.acc = acc;
		this.entityType = entityType;
		this.timestamp = timestamp;
	}

	/**
	 * Uses the current time as timestamp.
	 */
	public UnloadLogEntry ( String acc, String entityType )
	{
		this ( acc, entityType, new Date () );
	}

	/**
	 * Uses entity.getClass().getSimpleName() as type and {@link Accessible#getAcc()} as accession.
	 */
	public UnloadLogEntry ( Accessible entity, Date timestamp )
	{
		super ();
		if ( entity != null ) 
		{
			this.acc = entity.getAcc ();
			this.entityType = entity.getClass ().getSimpleName ();
		}
		this.timestamp = timestamp;
	}
	
	/**
	 * Like {@link #UnloadLogEntry(Accessible, Date)}, but uses the current time as timestamp.
	 */
	public UnloadLogEntry ( Accessible entity )
	{
		this ( entity, new Date () );
	}
	
	
	/**
	 * A string tag to identify the entity that was deleted. We recommend you use {@link Class#getSimpleName()} for this.
	 * The biosd model has no two classes with the same name and from different packages at the moment and we commit to avoid this
	 * in future too.
	 */
	@NotEmpty
	@Index ( name = "undel_log_entity" )
	public String getEntityType ()
	{
		return entityType;
	}

	protected void setEntityType ( String entityType )
	{
		this.entityType = entityType;
	}

	/**
	 * An accession identifying the entity of type {@link #getEntityType()} that was deleted. 
	 * We assume you keep track of {@link Accessible} only, so we recommend you use {@link Accessible#getAcc()} for this.
	 */
	@NotEmpty
	@Index ( name = "undel_log_acc" )
	public String getAcc ()
	{
		return acc;
	}

	protected void setAcc ( String acc )
	{
		this.acc = acc;
	}

	/**
	 * When the entity was deleted.
	 */
	@NotNull
	@Index ( name = "undel_log_ts" )
	public Date getTimestamp () {
		return timestamp;
	}
	
	protected void setTimestamp ( Date timestamp ) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Based on {@link #getEntityType()} + {@link #getAcc()} + {@link #getTimestamp()}.
	 */
  @Override
  public boolean equals ( Object o ) 
  {
  	if ( o == null ) return false;
  	if ( this == o ) return true;
  	if ( this.getClass () != o.getClass () ) return false;
  	
    // The entity type
  	UnloadLogEntry that = (UnloadLogEntry) o;
    if ( this.getEntityType () == null || that.getEntityType () == null || !this.entityType.equals ( that.entityType ) ) return false; 
    if ( this.getAcc () == null || that.getAcc () == null || !this.acc.equals ( that.acc ) ) return false; 
    if ( this.getTimestamp () == null || that.getTimestamp () == null || !this.timestamp.equals ( that.timestamp ) ) return false;
    return true;
  }
	
	/**
	 * Based on {@link #getEntityType()} + {@link #getAcc()} + {@link #getTimestamp()}.
	 */
	@Override
	public int hashCode ()
	{
		int result = 1;
		result = 31 * result + ( ( this.getEntityType () == null ) ? 0 : entityType.hashCode () );
		result = 31 * result + ( ( this.getAcc () == null ) ? 0 : acc.hashCode () );
		result = 31 * result + ( ( this.getTimestamp () == null ) ? 0 : timestamp.hashCode () );
		return result;
	}
  
  @Override
  public String toString() {
  	return String.format ( "%s { id: %d, entity: '%s', acc: '%s', timestamp: %t$5F %t$5T.%t$5L", 
  		this.getClass ().getSimpleName (), this.getId (), this.getEntityType (), this.getAcc (), this.getTimestamp () );
  }

}
