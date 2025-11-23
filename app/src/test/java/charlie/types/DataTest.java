/**************************************************************************************************
 Copyright 2023--2025 Cynthia Kop

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
import java.util.ArrayList;

import charlie.util.FixedList;
import charlie.util.NullStorageException;

class DataTest {
  @Test
  void testConstructedWithNull() {
    ArrayList<Type> args = new ArrayList<Type>();
    args.add(new Base("x"));
    args.add(new Base("y"));
    args.add(null);
    FixedList<Type> lst = null;
    assertThrows(NullStorageException.class, () -> new Data("c", lst));
    assertThrows(charlie.util.NullStorageException.class,
      () -> TypeFactory.createSort("c", args));
    assertThrows(charlie.util.NullStorageException.class,
      () -> new Data("c", FixedList.copy(args)));
    assertThrows(charlie.util.NullStorageException.class,
      () -> TypeFactory.createSort("c", FixedList.copy(args)));
    assertThrows(charlie.util.NullStorageException.class,
      () -> TypeFactory.createSort("c", null, new Base("z")));
    assertThrows(charlie.util.NullStorageException.class,
      () -> TypeFactory.createSort(null, new Base("z")));
    assertThrows(charlie.util.NullStorageException.class,
      () -> TypeFactory.createSort("c", new Base("z"), new Base("q"), null));
  }

  @Test
  public void testConstructWithEmptyArguments() {
    FixedList.Builder<Type> builder = new FixedList.Builder<Type>();
    FixedList<Type> l = builder.build();
    assertThrows(IllegalArgumentException.class, () -> new Data("constructor", l));
    Type t = TypeFactory.createSort("c", l);
    assertTrue(t instanceof Base);
  }

  @Test
  public void testBasics() {
    Type prod = TypeFactory.createSort("p", new Base("a"), new Arrow(new Base("b"), new Base("c")));
    Type prod2 = TypeFactory.createSort("c", new Base("a"), new TVar("γ"), new Base("c"));
    assertTrue(prod.isDataType());
    assertFalse(prod.isBaseType());
    assertFalse(prod2.isBaseType());
    assertFalse(prod.isArrowType());
    assertFalse(prod.isVariableType());
    assertTrue(prod.isSort());
    assertFalse(prod.isSimple());
    assertTrue(prod.isMonomorphic());
    assertFalse(prod2.isMonomorphic());
    assertFalse(prod.isTheoryType());
    assertTrue(prod.queryArity() == 0);
    assertTrue(prod.queryOutputType() == prod);
    assertTrue(prod.queryNumberSubtypes() == 2);
    assertTrue(prod2.queryNumberSubtypes() == 3);
    assertTrue(prod.querySubtype(1).equals(prod2.querySubtype(1)));
    assertTrue(prod.querySimpleTypeOrder() == 0);
    assertTrue(prod.queryFullTypeOrder() == 1);
    assertTrue(prod2.querySimpleTypeOrder() == 0);
    assertTrue(prod2.queryFullTypeOrder() == Integer.MAX_VALUE);
  }

  @Test
  public void testZeroSort() {
    Type a = TypeFactory.createSort("a", new Base("b"), TypeFactory.createSort("a", new Base("c")));
    assertTrue(a.isZeroSort());
    Type b = TypeFactory.createSort("a", new Base("b"), TypeFactory.createSort("a", new TVar("c")));
    assertFalse(b.isZeroSort());
    Type c = TypeFactory.createSort("a", TypeFactory.createArrow(new Base("a"), new Base("b")));
    assertFalse(c.isZeroSort());
  }

  @Test
  public void testTheory() {
    Type prod = TypeFactory.createSort("c", TypeFactory.intSort);
    assertFalse(prod.isTheoryType());
  }

  @Test
  public void testEquality() {
    Type a = new Base("a");
    Type b = new Base("b");
    Type c = new Base("c");
    // u(a, b, c)
    Type uabc = TypeFactory.createSort("u", a, b, c);
    // v(a, b, c)
    Type vabc = TypeFactory.createSort("v", a, b, c);
    // u(a, c, b)
    Type uacb = TypeFactory.createSort("u", a, c, b);
    // u(a, b)
    Type uab = TypeFactory.createSort("u", a, b);

    Object o = TypeFactory.createSort("u", a, b, c);
    assertTrue(uabc.equals(o));
    assertFalse(uabc.equals(vabc));
    assertFalse(uabc.equals(uacb));
    assertFalse(uabc.equals(uab));
  }

  @Test
  public void testToString() {
    Type a = new Base("a");
    Type b = new Base("b");
    Type c = new Base("c");
    Type d = new Base("d");
    // p(a, b, c)
    Type abc = TypeFactory.createSort("p", a, b, c);
    // pair(pair(a, b), other(d))
    Type abcd = TypeFactory.createSort("pair", TypeFactory.createSort("pair", a, b),
                                               TypeFactory.createSort("other", d));
    // xx(a -> b, c)
    Type aarrbc = TypeFactory.createSort("xx", new Arrow(a, b), c);
    // pair(a, b) -> c
    Type atimesbc = new Arrow(TypeFactory.createSort("pair", a, b), c);

    assertTrue(abc.toString().equals("p(a, b, c)"));
    assertTrue(abcd.toString().equals("pair(pair(a, b), other(d))"));
    assertTrue(aarrbc.toString().equals("xx(a → b, c)"));
    assertTrue(atimesbc.toString().equals("pair(a, b) → c"));
  }

  @Test
  public void testHashCode() {
    Type a = new Base("a");
    Type b = new Base("b");
    Type c = new Base("c");
    Type d = new Base("d");
    Type type1 = TypeFactory.createSort("x", a, new Arrow(b, c), d);
    Type type2 = TypeFactory.createSort("x", a, new Arrow(b, c), new Base("d"));
    assertTrue(type1.hashCode() == type2.hashCode());
  }
}
