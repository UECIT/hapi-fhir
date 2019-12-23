package ca.uhn.fhir.rest.param.binder;

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

import java.lang.reflect.Constructor;
import java.util.List;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.param.CompositeParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import java.util.stream.Stream;

class BaseBinder<T> {
	private List<Class<? extends IQueryParameterType>> myCompositeTypes;
	private Constructor<? extends T> myConstructor;
	private final Class<?> myConstructedType;

	public BaseBinder(
		Class<? extends T> theType,
		List<Class<? extends IQueryParameterType>> theCompositeTypes,
		Class<?> theConstructedType) {

		myCompositeTypes = theCompositeTypes;
		myConstructedType = theConstructedType;

		if (theType.equals(CompositeParam.class) && theCompositeTypes.size() != 2) {
			throw new ConfigurationException("Search parameter of type " + theType.getName()
				+ " must have 2 composite types declared in parameter annotation, found "
				+ theCompositeTypes.size());
		}

		try {
			Class<?>[] args = getArgs().map(t -> Class.class).toArray(Class[]::new);
			myConstructor = theType.getConstructor(args);
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException("Query parameter type " + theType.getName() + " has no constructor with types " + theCompositeTypes);
		}
	}

	public T newInstance() {
		try {
			return myConstructor.newInstance(getArgs().toArray());
		} catch (final Exception e) {
			throw new InternalErrorException(e);
		}
	}

	private Stream<Class<?>> getArgs() {
		return Stream.concat(myCompositeTypes.stream(), Stream.of(myConstructedType))
			.filter(t -> t != void.class && t != null);
	}
}
