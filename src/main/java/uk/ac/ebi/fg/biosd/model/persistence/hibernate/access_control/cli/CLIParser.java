package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.regex.RegEx;


/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
abstract class CLIParser
{
	protected static final RegEx SAMPLE_GROUP_VISIBILITY_SET_SPEC_RE = new RegEx ( "(\\+|\\-\\-|\\-)([^\\s\\+]+)(\\+\\+)?" );
	protected static final RegEx SAMPLE_GROUP_VISIBILITY_GET_SPEC_RE = new RegEx ( "([^\\s\\+]+)(\\+\\+)?" );

	protected Logger log = LoggerFactory.getLogger ( this.getClass () );

	protected Map<String, Object> options = new HashMap<String, Object> ();
	
	public CLIParser ( EntityManager entityManager )
	{
		this.setEntityManager ( entityManager );
	}

	public abstract EntityManager getEntityManager ();
	public abstract void setEntityManager ( EntityManager entityManager );
}
