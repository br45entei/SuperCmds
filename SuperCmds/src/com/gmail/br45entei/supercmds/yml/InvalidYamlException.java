package com.gmail.br45entei.supercmds.yml;


/**This is a customized exception created by <a href="http://enteisislandsurvival.no-ip.org/about/author.html">Brian_Entei</a> to handle plugin errors when dealing with the configuration files.
 * @since 0.1
 * @author <a href="http://enteisislandsurvival.no-ip.org/about/author.html">Brian_Entei</a>
 */
public class InvalidYamlException extends Exception {
	/**@see <a href="http://docs.oracle.com/javase/7/docs/api/java/io/Serializable.html">Serializable</a>
	 * @author Brian_Entei*/
	private static final long serialVersionUID = -5098312743895938502L;
	public InvalidYamlException() {super();}
	public InvalidYamlException(String message) {super(message);}
	public InvalidYamlException(String message, Throwable cause) {super(message, cause);}
	public InvalidYamlException(Throwable cause) {super(cause);}
}