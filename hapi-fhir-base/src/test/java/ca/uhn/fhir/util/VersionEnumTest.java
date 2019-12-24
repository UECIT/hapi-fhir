package ca.uhn.fhir.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class VersionEnumTest {

	@Test
	public void testCurrentVersionExists() {
		List<String> versions = Arrays.stream(VersionEnum.values())
			.map(Enum::name)
			.collect(Collectors.toList());

		String version = VersionUtil.getVersion();
		version = version.replace('.', '_');
		version = version.replace('-', '_');
		version = version.replace("-SNAPSHOT", "");
		version = "V" + version.toUpperCase();

		assertThat(versions, hasItem(version));
	}


}
