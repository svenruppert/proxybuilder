/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.generated.v008;


import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticLoggingProxy;
import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticMetricsProxy;

import java.util.List;


@StaticLoggingProxy
@StaticMetricsProxy
public interface MyLoggingInterface {

  <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException;

  <T extends List> T unwrapList(T type);
  <T extends List> T unwrapList(T type, String str);


//  <X extends List>  void unwrapVoid(java.lang.Class<X> iface) throws java.sql.SQLException;


}