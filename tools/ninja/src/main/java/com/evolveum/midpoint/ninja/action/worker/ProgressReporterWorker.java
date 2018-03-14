/*
 * Copyright (c) 2010-2018 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.ninja.action.worker;

import com.evolveum.midpoint.ninja.impl.NinjaContext;
import com.evolveum.midpoint.ninja.opts.ImportOptions;
import com.evolveum.midpoint.ninja.util.NinjaUtils;
import com.evolveum.midpoint.ninja.util.OperationStatus;
import com.evolveum.midpoint.prism.PrismObject;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProgressReporterWorker extends BaseWorker<ImportOptions, PrismObject> {

    public ProgressReporterWorker(NinjaContext context, ImportOptions options, BlockingQueue<PrismObject> queue,
                                  OperationStatus operation) {
        super(context, options, queue, operation);
    }

    @Override
    public void run() {
        while (!shouldStop()) {
            if (operation.isStarted() || operation.isProducerFinished()) {
                operation.print(context.getLog());
            }

            try {
                Thread.sleep(NinjaUtils.COUNT_STATUS_LOG_INTERVAL);
            } catch (InterruptedException ex) {
            }
        }
    }

    private boolean shouldStop() {
        if (operation.isFinished()) {
            return true;
        }

        if (operation.isStarted()) {
            return false;
        }

        if (operation.isProducerFinished() && !queue.isEmpty()) {
            return false;
        }

        return true;
    }
}