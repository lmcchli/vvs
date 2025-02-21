/**
 * Models an attribute used in an Entry
 * 
 * @author emahagl
 */
public class Attribute {

	private String name;
	private String[] values;

	public Attribute(String name) {
		this.name = name;
	}

	public Attribute(String name, String[] values) {
		this.name = name;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public void addValue(String value) {
		if (values == null) {
			values = new String[] { value };
		} else {
			String[] newArray = new String[values.length + 1];
			System.arraycopy(values, 0, newArray, 0, values.length);
			newArray[values.length] = value;
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof Attribute) {
			Attribute a = (Attribute) obj;
			return a.getName().equalsIgnoreCase(name);
		} else {
			return super.equals(obj);
		}
	}
}
