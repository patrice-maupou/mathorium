/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * pour ne pas utiliser Vector
 */
public class TreeEnum implements Enumeration<Schema> {
  private final Iterator<Schema> it;

  public TreeEnum(ArrayList<Schema> list) {
    it = list.iterator();
  }

  @Override
  public boolean hasMoreElements() {
    return it.hasNext();
  }

  @Override
  public Schema nextElement() {
    return it.next();
  }
  
}
