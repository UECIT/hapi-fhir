package ca.uhn.fhir.rest.param.construction;

public class BeanHandlerException extends RuntimeException {

	public BeanHandlerException(String message, Throwable e) {
		super(message, e);
	}

	public BeanHandlerException(String message) {
		super(message);
	}
}
