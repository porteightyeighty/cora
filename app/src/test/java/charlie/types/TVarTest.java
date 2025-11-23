/**************************************************************************************************
 Copyright 2025 Cynthia Kop

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

import charlie.util.NullStorageException;

class TVarTest {
  @Test
  void testBasics() {
    Type t = new TVar("β");
    assertFalse(t.isBaseType());
    assertFalse(t.isArrowType());
    assertFalse(t.isDataType());
    assertTrue(t.isVariableType());
    assertFalse(t.isSort());
    assertFalse(t.isSimple());
    assertFalse(t.isMonomorphic());
    assertFalse(t.isTheoryType());
    assertFalse(t.isZeroSort());
    assertTrue(t.equals(new TVar("β")));
    assertFalse(t.equals(new TVar("δ")));
    assertFalse(t.equals("$β"));
    assertTrue(t.toString().equals("$β"));
    assertTrue(t.queryArity() == 0);
    assertTrue(t.queryNumberSubtypes() == 0);
    assertTrue(t.queryOutputType() == t);
    assertTrue(t.querySimpleTypeOrder() == 0);
    assertTrue(t.queryFullTypeOrder() == Integer.MAX_VALUE);
  }

  @Test
  public void testNullCreation() {
    assertThrows(NullStorageException.class, () -> new TVar(null));
  }

  @Test
  public void testHashCode() {
    TVar a = new TVar("alpha");
    TVar b = new TVar("alpha");
    assertTrue(a.hashCode() == b.hashCode());
  }
}

