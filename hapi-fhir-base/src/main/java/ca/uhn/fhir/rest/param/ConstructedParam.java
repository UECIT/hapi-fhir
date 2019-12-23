package ca.uhn.fhir.rest.param;

/*
 * #%L
 * HAPI FHIR - Core Library
 * %%
 * Copyright (C) 2014 - 2019 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.param.construction.AnnotationBeanHandler;
import ca.uhn.fhir.rest.param.construction.BeanHandler;
import ca.uhn.fhir.rest.param.construction.BeanHandlerException;
import ca.uhn.fhir.rest.param.construction.ConstructorBeanHandler;
import ca.uhn.fhir.rest.param.construction.TypeValue;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.Validate;

/**
 * Represents a composite parameter that will have its parts mapped
 * to an object based on the argument order in the object constructor
 * @param <T>
 */
public class ConstructedParam<T> extends BaseParam implements IQueryParameterType {

	private static final char DELIMITER = '$';

	private T instance;
	private BeanHandler<T> beanHandler;

	public ConstructedParam(Class<T> type) {
		Validate.notNull(type);
		this.beanHandler = AnnotationBeanHandler.supports(type)
			? new AnnotationBeanHandler<>(type)
			: new ConstructorBeanHandler<>(type);
	}

	@Override
	String doGetQueryParameterQualifier() {
		return null;
	}


	@Override
	String doGetValueAsQueryToken(FhirContext theContext) {

		if (instance == null) {
			throw new InvalidRequestException("Value has not been set yet");
		}

		return String.join(
			Character.toString(DELIMITER),
			beanHandler.getValues(instance, param -> param.getValueAsQueryToken(theContext)));
	}

	private Stream<TypeValue<?>> getTypeValues(String value) {

		List<String> parts = ParameterUtil.splitParameterString(value, DELIMITER, false);
		List<Class<?>> types = beanHandler.getTypes();

		if (parts.size() != types.size()) {
			throw new BeanHandlerException(
				"Invalid value for composite parameter (did not match constructor argument count)."
					+ " Value was: " + value);
		}

		return Streams.zip(types.stream(), parts.stream(), TypeValue::new);
	}

	@Override
	void doSetValueAsQueryToken(
		FhirContext theContext,
		String theParamName,
		String theQualifier,
		String theValue) {

		List<TypeValue<?>> args = getTypeValues(theValue).collect(Collectors.toList());
		instance = beanHandler.buildBean(args, (param, value) ->
			param.setValueAsQueryToken(theContext, theParamName, theQualifier, value));
	}

	/**
	 * @return Returns the value for this parameter (the instance holding the values of the composite)
	 */
	public T getValue() {
		return instance;
	}

}
