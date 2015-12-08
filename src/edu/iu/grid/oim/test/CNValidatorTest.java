package edu.iu.grid.oim.test;

import static org.junit.Assert.*;
/**
 * MT
 * Unit Test Cases for CNValidator
 */

import org.junit.Test;
import edu.iu.grid.oim.view.divrep.form.validator.CNValidator;

public class CNValidatorTest {

	@Test
	public final void testIsValidCertificate() {
		CNValidator cnv = new CNValidator(CNValidator.Type.HOST);
		
		// List of valid certificates
		String[] validCertNames = {"iu.edu", "*.edu", "www.something.com"};
		
		for (String validCertName : validCertNames) 	{
			
			assertTrue("Certificate >" + validCertName + "< is valid", cnv.isValid(validCertName));	
		}
	}
	
	@Test
	public final void testIsInvalidCertificate() {
		CNValidator cnv = new CNValidator(CNValidator.Type.HOST);
		
		// List of invalid certificates
		String[] invalidCertNames = {".iu.edu", "/edu"};
		
		for (String invalidCertName : invalidCertNames) 	{
			
			assertFalse("Certificate >" + invalidCertName + "< is invalid", cnv.isValid(invalidCertName));	
		}
	}
}
