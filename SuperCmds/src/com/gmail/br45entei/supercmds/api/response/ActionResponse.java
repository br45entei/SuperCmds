package com.gmail.br45entei.supercmds.api.response;

/** Class used to provide detailed information about the result of any arbitrary
 * action.
 * 
 * @author Brian_Entei */
public class ActionResponse {
	private final boolean	wasSuccessful;
	private final String	message;
	private final int		statusCode;
	
	/** @param successful Whether or not the action was successful
	 * @param message The message, if any, of the action's result
	 * @param statusCode The status code of the action(zero is default) */
	public ActionResponse(boolean successful, String message, int statusCode) {
		this.wasSuccessful = successful;
		this.message = message;
		this.statusCode = statusCode;
	}
	
	/** @param successful Whether or not the action was successful
	 * @param message The message, if any, of the action's result */
	public ActionResponse(boolean successful, String message) {
		this(successful, message, 0);
	}
	
	/** @return Whether or not the action was successful */
	public final boolean success() {
		return this.wasSuccessful;
	}
	
	/** @return The message, if any, of the action's result(if the message is
	 *         null, an empty string "" is returned) */
	public final String getMessage() {
		return(this.message != null ? this.message : "");
	}
	
	/** @return The status code of the action(zero is default) */
	public final int getStatusCode() {
		return this.statusCode;
	}
	
}
