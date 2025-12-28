/**************************************************************************************************
 Copyright 2024 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

package charlie.trs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import charlie.trs.TrsProperties.*;

public class RuleRestrictionsTest {
  @Test
  public void testBasicCreate() {
    RuleRestrictions rest = new RuleRestrictions(Level.APPLICATIVE, Constrained.YES,
                       TypeLevel.MONOMORPHIC, Lhs.NONPATTERN, Root.ANY, FreshRight.CVARS);
    assertTrue(rest.queryLevel() == Level.APPLICATIVE);
    assertTrue(rest.theoriesUsed());
    assertTrue(rest.queryTypes() == TypeLevel.MONOMORPHIC);
    assertTrue(rest.patternStatus() == Lhs.NONPATTERN);
    assertTrue(rest.rootStatus() == Root.ANY);
    assertTrue(rest.rightReplaceablePolicy() == FreshRight.CVARS);
  }

  @Test
  public void testCovers() {
    RuleRestrictions nothing = new RuleRestrictions(Level.FIRSTORDER, Constrained.YES,
                                                    TypeLevel.SIMPLE, Lhs.PATTERN,
                                                    Root.FUNCTION, FreshRight.NONE);
    RuleRestrictions anything = new RuleRestrictions(Level.META, Constrained.YES,
                                                     TypeLevel.POLYMORPHIC, Lhs.NONPATTERN,
                                                     Root.ANY, FreshRight.ANY);
    assertTrue(nothing.checkCoverage(nothing) == null);
    assertTrue(nothing.checkCoverage(anything).equals(
      "the rule level is limited to first-order terms, not meta-terms."));
    RuleRestrictions a = new RuleRestrictions(Level.APPLICATIVE, Constrained.YES,
      TypeLevel.MONOMORPHIC, Lhs.PATTERN, Root.THEORY, FreshRight.NONE);
    RuleRestrictions b = new RuleRestrictions(Level.LAMBDA, Constrained.NO,
      TypeLevel.MONOMORPHIC, Lhs.SEMIPATTERN, Root.THEORY, FreshRight.ANY);
    RuleRestrictions c = new RuleRestrictions(Level.APPLICATIVE, Constrained.YES,
      TypeLevel.SIMPLE, Lhs.SEMIPATTERN, Root.ANY, FreshRight.CVARS);
    RuleRestrictions d = new RuleRestrictions(Level.META, Constrained.YES,
      TypeLevel.POLYMORPHIC, Lhs.PATTERN, Root.ANY, FreshRight.ANY);
    RuleRestrictions e = new RuleRestrictions(Level.FIRSTORDER, Constrained.NO,
      TypeLevel.SIMPLE, Lhs.PATTERN, Root.FUNCTION, FreshRight.CVARS);
    RuleRestrictions f = new RuleRestrictions(Level.META, Constrained.YES, TypeLevel.SIMPLE,
      Lhs.SEMIPATTERN, Root.ANY, FreshRight.CVARS);
    RuleRestrictions g = new RuleRestrictions(Level.META, Constrained.YES, TypeLevel.SIMPLE,
      Lhs.SEMIPATTERN, Root.ANY, FreshRight.ANY);
    assertTrue(a.checkCoverage(b).equals(
      "the rule level is limited to applicative terms, not true terms."));
    assertTrue(a.checkCoverage(c).equals(
      "the left-hand side should have a function symbol as root, not anything else."));
    assertTrue(b.checkCoverage(a).equals(
      "the use of theory symbols / constraints is not supported."));
    assertTrue(c.checkCoverage(a).equals(
      "the use of data constructors (with non-zero arity) is not supported."));
    assertTrue(d.checkCoverage(b).equals(
      "the left-hand side should be a pattern, not a semi-pattern."));
    assertTrue(nothing.checkCoverage(e).equals(
      "the right-hand side contains a variable that does not occur in the left-hand side."));
    assertTrue(f.checkCoverage(d).equals(
      "the right-hand side contains a meta-variable that does not occur in the left-hand " +
      "side or the constraint."));
    assertTrue(g.checkCoverage(d).equals("the use of type variables is not supported."));
  }

  @Test
  public void testSupremum() {
    RuleRestrictions a = new RuleRestrictions(Level.APPLICATIVE, Constrained.NO,
                                              TypeLevel.MONOMORPHIC, Lhs.SEMIPATTERN, Root.ANY,
                                              FreshRight.NONE);
    RuleRestrictions b = new RuleRestrictions(Level.META, Constrained.YES, TypeLevel.POLYMORPHIC,
                                              Lhs.PATTERN, Root.THEORY, FreshRight.CVARS);
    // doing it from either side should result in the same
    RuleRestrictions c = a.supremum(b);
    RuleRestrictions d = b.supremum(a);
    assertTrue(c.queryLevel() == Level.META);
    assertTrue(d.queryLevel() == Level.META);
    assertTrue(c.theoriesUsed());
    assertTrue(d.theoriesUsed());
    assertTrue(c.queryTypes() == TypeLevel.POLYMORPHIC);
    assertTrue(d.queryTypes() == TypeLevel.POLYMORPHIC);
    assertTrue(c.patternStatus() == Lhs.SEMIPATTERN);
    assertTrue(d.patternStatus() == Lhs.SEMIPATTERN);
    assertTrue(c.rootStatus() == Root.ANY);
    assertTrue(d.rootStatus() == Root.ANY);
    assertTrue(c.rightReplaceablePolicy() == FreshRight.CVARS);
    assertTrue(d.rightReplaceablePolicy() == FreshRight.CVARS);
  }
}

