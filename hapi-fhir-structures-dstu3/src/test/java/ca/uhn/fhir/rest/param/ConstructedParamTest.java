package ca.uhn.fhir.rest.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.construction.BeanHandlerException;
import ca.uhn.fhir.rest.param.construction.OrderedField;
import ca.uhn.fhir.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Test;

public class ConstructedParamTest {

	private static FhirContext ourCtx = FhirContext.forDstu3();

	public static class TestBeanNoAnnotation {
		@SuppressWarnings("FieldCanBeLocal")
		private int someInt;

		public TestBeanNoAnnotation(int someInt) {
			this.someInt = someInt;
		}
	}
	public static class TestBean {
		@OrderedField(order = 2) private String someString;
		@OrderedField(order = 1) private int someInt;
		@OrderedField(order = 3) private TokenParam someToken;
		@OrderedField(order = 4) private TokenParam someOtherToken;
	}

	@Test
	public void testParse_bean() {
		String queryString = "1337$leet$man|spider$innovation";

		ConstructedParam<TestBean> params = new ConstructedParam<>(TestBean.class);
		params.setValueAsQueryToken(ourCtx, null, null, queryString);

		assertEquals(queryString, params.getValueAsQueryToken(ourCtx));
		assertEquals(1337, params.getValue().someInt);
		assertEquals("leet", params.getValue().someString);
		assertEquals("man", params.getValue().someToken.getSystem());
		assertEquals("spider", params.getValue().someToken.getValue());
		assertNull(params.getValue().someOtherToken.getSystem());
		assertEquals("innovation", params.getValue().someOtherToken.getValue());
	}

	@Test(expected = BeanHandlerException.class)
	public void testParse_throws() {
		String queryString = "1337";

		ConstructedParam<TestBeanNoAnnotation> params = new ConstructedParam<>(TestBeanNoAnnotation.class);
		params.setValueAsQueryToken(ourCtx, null, null, queryString);

		params.getValueAsQueryToken(ourCtx);
	}

	@AfterClass
	public static void afterClassClearContext() {
		TestUtil.clearAllStaticFieldsForUnitTest();
	}
}
