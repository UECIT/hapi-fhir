package ca.uhn.fhir.rest.param.construction;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import ca.uhn.fhir.model.api.IQueryParameterType;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handler for mapping positional arguments to a bean via a constructor
 * Requires the following about the TBean type:
 *    - has a single constructor
 *    - all constructor parameter names are identical to the corresponding field names
 *    - the app will be compiled with the -parameters argument
 *
 * @param <TBean>
 */
public final class ConstructorBeanHandler<TBean> extends BeanHandler<TBean> {

  public ConstructorBeanHandler(Class<TBean> type) {
    super(type);
  }

  @Override
  public TBean buildBean(
    List<TypeValue<?>> typeValues,
    BiConsumer<IQueryParameterType, String> setParamValue) {

	  Object[] args = typeValues.stream()
		  .map(typeValue -> toFieldValue(typeValue, setParamValue))
		  .toArray(Object[]::new);

	  return newInstance(args);
  }

  @Override
  public List<String> getValues(
  	TBean instance,
	  Function<IQueryParameterType, String> getParamValue) {
  	return stream(getConstructor().getParameters())
		 .map(Parameter::getName)
		 .peek(this::throwIfUnnamed)
       .map(this::getField)
       .map(getFieldValue(instance, getParamValue))
       .collect(Collectors.toList());
  }

  @Override
  public List<Class<?>> getTypes() {
    return asList(getConstructor().getParameterTypes());
  }

  private void throwIfUnnamed(String name) {
	  if (Pattern.compile("^isArg[0-9]+$").matcher(name).matches()) {
		  return;
	  }
	  throw new BeanHandlerException("Code was not compiled with the -parameters flag so cannot infer bean field order"
			  + " for serialisation. You can also annotate fields with OrderedField(order)");
  }

  private Field getField(String name) {
    try {
      Field field = type.getDeclaredField(name);
      field.setAccessible(true);
      return field;
    } catch (NoSuchFieldException e) {
    	throw new BeanHandlerException("Field not found", e);
    }
  }
}
