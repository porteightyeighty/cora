/**************************************************************************************************
 Copyright 2026 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

package charlie.types;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

class TVarSetTest {
  @Test
  public void testEmpty() {
    TVarSet set = new TVarSet();
    assertTrue(set.size() == 0);
    assertFalse(set.contains(new TVar("alpha")));
  }

  @Test
  public void testSingular() {
    TVarSet set = new TVarSet(new TVar("alpha"));
    assertTrue(set.size() == 1);
    assertTrue(set.contains(new TVar("alpha")));
    assertFalse(set.contains(new TVar("beta")));
    int tmp = 0;
    for (TVar x : set) {
      tmp++;
      assertTrue(x.equals(new TVar("alpha")));
    }
    assertTrue(tmp == 1);
  }

  @Test
  public void testMultiple() {
    TVarSet set = new TVarSet(Set.of(new TVar("alpha"), new TVar("gamma")));
    assertTrue(set.size() == 2);
    assertTrue(set.contains(new TVar("alpha")));
    assertFalse(set.contains(new TVar("beta")));
    assertTrue(set.contains(new TVar("gamma")));
  }

  @Test
  public void testAddOne() {
    TVarSet set = new TVarSet(new TVar("alpha"));
    TVarSet newset =
      set.add(TypeFactory.createArrow(TypeFactory.createSort("base"), new TVar("beta")));
    assertTrue(set.size() == 1);
    assertTrue(set.contains(new TVar("alpha")));
    assertFalse(set.contains(new TVar("beta")));
    assertTrue(newset.size() == 2);
    assertTrue(newset.contains(new TVar("alpha")));
    assertTrue(newset.contains(new TVar("beta")));
  }

  @Test
  public void testAddNothingNew() {
    TVarSet set = new TVarSet(new TVar("alpha"));
    TVarSet newset = set.add(TypeFactory.createSort("c", TypeFactory.createSort("base"),
                                        new TVar("alpha"), TypeFactory.createSort("q")));
    assertTrue(set == newset);
    assertTrue(set.size() == 1);
  }

  @Test
  public void testAddTwoNewAndOneOld() {
    TVarSet set = new TVarSet(Set.of(new TVar("a"), new TVar("b")));
    TVarSet newset = set.add(TypeFactory.createSort("C", new TVar("c"), new TVar("a"),
                                                    TypeFactory.createSort("D", new TVar("d"))));
    assertTrue(set.size() == 2);
    assertTrue(newset.size() == 4);
  }

  @Test
  public void testCombine() {
    TVarSet set1 = new TVarSet(Set.of(new TVar("a"), new TVar("b")));
    TVarSet set2 = new TVarSet(Set.of(new TVar("d"), new TVar("c"), new TVar("a")));
    TVarSet set3 = new TVarSet(Set.of(new TVar("a")));
    TVarSet set4 = new TVarSet(Set.of(new TVar("b"), new TVar("c"), new TVar("a")));
    assertTrue(set1.combine(set2).size() == 4);
    assertTrue(set2.combine(set1).size() == 4);
    assertTrue(set1.combine(set3) == set1);
    assertTrue(set1.combine(set4) == set4);
    assertTrue(set1.size() == 2);
    assertTrue(set2.size() == 3);
  }
}

