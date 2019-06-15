package org.rootbr.camunda.spin.gson;

import org.camunda.spin.json.SpinJsonNode;

public abstract class SpinJsonNodeAdapter extends SpinJsonNode {
  @Override
  public SpinJsonNode prop(String name, boolean newProperty) {
    return prop(name, new Boolean(newProperty));
  }
}
