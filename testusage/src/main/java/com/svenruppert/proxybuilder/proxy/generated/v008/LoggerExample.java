/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.proxy.generated.v008;


import java.sql.SQLException;
import java.util.List;

public class LoggerExample implements MyLoggingInterface {


    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
      return null;
    }

    @Override
    public <T extends List> T unwrapList(final T type) {
      return null;
    }

    @Override
    public <T extends List> T unwrapList(final T type, final String str) {
      return null;
    }
  }