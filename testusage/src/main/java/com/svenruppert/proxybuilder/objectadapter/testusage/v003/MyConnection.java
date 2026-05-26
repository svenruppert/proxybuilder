/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the EUPL-1.2.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package com.svenruppert.proxybuilder.objectadapter.testusage.v003;


import com.svenruppert.proxybuilder.objectadapter.dynamic.DynamicObjectAdapterBuilder;
import com.svenruppert.proxybuilder.objectadapter.generated.StaticObjectAdapter;

import java.sql.Connection;


@DynamicObjectAdapterBuilder
@StaticObjectAdapter
public interface MyConnection extends Connection {
  String doWorkA();
}