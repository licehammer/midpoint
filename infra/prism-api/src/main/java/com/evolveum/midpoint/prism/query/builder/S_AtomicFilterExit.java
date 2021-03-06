/*
 * Copyright (c) 2010-2015 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.query.builder;

/**
 * @author mederly
 */
public interface S_AtomicFilterExit extends S_FilterExit {
    S_FilterEntry or() ;
    S_FilterEntry and() ;
}
