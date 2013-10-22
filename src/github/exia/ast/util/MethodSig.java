package github.exia.ast.util;

import github.exia.util.CommonUtils;

import java.util.List;

import org.junit.Assert;

/**
 * @Immutable 
 */
public class MethodSig {
	private String packageName;
	private String className;
	private String methodName;
	private List<String> parameters;
	
	// boosts
	private String stdQname;
	private String octOwnerName;
	private int hash;

	public MethodSig(String packageName, String className, String methodName, List<String> parameters) {
        assertNoSpace(packageName);
        assertNoSpace(className);
        assertNoSpace(methodName);
        for (String p : parameters) {
          assertNoSpace(p);
        }
		this.packageName = packageName;
		this.className = className;
		this.methodName = methodName;
		this.parameters = parameters;
		
		// Big boost to construct octQname only once. 
		stdQname = packageName + '.' + className + '.' + methodName;
		octOwnerName = packageName.replace('.', '/') + '/' + className;
		hash = stdQname.hashCode() + parameters.hashCode();
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<String> parameters() {
		return parameters;
	}

	public String stdQname() {
		return stdQname;
	}
	
	public String octOwnerName() {
		return octOwnerName;
	}
	
	public String toMeta() {
	  final String D = "::";
	  return packageName+D+className+D+methodName+D+parameters.toString();
	}
	
	public static MethodSig fromMeta(String meta) {
	  String[] parts = meta.split("::");
	  return new MethodSig(parts[0], parts[1], parts[2], CommonUtils.readListRegardNull(parts[3]));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof MethodSig)) {
			return false;
		}
		
		MethodSig other = (MethodSig) obj;
		if (!other.packageName.equals(packageName)) {
			return false;
		}
		if (!other.className.equals(className)) {
			return false;
		}
		if (!other.methodName.equals(methodName)) {
			return false;
		}
		if (!other.parameters.equals(parameters)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return packageName+'.'+className+'#'+methodName+':'+parameters;
	}

    @Override
    public int hashCode() {
    	return hash;
    }

    private void assertNoSpace(String str) {
      Assert.assertTrue(str.indexOf(' ') == -1);
    }
}
