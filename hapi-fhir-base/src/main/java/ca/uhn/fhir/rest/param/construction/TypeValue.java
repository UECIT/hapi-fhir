package ca.uhn.fhir.rest.param.construction;

public class TypeValue<V> {

  private final Class<V> type;
  private final String value;

  public TypeValue(Class<V> type, String value) {
    this.type = type;
    this.value = value;
  }

	public Class<V> getType() {
		return type;
	}

	public String getValue() {
		return value;
	}
}
