package ca.uhn.fhir.rest.param.construction;

import static java.util.Arrays.stream;

import ca.uhn.fhir.model.api.IQueryParameterType;
import com.google.common.collect.Streams;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handler for mapping positional arguments to a bean via annotations.
 * Requires the following about the TBean type:
 * 	- fields must have the OrderedField annotation with unique order values for consideration
 *    - has a single constructor
 * 	- has a parameterless constructor
 * @param <TBean>
 */
public final class AnnotationBeanHandler<TBean> extends BeanHandler<TBean> {

  public AnnotationBeanHandler(Class<TBean> type) {
    super(type);
  }

  public static <TBean> boolean supports(Class<TBean> type) {
    return stream(type.getDeclaredFields())
      .anyMatch(f -> f.isAnnotationPresent(OrderedField.class));
  }

  @Override
  public TBean buildBean(
    List<TypeValue<?>> typeValues,
    BiConsumer<IQueryParameterType, String> setParamValue) {

  	TBean bean = newInstance();

  	Streams.forEachPair(getOrderedFields(), typeValues.stream(), setField(bean, setParamValue));

  	return bean;
  }

  @Override
  public List<String> getValues(
    TBean instance,
    Function<IQueryParameterType, String> getParamValue) {
	  return getOrderedFields()
		  .map(getFieldValue(instance, getParamValue))
		  .collect(Collectors.toList());
  }

  @Override
  public List<Class<?>> getTypes() {
	  return getOrderedFields()
		  .map(Field::getType)
		  .collect(Collectors.toList());
  }

	private Stream<Field> getOrderedFields() {
		return stream(type.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(OrderedField.class))
			.sorted(Comparator.comparingInt(f -> f.getAnnotation(OrderedField.class).order()))
			.peek(f -> f.setAccessible(true));
	}

	private BiConsumer<Field, TypeValue<?>> setField(
		TBean bean,
		BiConsumer<IQueryParameterType, String> setParamValue) {
  		return (field, typeValue) -> {
			Object fieldValue = toFieldValue(typeValue, setParamValue);
			try {
				field.set(bean, fieldValue);
			} catch (IllegalAccessException e) {
				throw new BeanHandlerException("Cannot set bean value: " + fieldValue);
			}
		};
	}
}
