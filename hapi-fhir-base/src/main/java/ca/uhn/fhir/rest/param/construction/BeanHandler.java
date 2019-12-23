package ca.uhn.fhir.rest.param.construction;

import static java.util.Arrays.stream;

import ca.uhn.fhir.model.api.IQueryParameterType;
import com.google.common.collect.MoreCollectors;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Generic handler class for mapping positional arguments to a bean.
 * Works with the following argument types:
 * 	- String
 * 	- int/Integer
 * 	- IQueryParameterType
 * @param <TBean>
 */
public abstract class BeanHandler<TBean> {

  protected final Class<TBean> type;

  public BeanHandler(Class<TBean> type) {
    this.type = type;
  }

  public abstract TBean buildBean(
    List<TypeValue<?>> typeValues,
    BiConsumer<IQueryParameterType, String> setParamValue);
  public abstract List<String> getValues(
    TBean instance,
    Function<IQueryParameterType, String> getParamValue);
  public abstract List<Class<?>> getTypes();

	@SuppressWarnings("unchecked")
	protected Constructor<TBean> getConstructor() {
		return (Constructor<TBean>) stream(type.getConstructors())
			.collect(MoreCollectors.onlyElement());
	}

	protected TBean newInstance() {
		return newInstance(new Object[]{});
	}
	protected TBean newInstance(Object[] args) {
		try {
			return getConstructor().newInstance(args);
		} catch (ReflectiveOperationException e) {
			throw new BeanHandlerException("Failed to instantiate type: " + type, e);
		}
	}

	protected Function<Field, String> getFieldValue(
		TBean instance,
		Function<IQueryParameterType, String> getParamValue) {

		return field -> {
			try {
				Object value = field.get(instance);

				if (value instanceof IQueryParameterType) {
					return getParamValue.apply((IQueryParameterType) value);
				}

				return value.toString();
			} catch (IllegalAccessException e) {
				throw new BeanHandlerException("Inaccessible field: " + field, e);
			}
		};
	}

	protected  <V> Object toFieldValue(
		TypeValue<V> typeValue,
		BiConsumer<IQueryParameterType, String> setParamValue) {

		Class<V> fieldType = typeValue.getType();

		if (fieldType == Integer.class || fieldType == int.class) {
			return Integer.parseInt(typeValue.getValue());
		}

		if (fieldType == String.class) {
			return typeValue.getValue();
		}

		if (IQueryParameterType.class.isAssignableFrom(fieldType)) {
			IQueryParameterType newParam;
			try {
				Constructor<?> newParamConstructor = fieldType.getConstructor();
				newParam = (IQueryParameterType) newParamConstructor.newInstance();
			} catch (ReflectiveOperationException e) {
				String message = "Error building param " + fieldType.getName() + " with empty constructor";
				throw new BeanHandlerException(message, e);
			}
			setParamValue.accept(newParam, typeValue.getValue());
			return newParam;
		}

		return null;
	}
}
