package org.yarnandtail.andhow.load;

import org.yarnandtail.andhow.AndHow;
import org.yarnandtail.andhow.AppFatalException;
import org.yarnandtail.andhow.PropertyGroup;
import java.util.List;
import org.junit.Test;
import org.yarnandtail.andhow.*;

import static org.junit.Assert.*;

import org.yarnandtail.andhow.internal.ConstructionProblem.LoaderPropertyNotRegistered;
import org.yarnandtail.andhow.internal.LoaderProblem.SourceNotFoundLoaderProblem;
import org.yarnandtail.andhow.name.BasicNamingStrategy;
import org.yarnandtail.andhow.property.StrProp;

import static org.yarnandtail.andhow.AndHowTestBase.reloader;

/**
 * Just like the unit test version, but builds an entire AppConfig instance so
 * some of the higher-level errors can be tested
 * @author eeverman
 */
public class PropertyFileFromClasspathLoaderAppTest {
	
	public static interface TestProps extends PropertyGroup {
		StrProp CLAZZ_PATH = StrProp.builder().build();
	}
	
	@Test
	public void testHappyPath() throws Exception {
		AndHow.builder().namingStrategy(new BasicNamingStrategy())
				.loader(new CmdLineLoader())
				.loader(new PropertyFileFromClasspathLoader(TestProps.CLAZZ_PATH))
				.cmdLineArg(PropertyGroup.getCanonicalName(TestProps.class, TestProps.CLAZZ_PATH), 
						"/org/yarnandtail/andhow/load/SimpleParams1.properties")
				.group(SimpleParams.class)
				.group(TestProps.class)
				.reloadForNonPropduction(reloader);
		

		assertEquals("/org/yarnandtail/andhow/load/SimpleParams1.properties", TestProps.CLAZZ_PATH.getValue());
		assertEquals("kvpBobValue", SimpleParams.STR_BOB.getValue());
		assertEquals("kvpNullValue", SimpleParams.STR_NULL.getValue());
		assertEquals(Boolean.FALSE, SimpleParams.FLAG_TRUE.getValue());
		assertEquals(Boolean.TRUE, SimpleParams.FLAG_FALSE.getValue());
		assertEquals(Boolean.TRUE, SimpleParams.FLAG_NULL.getValue());
	}
	
	@Test
	public void testUnregisteredPropLoaderProperty() throws Exception {
		
		try {
			AndHow.builder().namingStrategy(new BasicNamingStrategy())
					.loader(new CmdLineLoader())
					.loader(new PropertyFileFromClasspathLoader(TestProps.CLAZZ_PATH))
					.cmdLineArg(PropertyGroup.getCanonicalName(TestProps.class, TestProps.CLAZZ_PATH), 
							"/org/yarnandtail/andhow/load/SimpleParams1.properties")
					.group(SimpleParams.class)
					//.group(TestProps.class)	//This must be declared or the Prop loader can't work
					.reloadForNonPropduction(reloader);
		
			fail("The Property loader config parameter is not registered, so it should have failed");
		} catch (AppFatalException afe) {
			List<LoaderPropertyNotRegistered> probs = afe.getProblems().filter(LoaderPropertyNotRegistered.class);
			assertEquals(1, probs.size());
		}

	}
	
	/**
	 * It is not an error to not specify the classpath param, it just means the laoder
	 * will not find anything.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void testUnspecifiedConfigParam() throws Exception {
		AndHow.builder().namingStrategy(new BasicNamingStrategy())
				.loader(new CmdLineLoader())
				.loader(new PropertyFileFromClasspathLoader(TestProps.CLAZZ_PATH))
				.group(SimpleParams.class)
				.group(TestProps.class)
				.reloadForNonPropduction(reloader);
		
		//These are just default values
		assertEquals("bob", SimpleParams.STR_BOB.getValue());
		assertNull(SimpleParams.STR_NULL.getValue());
	}
	
	@Test
	public void testABadClasspathThatDoesNotPointToAFile() throws Exception {
		
		try {
			AndHow.builder().namingStrategy(new BasicNamingStrategy())
					.loader(new CmdLineLoader())
					.loader(new PropertyFileFromClasspathLoader(TestProps.CLAZZ_PATH))
					.cmdLineArg(PropertyGroup.getCanonicalName(TestProps.class, TestProps.CLAZZ_PATH), 
							"asdfasdfasdf/asdfasdf/asdf")
					.group(SimpleParams.class)
					.group(TestProps.class)
					.reloadForNonPropduction(reloader);
			
			fail("The Property loader config property is not pointing to a real file location");
			
		} catch (AppFatalException afe) {
			List<SourceNotFoundLoaderProblem> probs = afe.getProblems().filter(SourceNotFoundLoaderProblem.class);
			assertEquals(1, probs.size());
		}
	}

}
