import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Models an entry in the register.
 *
 * @author emahagl
 */
public class Entry {
	private String dn;
	private List<Attribute> attributes = new ArrayList<Attribute>();

	public Entry() {
	}

	public Entry(String dn) {
		this.dn = dn;
	}

	public String getDn() {
		return dn;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public Attribute getAttribute(String name) {
		for (Iterator<Attribute> iterator = attributes.iterator(); iterator.hasNext();) {
			Attribute attribute = iterator.next();
			if (attribute.getName().equalsIgnoreCase(name)) return attribute;
		}
		return null;
	}

	public void addAttribute(Attribute attribute) {
		attributes.add(attribute);
	}
	
	public void addAttributes(List<Attribute> attributeList) {
		attributes.addAll(attributeList);
	}
}

