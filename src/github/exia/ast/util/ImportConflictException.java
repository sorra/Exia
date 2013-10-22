package github.exia.ast.util;

public class ImportConflictException extends RuntimeException {

	private static final long serialVersionUID = -4574323641616693481L;

	public ImportConflictException() {
	}

	public ImportConflictException(String arg0) {
		super(arg0);
	}

	public ImportConflictException(Throwable arg0) {
		super(arg0);
	}

	public ImportConflictException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
