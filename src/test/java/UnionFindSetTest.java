import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import junit.framework.TestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.*;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Suite.class)
@Suite.SuiteClasses({UnionFindSetTest.GuavaTests.class, UnionFindSetTest.AdditionalTests.class,})
public class UnionFindSetTest {

	public static class GuavaTests {
		public static TestSuite suite() {
			return SetTestSuiteBuilder.using(new TestStringSetGenerator() {

				@Override
				protected Set<String> create(String[] elements) {
					return new UnionFindSet<>(asList(elements));
				}
			}).named("UnionFindSet tests").withFeatures(CollectionSize.ANY, CollectionFeature.ALLOWS_NULL_QUERIES,
				CollectionFeature.ALLOWS_NULL_VALUES, CollectionFeature.NON_STANDARD_TOSTRING,
				CollectionFeature.SUPPORTS_ADD).createTestSuite();
		}
	}

	public static class AdditionalTests {

		@Test(expected = IllegalArgumentException.class)
		public void intConstructorShouldThrowIAEIfInitialCapacityIsLessThanZero() {
			new UnionFindSet<>(-1);
		}

		@Test(expected = IllegalArgumentException.class)
		public void intFloatConstructorShouldThrowIAEIfInitialCapacityIsLessThanZero() {
			new UnionFindSet<>(-1, 0.75F);
		}

		@Test(expected = IllegalArgumentException.class)
		public void intFloatConstructorShouldThrowIAEIfLoadFactorIsNonPositive() {
			new UnionFindSet<>(16, 0.0F);
		}

		@Test(expected = NullPointerException.class)
		public void collectionConstructorShouldThrowNPEIfCIsNull() {
			new UnionFindSet<>(null);
		}

		@Test(expected = UnsupportedOperationException.class)
		public void removeIfShouldThrowUnsupportedMethodException() {
			new UnionFindSet<Integer>().removeIf((Integer i) -> false);
		}

		@Test
		public void areEquivalentShouldReturnFalseForElementsThatHaveNotBeenJoined() {
			UnionFindSet<Integer> set = new UnionFindSet<>(asList(0, 1));

			assertThat(set.areEquivalent(0, 1), is(false));
		}

		@Test
		public void areEquivalentShouldReturnTrueForElementsThatHaveBeenJoinedDirectly() {
			UnionFindSet<Integer> set = new UnionFindSet<>(asList(0, 1));

			set.join(0, 1);

			assertThat(set.areEquivalent(0, 1), is(true));
			assertThat(set.areEquivalent(1, 0), is(true));
		}

		@Test
		public void areEquivalentShouldReturnFalseForValuesNotInTheSet() {
			UnionFindSet<Integer> set = new UnionFindSet<>(singletonList(1));

			assertThat(set.areEquivalent(0, 1), is(false));
			assertThat(set.areEquivalent(1, 2), is(false));
			assertThat(set.areEquivalent(0, 2), is(false));
		}

		@Test
		public void areEquivalentShouldReturnTrueForElementsThatHaveBeenJoinedTransitively() {
			UnionFindSet<Integer> set = new UnionFindSet<>(asList(0, 1, 2));

			set.join(0, 1);
			set.join(1, 2);

			assertThat(set.areEquivalent(0, 2), is(true));
			assertThat(set.areEquivalent(2, 0), is(true));
		}

		@Test
		public void whenPassedAValueThatIsNotInTheSetJoinShouldAddTheValueAndReturnTrue() {
			UnionFindSet<Integer> set = new UnionFindSet<>(singletonList(1));

			assertThat(set.join(0, 1), is(true));
			assertThat(set.join(1, 2), is(true));
			assertThat(set.join(3, 4), is(true));
			assertThat(set.contains(0), is(true));
			assertThat(set.contains(2), is(true));
			assertThat(set.contains(3), is(true));
			assertThat(set.contains(4), is(true));
		}

		@Test
		public void whenPassedElementsFromDifferentEquivalenceClassesJoinShouldReturnTrue() {
			UnionFindSet<Integer> set = new UnionFindSet<>(asList(0, 1));

			assertThat(set.join(0, 1), is(true));
		}

		@Test
		public void whenPassedElementsFromTheSameEquivalenceClassJoinShouldReturnFalse() {
			UnionFindSet<Integer> set = new UnionFindSet<>(asList(0, 1));

			assertThat(set.join(0, 1), is(true));
			assertThat(set.join(0, 1), is(false));
		}

		@Test
		public void whenPassedTheSameElementTwiceJoinShouldReturnFalse() {
			UnionFindSet<Integer> set = new UnionFindSet<>(singletonList(0));

			assertThat(set.join(0, 0), is(false));
		}

		@Test
		public void whenPassedAValueThatIsNotInTheSetJoinIfPresentShouldDoNothingAndReturnFalse() {
			UnionFindSet<Integer> set = new UnionFindSet<>(singletonList(1));

			assertThat(set.joinIfPresent(0, 1), is(false));
			assertThat(set.joinIfPresent(1, 2), is(false));
			assertThat(set.joinIfPresent(3, 4), is(false));
			assertThat(set.contains(0), is(false));
			assertThat(set.contains(2), is(false));
			assertThat(set.contains(3), is(false));
			assertThat(set.contains(4), is(false));
		}

		@Test
		public void whenPassedValuesFromDifferentEquivalenceClassesJoinIfPresentShouldReturnTrue() {
			UnionFindSet<Integer> set = new UnionFindSet<>(asList(0, 1));

			assertThat(set.joinIfPresent(0, 1), is(true));
			assertThat(set.areEquivalent(0, 1), is(true));
			assertThat(set.areEquivalent(1, 0), is(true));
		}

		@Test
		public void getEquivalenceClassShouldReturnEmptyOptionalIfTIsNotAnElement() {
			UnionFindSet<Integer> set = new UnionFindSet<>(emptyList());
			Optional<Set<Integer>> optional = set.getEquivalenceClass(1);

			assertThat(optional.isPresent(), is(false));
		}

		@Test
		public void getEquivalenceClassShouldEquivalenceClassOfT() {
			UnionFindSet<Integer> set = new UnionFindSet<>(asList(0, 1, 2, 3));
			set.join(0, 1);
			set.join(1, 2);
			Optional<Set<Integer>> optional = set.getEquivalenceClass(1);

			assertThat(optional.isPresent(), is(true));
			assertThat(optional.get(), is(newHashSet(0, 1, 2)));
		}

		@Test
		public void getEquivalenceClassesShouldReturnCollectionOfAllEquivalenceClasses() {
			UnionFindSet<Integer> set = new UnionFindSet<>(asList(0, 1, 2, 3));
			set.join(0, 1);
			set.join(2, 3);
			Collection<Set<Integer>> equivalenceClasses = set.getEquivalenceClasses();

			// The actual collection implementation being returned doesn't override equals, and therefor needs to be
			// wrapped in an implementation that does.
			//noinspection unchecked
			assertThat(newHashSet(equivalenceClasses), is(newHashSet(newHashSet(0, 1), newHashSet(2, 3))));
		}
	}
}