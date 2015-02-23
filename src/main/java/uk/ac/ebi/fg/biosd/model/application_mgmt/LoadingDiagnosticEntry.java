package uk.ac.ebi.fg.biosd.model.application_mgmt;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import uk.ac.ebi.fg.core_model.resources.Const;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;

/**
 * Used in the BioSD SampleTab loader to store some diagnostic information during loading operations.
 *
 * <dl><dt>date</dt><dd>22 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table ( name = "loading_diagnostics" )
public class LoadingDiagnosticEntry extends Identifiable
{
	private String msiAcc;
	private String sampleTabPath;
	private String exceptionClassName;
	private String exceptionMessage;
	private Date timestamp;
	private Long parsingTimeMs;
	private Long persistenceTimeMs;
	private Integer itemsCount;

	protected LoadingDiagnosticEntry () {}

	/**
	 * We're using primitive classes to be able to accept nulls.
	 */
	public LoadingDiagnosticEntry ( String msiAcc, String sampleTabPath, Throwable ex, Long parsingTimeMs, Long persistenceTimeMs,
			Integer itemsCount )
	{
		super ();
		
		this.msiAcc = msiAcc;
		this.sampleTabPath = sampleTabPath;
		
		if ( ex != null ) {
			this.exceptionClassName = ex.getClass ().getName ();
			this.exceptionMessage = ex.getMessage ();
		}
		
		this.parsingTimeMs = parsingTimeMs;
		this.persistenceTimeMs = persistenceTimeMs;
		this.itemsCount = itemsCount;
		this.timestamp = new Date ();
	}
	
	@Column ( name = "msi_acc", length = Const.COL_LENGTH_S )
	@Index ( name = "load_diag_acc" )
	public String getMsiAcc ()
	{
		return msiAcc;
	}

	@Column ( name = "sampletab_path", length = 300 )
	@Index ( name = "load_diag_st" )
	public String getSampleTabPath ()
	{
		return sampleTabPath;
	}

	@Column ( name = "exception", length = 200 )
	@Index ( name = "load_diag_ex" )
	public String getExceptionClassName ()
	{
		return exceptionClassName;
	}

	@Column ( name = "ex_message", length = 300 )
	@Index ( name = "load_diag_exm" )
	public String getExceptionMessage ()
	{
		return exceptionMessage;
	}

	@Index ( name = "load_diag_ts" )
	public Date getTimestamp ()
	{
		return timestamp;
	}

	@Column ( name = "parse_time_ms", nullable = true )
	@Index ( name = "load_diag_parse_t" )
	public Long getParsingTimeMs ()
	{
		return parsingTimeMs;
	}

	@Column ( name = "persist_time_ms", nullable = true  )
	@Index ( name = "load_diag_perst_t" )
	public Long getPersistenceTimeMs ()
	{
		return persistenceTimeMs;
	}

	@Column ( name = "items_count", nullable = true )
	@Index ( name = "load_diag_items" )
	public Integer getItemsCount ()
	{
		return itemsCount;
	}

	
	
	
	protected void setMsiAcc ( String msiAcc )
	{
		this.msiAcc = msiAcc;
	}

	protected void setSampleTabPath ( String sampleTabPath )
	{
		this.sampleTabPath = sampleTabPath;
	}
	
	protected void setExceptionClassName ( String exceptionClassName )
	{
		this.exceptionClassName = exceptionClassName;
	}

	protected void setExceptionMessage ( String exceptionMessage )
	{
		this.exceptionMessage = exceptionMessage;
	}

	protected void setTimestamp ( Date timestamp )
	{
		this.timestamp = timestamp;
	}

	protected void setParsingTimeMs ( Long parsingTimeMs )
	{
		this.parsingTimeMs = parsingTimeMs;
	}

	protected void setPersistenceTimeMs ( Long persistenceTimeMs )
	{
		this.persistenceTimeMs = persistenceTimeMs;
	}

	protected void setItemsCount ( Integer itemsCount )
	{
		this.itemsCount = itemsCount;
	}
	
}

