/*
 * Copyright (C) 2015 Patrice.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.maupou.expressions;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Patrice
 */
public class ExprTest {
  
  public ExprTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of copy method, of class Expr.
   */
  @Ignore
  @Test
  public void testCopy() {
    System.out.println("copy");
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of scanExpr method, of class Expr.
   */
  //*
  @Test
  public void testScanExpr() {
    System.out.println("scanExpr");
    /*
    String[] txts = new String[] {
      "(FUNC:func,(FUNC:func,A:type,B:type):type,(FUNC:func,C:type,D:type):type):type",
      "(3:N", "3:N", 
      "(ADD:func,3:N,x:R):R", "(((f:func,x:term):func,b:term):func,c:term):term", 
      "a:Z", "(a:Z)", "ADD:func,3:N,x:R)", "(ADD,3,x)"
    };
    String[] expResults = new String[] {
      "(FUNC,(FUNC,A,B),(FUNC,C,D))",
      "[]", "[3]", 
      "[(ADD,3,x)]", "[(((f,x),b),c)]", 
      "[a]", "[]", "[]", "[]"
    };
    for (int i = 0; i < expResults.length; i++) {
      String expResult = expResults[i];
      List<Expr> result = Expr.scanExpr( txts[i], true);
      assertEquals(expResult, result.toString());
      if(result.isEmpty()) {
        System.out.println(txts[i] + " : expression mal formée");
      }
      else {
        System.out.println(result.toString());
      }
    }
    //*/
  }

  /**
   * Test of toText method, of class Expr.
   */
  @Ignore
  @Test
  public void testToText() {
    System.out.println("toText");
    Expr instance = null;
    String expResult = "";
    String result = instance.toText();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class Expr.
   */
  @Ignore
  @Test
  public void testToString() {
    System.out.println("toString");
    Expr instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

 
  
}
