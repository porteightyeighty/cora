/**************************************************************************************************
 Copyright 2019--2025 Cynthia Kop

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
import java.util.ArrayList;
import java.util.List;

import charlie.util.LookupMap;
import charlie.util.NullStorageException;
import charlie.types.*;
import charlie.terms.FunctionSymbol;
import charlie.terms.TermFactory;
import charlie.terms.TypingException;

public class AlphabetTest {
  private Type baseType(String name) {
    return TypeFactory.createSort(name);
  }

  private FunctionSymbol makeConstant(String name, String sort) {
    return TermFactory.createConstant(name, baseType(sort));
  }

  private FunctionSymbol makeSymbol(String name, Type type) {
    return TermFactory.createConstant(name, type);
  }

  @Test
  public void testBasics() {
    ArrayList<FunctionSymbol> symbols = new ArrayList<FunctionSymbol>();
    symbols.add(makeConstant("0", "Nat"));
    symbols.add(makeSymbol("S", TypeFactory.createArrow(baseType("Nat"), baseType("Nat"))));
    Alphabet a = new Alphabet(symbols);
    assertTrue(a.lookup("0").equals(symbols.get(0)));
    assertTrue(a.lookup("S").equals(symbols.get(1)));
    assertTrue(a.lookup("s") == null);  // it's case-sensitive!
  }

  @Test
  public void testAdd() {
    FunctionSymbol zero = makeConstant("0", "Nat");
    FunctionSymbol s = makeSymbol("S", TypeFactory.createArrow(baseType("Nat"), baseType("Nat")));
    Alphabet alf1 = new Alphabet(List.of(zero, s));
    FunctionSymbol a = makeConstant("a", "A");
    FunctionSymbol b = makeConstant("b", "A");
    FunctionSymbol c = makeConstant("a", "C");
    FunctionSymbol d = makeConstant("0", "C");
    Alphabet alf2 = alf1.add(List.of(a, s, b));
    assertTrue(alf1.lookup("0") == zero);
    assertTrue(alf1.lookup("S") == s);
    assertTrue(alf1.lookup("a") == null);
    assertTrue(alf1.lookup("b") == null);
    assertTrue(alf2.lookup("0") == zero);
    assertTrue(alf2.lookup("S") == s);
    assertTrue(alf2.lookup("a") == a);
    assertTrue(alf2.lookup("b") == b);
    alf1.add(List.of(b, c));  // no problem here
    assertThrows(TypingException.class, () -> alf2.add(List.of(b, c)));
    assertThrows(TypingException.class, () -> alf1.add(List.of(d)));
  }

  @Test
  public void testDuplicateAcceptable() {
    ArrayList<FunctionSymbol> symbols = new ArrayList<FunctionSymbol>();
    symbols.add(makeConstant("0", "Nat"));
    symbols.add(makeSymbol("S", TypeFactory.createArrow(baseType("Nat"), baseType("Nat"))));
    symbols.add(makeConstant("0", "Nat"));
    symbols.add(makeConstant("0", "Nat"));
    Alphabet a = new Alphabet(symbols);
    assertTrue(a.lookup("0").equals(symbols.get(0)));
    assertTrue(a.lookup("S").equals(symbols.get(1)));
  }

  @Test
  public void testAlphabetNullInitialisation() {
    LookupMap<FunctionSymbol> map1 = null;
    ArrayList<FunctionSymbol> map2 = null;
    LookupMap<FunctionSymbol> map3 = LookupMap.empty();
    LookupMap<Integer> sorts1 = null;
    LookupMap<Integer> sorts2 = LookupMap.empty();
    assertThrows(NullStorageException.class, () -> new Alphabet(map1));
    assertThrows(NullPointerException.class, () -> new Alphabet(map2));
    assertThrows(NullStorageException.class, () -> new Alphabet(sorts1, map3));
    assertThrows(NullStorageException.class, () -> new Alphabet(sorts2, map1));
  }

  @Test
  public void testAlphabetNullSymbolInitialisation() {
    ArrayList<FunctionSymbol> symbols = new ArrayList<FunctionSymbol>();
    symbols.add(makeConstant("0", "Nat"));
    symbols.add(null);
    symbols.add(makeSymbol("S", TypeFactory.createArrow(baseType("Nat"), baseType("Nat"))));
    assertThrows(NullStorageException.class, () ->new Alphabet(symbols));
  }

  @Test
  public void testUnacceptableDuplicate() {
    ArrayList<FunctionSymbol> symbols = new ArrayList<FunctionSymbol>();
    symbols.add(makeConstant("0", "Nat"));
    symbols.add(makeSymbol("S", TypeFactory.createArrow(baseType("Nat"), baseType("Nat"))));
    symbols.add(makeSymbol("S", TypeFactory.createArrow(baseType("Nat"), baseType("nat"))));
      // Nat vs nat
    assertThrows(TypingException.class, () -> new Alphabet(symbols));
  }

  // creates f :: d(Int) → c(a, $β) → c($β, list) -> list
  private FunctionSymbol makeSymbolWithComplexType() {
    Type list = baseType("list");
    Type cblist = TypeFactory.createSort("c", TypeFactory.createVariable("β"), list);
    Type cab = TypeFactory.createSort("c", baseType("a"), TypeFactory.createVariable("β"));
    Type dint = TypeFactory.createSort("d", TypeFactory.intSort);
    Type type = TypeFactory.createArrow(dint, TypeFactory.createArrow(cab,
      TypeFactory.createArrow(cblist, list)));
    return makeSymbol("f", type);
  }

  @Test
  public void testDeduceSortConstructors() {
    ArrayList<FunctionSymbol> symbols = new ArrayList<FunctionSymbol>();
    symbols.add(makeSymbolWithComplexType());
    symbols.add(makeSymbol("test", baseType("Bool"))); // not a theory type!
    Alphabet alf = new Alphabet(symbols);
    assertTrue(alf.querySortConstructorArity("d") == 1);
    assertTrue(alf.querySortConstructorArity("Int") == -1);
    assertTrue(alf.querySortConstructorArity("c") == 2);
    assertTrue(alf.querySortConstructorArity("a") == 0);
    assertTrue(alf.querySortConstructorArity("list") == 0);
    assertTrue(alf.querySortConstructorArity("Bool") == 0);
  }

  @Test
  public void testInconsistentSortConstructors() {
    // d(Int) → c(a, $β) → c($β, list) -> list
    LookupMap.Builder<FunctionSymbol> builder = new LookupMap.Builder<FunctionSymbol>();
    builder.put("f", makeSymbolWithComplexType());
    builder.put("g", makeSymbol("g", TypeFactory.createSort("c", TypeFactory.intSort))); // c(Int)
    LookupMap<FunctionSymbol> map = builder.build();
    assertThrows(InconsistentSortException.class, () -> new Alphabet(map));
  }
}

