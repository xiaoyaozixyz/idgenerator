package com.xyz.idgen.exception;

public class UnInitedException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6357003004801638754L;

	public UnInitedException(String eInfo)
	{
		super("[uninitialized exception!] " + eInfo);
	}
	public UnInitedException()
	{
		super("[uninitialized exception!]");
	}
}
