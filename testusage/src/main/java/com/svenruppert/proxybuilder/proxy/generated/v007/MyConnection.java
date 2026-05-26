/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.generated.v007;


import com.svenruppert.proxybuilder.proxy.generated.annotations.StaticMetricsProxy;

import java.util.List;




@StaticMetricsProxy
public interface MyConnection {

  <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException;

  <T extends List> T unwrapList(T type);


//  default String getDatabaseProductName() {
//    try {
//      return getMetaData().getDatabaseProductName() == null ? "" : getMetaData().getDatabaseProductName().toLowerCase();
//    } catch (SQLException e) {
//      return "";
//    }
//  }


}