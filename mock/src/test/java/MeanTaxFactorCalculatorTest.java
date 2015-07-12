import common.person.Person;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Matchers.any;

public class MeanTaxFactorCalculatorTest {

    static final double TAX_FACTOR = 10;

    @Mock TaxService taxService;
    @InjectMocks MeanTaxFactorCalculator systemUnderTest;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_calculate_mean_tax_factor() {

        // given
        given(taxService.getCurrentTaxFactorFor(any(Person.class))).willReturn(TAX_FACTOR);

        // when
        double meanTaxFactor = systemUnderTest.calculateMeanTaxFactorFor(new Person());

        // then
	    assertThat(meanTaxFactor, equalTo(TAX_FACTOR));
    }

}
