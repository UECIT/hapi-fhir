package ca.uhn.fhir.model.primitive;

/*
 * #%L
 * HAPI FHIR - Core Library
 * %%
 * Copyright (C) 2014 - 2016 University Health Network
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
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.DAY;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.MILLI;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.MONTH;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.SECOND;
import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.YEAR;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.parser.DataFormatException;

public abstract class BaseDateTimeDt extends BasePrimitive<Date> {

	/*
	 * Add any new formatters to the static block below!!
	 */
	private static final List<FastDateFormat> ourFormatters;

	private static final Pattern ourYearDashMonthDashDayPattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
	private static final Pattern ourYearDashMonthPattern = Pattern.compile("[0-9]{4}-[0-9]{2}");
	private static final FastDateFormat ourYearFormat = FastDateFormat.getInstance("yyyy");
	private static final FastDateFormat ourYearMonthDayFormat = FastDateFormat.getInstance("yyyy-MM-dd");
	private static final FastDateFormat ourYearMonthDayNoDashesFormat = FastDateFormat.getInstance("yyyyMMdd");
	private static final Pattern ourYearMonthDayPattern = Pattern.compile("[0-9]{4}[0-9]{2}[0-9]{2}");
	private static final FastDateFormat ourYearMonthDayTimeFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");
	private static final FastDateFormat ourYearMonthDayTimeMilliFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private static final FastDateFormat ourYearMonthDayTimeMilliUTCZFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"));
	private static final FastDateFormat ourYearMonthDayTimeMilliZoneFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
	private static final FastDateFormat ourYearMonthDayTimeUTCZFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"));
	private static final FastDateFormat ourYearMonthDayTimeZoneFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZZ");
	private static final FastDateFormat ourYearMonthFormat = FastDateFormat.getInstance("yyyy-MM");
	private static final FastDateFormat ourYearMonthNoDashesFormat = FastDateFormat.getInstance("yyyyMM");
	private static final FastDateFormat ourHumanDateTimeFormat = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM);
	private static final FastDateFormat ourHumanDateFormat = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM);
	private static final Pattern ourYearMonthPattern = Pattern.compile("[0-9]{4}[0-9]{2}");
	private static final Pattern ourYearPattern = Pattern.compile("[0-9]{4}");
	private static final FastDateFormat ourYearMonthDayTimeMinsFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm");
	private static final FastDateFormat ourYearMonthDayTimeMinsZoneFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mmZZ");

	static {
		ArrayList<FastDateFormat> formatters = new ArrayList<FastDateFormat>();
		formatters.add(ourYearFormat);
		formatters.add(ourYearMonthDayFormat);
		formatters.add(ourYearMonthDayNoDashesFormat);
		formatters.add(ourYearMonthDayTimeFormat);
		formatters.add(ourYearMonthDayTimeMilliFormat);
		formatters.add(ourYearMonthDayTimeUTCZFormat);
		formatters.add(ourYearMonthDayTimeMilliUTCZFormat);
		formatters.add(ourYearMonthDayTimeMilliZoneFormat);
		formatters.add(ourYearMonthDayTimeZoneFormat);
		formatters.add(ourYearMonthFormat);
		formatters.add(ourYearMonthNoDashesFormat);
		ourFormatters = Collections.unmodifiableList(formatters);
	}

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BaseDateTimeDt.class);

	private TemporalPrecisionEnum myPrecision = TemporalPrecisionEnum.SECOND;
	private TimeZone myTimeZone;
	private boolean myTimeZoneZulu = false;

	/**
	 * Constructor
	 */
	public BaseDateTimeDt() {
		// nothing
	}

	/**
	 * Constructor
	 * 
	 * @throws DataFormatException
	 *            If the specified precision is not allowed for this type
	 */
	public BaseDateTimeDt(Date theDate, TemporalPrecisionEnum thePrecision) {
		setValue(theDate, thePrecision);
		if (isPrecisionAllowed(thePrecision) == false) {
			throw new DataFormatException("Invalid date/time string (datatype " + getClass().getSimpleName() + " does not support " + thePrecision + " precision): " + theDate);
		}
	}

	/**
	 * Constructor
	 */
	public BaseDateTimeDt(Date theDate, TemporalPrecisionEnum thePrecision, TimeZone theTimeZone) {
		this(theDate, thePrecision);
		setTimeZone(theTimeZone);
	}

	/**
	 * Constructor
	 * 
	 * @throws DataFormatException
	 *            If the specified precision is not allowed for this type
	 */
	public BaseDateTimeDt(String theString) {
		setValueAsString(theString);
		if (isPrecisionAllowed(getPrecision()) == false) {
			throw new DataFormatException("Invalid date/time string (datatype " + getClass().getSimpleName() + " does not support " + getPrecision() + " precision): " + theString);
		}
	}

	private void clearTimeZone() {
		myTimeZone = null;
		myTimeZoneZulu = false;
	}

	@Override
	protected String encode(Date theValue) {
		if (theValue == null) {
			return null;
		} else {
			GregorianCalendar cal;
			if (myTimeZoneZulu) {
				cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			} else if (myTimeZone != null) {
				cal = new GregorianCalendar(myTimeZone);
			} else {
				cal = new GregorianCalendar();
			}
			cal.setTime(theValue);

			switch (myPrecision) {
			case DAY:
				return ourYearMonthDayFormat.format(cal);
			case MONTH:
				return ourYearMonthFormat.format(cal);
			case YEAR:
				return ourYearFormat.format(cal);
			case MINUTE:
				if (myTimeZoneZulu) {
					return ourYearMonthDayTimeMinsFormat.format(cal) + "Z";
				} else {
					return ourYearMonthDayTimeMinsZoneFormat.format(cal);
				}
			case SECOND:
				if (myTimeZoneZulu) {
					return ourYearMonthDayTimeFormat.format(cal) + "Z";
				} else {
					return ourYearMonthDayTimeZoneFormat.format(cal);
				}
			case MILLI:
				if (myTimeZoneZulu) {
					return ourYearMonthDayTimeMilliFormat.format(cal) + "Z";
				} else {
					return ourYearMonthDayTimeMilliZoneFormat.format(cal);
				}
			}
			throw new IllegalStateException("Invalid precision (this is a HAPI bug, shouldn't happen): " + myPrecision);
		}
	}

	/**
	 * Returns the default precision for the given datatype
	 */
	protected abstract TemporalPrecisionEnum getDefaultPrecisionForDatatype();

	/**
	 * Gets the precision for this datatype (using the default for the given type if not set)
	 * 
	 * @see #setPrecision(TemporalPrecisionEnum)
	 */
	public TemporalPrecisionEnum getPrecision() {
		if (myPrecision == null) {
			return getDefaultPrecisionForDatatype();
		}
		return myPrecision;
	}

	/**
	 * Returns the TimeZone associated with this dateTime's value. May return <code>null</code> if no timezone was
	 * supplied.
	 */
	public TimeZone getTimeZone() {
		return myTimeZone;
	}

	private boolean hasOffset(String theValue) {
		boolean inTime = false;
		for (int i = 0; i < theValue.length(); i++) {
			switch (theValue.charAt(i)) {
			case 'T':
				inTime = true;
				break;
			case '+':
			case '-':
				if (inTime) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	/**
	 * To be implemented by subclasses to indicate whether the given precision is allowed by this type
	 */
	abstract boolean isPrecisionAllowed(TemporalPrecisionEnum thePrecision);

	public boolean isTimeZoneZulu() {
		return myTimeZoneZulu;
	}

	/**
	 * Returns <code>true</code> if this object represents a date that is today's date
	 * 
	 * @throws NullPointerException
	 *            if {@link #getValue()} returns <code>null</code>
	 */
	public boolean isToday() {
		Validate.notNull(getValue(), getClass().getSimpleName() + " contains null value");
		return DateUtils.isSameDay(new Date(), getValue());
	}

	@Override
	protected Date parse(String theValue) throws DataFormatException {
		try {
			if (theValue.length() == 4 && ourYearPattern.matcher(theValue).matches()) {
				if (!isPrecisionAllowed(YEAR)) {
					ourLog.debug("Invalid date/time string (datatype " + getClass().getSimpleName() + " does not support YEAR precision): " + theValue);
				}
				setPrecision(YEAR);
				clearTimeZone();
				return ((ourYearFormat).parse(theValue));
			} else if (theValue.length() == 6 && ourYearMonthPattern.matcher(theValue).matches()) {
				// Eg. 198401 (allow this just to be lenient)
				if (!isPrecisionAllowed(MONTH)) {
					ourLog.debug("Invalid date/time string (datatype " + getClass().getSimpleName() + " does not support DAY precision): " + theValue);
				}
				setPrecision(MONTH);
				clearTimeZone();
				return ((ourYearMonthNoDashesFormat).parse(theValue));
			} else if (theValue.length() == 7 && ourYearDashMonthPattern.matcher(theValue).matches()) {
				// E.g. 1984-01 (this is valid according to the spec)
				if (!isPrecisionAllowed(MONTH)) {
					ourLog.debug("Invalid date/time string (datatype " + getClass().getSimpleName() + " does not support MONTH precision): " + theValue);
				}
				setPrecision(MONTH);
				clearTimeZone();
				return ((ourYearMonthFormat).parse(theValue));
			} else if (theValue.length() == 8 && ourYearMonthDayPattern.matcher(theValue).matches()) {
				// Eg. 19840101 (allow this just to be lenient)
				if (!isPrecisionAllowed(DAY)) {
					ourLog.debug("Invalid date/time string (datatype " + getClass().getSimpleName() + " does not support DAY precision): " + theValue);
				}
				setPrecision(DAY);
				clearTimeZone();
				return ((ourYearMonthDayNoDashesFormat).parse(theValue));
			} else if (theValue.length() == 10 && ourYearDashMonthDashDayPattern.matcher(theValue).matches()) {
				// E.g. 1984-01-01 (this is valid according to the spec)
				if (!isPrecisionAllowed(DAY)) {
					ourLog.debug("Invalid date/time string (datatype " + getClass().getSimpleName() + " does not support DAY precision): " + theValue);
				}
				setPrecision(DAY);
				clearTimeZone();
				return ((ourYearMonthDayFormat).parse(theValue));
			} else if (theValue.length() >= 18) { // date and time with possible time zone
				char timeSeparator = theValue.charAt(10);
				if (timeSeparator != 'T') {
					throw new DataFormatException("Invalid date/time string: " + theValue);
				}
				int dotIndex = theValue.indexOf('.', 18);
				boolean hasMillis = dotIndex > -1;

				if (!hasMillis && !isPrecisionAllowed(SECOND)) {
					ourLog.debug("Invalid date/time string (data type does not support SECONDS precision): " + theValue);
				} else if (hasMillis && !isPrecisionAllowed(MILLI)) {
					ourLog.debug("Invalid date/time string (data type " + getClass().getSimpleName() + " does not support MILLIS precision):" + theValue);
				}

				Date retVal;
				if (hasMillis) {
					String value = theValue;
					
					/*
					 * If we have more than 3 digits of precision after the decimal point, we
					 * only parse the first 3 since Java Dates don't support more than that and
					 * FastDateFormat gets confused
					 */
					int offsetIndex = getOffsetIndex(theValue);
					if (offsetIndex >= 24) {
						value = theValue.substring(0, 23) + theValue.substring(offsetIndex);
					}
					
					try {
						if (hasOffset(value)) {
							retVal = ourYearMonthDayTimeMilliZoneFormat.parse(value);
						} else if (value.endsWith("Z")) {
							retVal = ourYearMonthDayTimeMilliUTCZFormat.parse(value);
						} else {
							retVal = ourYearMonthDayTimeMilliFormat.parse(value);
						}
					} catch (ParseException p2) {
						throw new DataFormatException("Invalid data/time string (" + p2.getMessage() + "): " + theValue);
					}
					setTimeZone(theValue);
					setPrecision(TemporalPrecisionEnum.MILLI);
				} else {
					try {
						if (hasOffset(theValue)) {
							retVal = ourYearMonthDayTimeZoneFormat.parse(theValue);
						} else if (theValue.endsWith("Z")) {
							retVal = ourYearMonthDayTimeUTCZFormat.parse(theValue);
						} else {
							retVal = ourYearMonthDayTimeFormat.parse(theValue);
						}
					} catch (ParseException p2) {
						throw new DataFormatException("Invalid data/time string (" + p2.getMessage() + "): " + theValue);
					}

					setTimeZone(theValue);
					setPrecision(TemporalPrecisionEnum.SECOND);
				}

				return retVal;
			} else {
				throw new DataFormatException("Invalid date/time string (invalid length): " + theValue);
			}
		} catch (ParseException e) {
			throw new DataFormatException("Invalid date string (" + e.getMessage() + "): " + theValue);
		}
	}

	/**
	 * Sets the precision for this datatype
	 * 
	 * @throws DataFormatException
	 */
	public void setPrecision(TemporalPrecisionEnum thePrecision) throws DataFormatException {
		if (thePrecision == null) {
			throw new NullPointerException("Precision may not be null");
		}
		myPrecision = thePrecision;
		updateStringValue();
	}

	private int getOffsetIndex(String theValueString) {
		int plusIndex = theValueString.indexOf('+', 19);
		int minusIndex = theValueString.indexOf('-', 19);
		int zIndex = theValueString.indexOf('Z', 19);
		int retVal = Math.max(Math.max(plusIndex, minusIndex), zIndex);
		if (retVal == -1) {
			return -1;
		}
		if ((retVal - 2) != (plusIndex + minusIndex + zIndex)) {
			// This means we have more than one separator
			throw new DataFormatException("Invalid FHIR date/time string: " + theValueString);
		}
		return retVal;
	}

	private BaseDateTimeDt setTimeZone(String theValueString) {
		clearTimeZone();
		
		int sepIndex = getOffsetIndex(theValueString);
		if (sepIndex != -1) {
			if (theValueString.charAt(sepIndex) == 'Z') {
				setTimeZoneZulu(true);
			} else {
				String offsetString = theValueString.substring(sepIndex);
				setTimeZone(TimeZone.getTimeZone("GMT" + offsetString));
			}
		}

		return this;
	}

	public BaseDateTimeDt setTimeZone(TimeZone theTimeZone) {
		myTimeZone = theTimeZone;
		updateStringValue();
		return this;
	}

	public BaseDateTimeDt setTimeZoneZulu(boolean theTimeZoneZulu) {
		myTimeZoneZulu = theTimeZoneZulu;
		updateStringValue();
		return this;
	}

	/**
	 * Sets the value for this type using the given Java Date object as the time, and using the default precision for
	 * this datatype, as well as the local timezone as determined by the local operating
	 * system. Both of these properties may be modified in subsequent calls if neccesary.
	 */
	@Override
	public BaseDateTimeDt setValue(Date theValue) {
		setValue(theValue, getDefaultPrecisionForDatatype());
		return this;
	}

	/**
	 * Sets the value for this type using the given Java Date object as the time, and using the specified precision, as
	 * well as the local timezone as determined by the local operating system. Both of
	 * these properties may be modified in subsequent calls if neccesary.
	 * 
	 * @param theValue
	 *           The date value
	 * @param thePrecision
	 *           The precision
	 * @throws DataFormatException
	 */
	public void setValue(Date theValue, TemporalPrecisionEnum thePrecision) throws DataFormatException {
		setTimeZone(TimeZone.getDefault());
		myPrecision = thePrecision;
		super.setValue(theValue);
	}

	@Override
	public void setValueAsString(String theValue) throws DataFormatException {
		clearTimeZone();
		super.setValueAsString(theValue);
	}

	/**
	 * Returns a human readable version of this date/time using the system local format.
	 * <p>
	 * <b>Note on time zones:</b> This method renders the value using the time zone that is contained within the value.
	 * For example, if this date object contains the value "2012-01-05T12:00:00-08:00",
	 * the human display will be rendered as "12:00:00" even if the application is being executed on a system in a
	 * different time zone. If this behaviour is not what you want, use
	 * {@link #toHumanDisplayLocalTimezone()} instead.
	 * </p>
	 */
	public String toHumanDisplay() {
		TimeZone tz = getTimeZone();
		Calendar value = tz != null ? Calendar.getInstance(tz) : Calendar.getInstance();
		value.setTime(getValue());

		switch (getPrecision()) {
		case YEAR:
		case MONTH:
		case DAY:
			return ourHumanDateFormat.format(value);
		case MILLI:
		case SECOND:
		default:
			return ourHumanDateTimeFormat.format(value);
		}
	}

	/**
	 * Returns a human readable version of this date/time using the system local format, converted to the local timezone
	 * if neccesary.
	 * 
	 * @see #toHumanDisplay() for a method which does not convert the time to the local timezone before rendering it.
	 */
	public String toHumanDisplayLocalTimezone() {
		switch (getPrecision()) {
		case YEAR:
		case MONTH:
		case DAY:
			return ourHumanDateFormat.format(getValue());
		case MILLI:
		case SECOND:
		default:
			return ourHumanDateTimeFormat.format(getValue());
		}
	}

	/**
	 * For unit tests only
	 */
	static List<FastDateFormat> getFormatters() {
		return ourFormatters;
	}

}
