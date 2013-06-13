package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base interface to make a command line interface for the BioSD access control API (users, permissions etc).
 * This follows the <a href = "http://en.wikipedia.org/wiki/Interpreter_pattern">interpreter pattern</a>, with both 
 * parsing and interpretation in the same procedure.
 * 
 * This abstract class contains just a few common things. Concrete implementations contains various signatures of the 
 * run() method.
 * 
 * @see AccessControlCLI, the entry point to the access control CLI.
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
abstract class CLIParser
{
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/** The default just invokes {@link #setEntityManager(EntityManager)}. */
	public CLIParser ( EntityManager entityManager )
	{
		this.setEntityManager ( entityManager );
	}

	public abstract EntityManager getEntityManager ();

	/**
	 * Prepares objects that need to connect an underline database (e.g., DAOs). This is usually called by the constructor.
	 */
	public abstract void setEntityManager ( EntityManager entityManager );
}
