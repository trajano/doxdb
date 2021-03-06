/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.trajano.doxdb.jsonpath.internal.token;

import java.util.List;

import net.trajano.doxdb.jsonpath.InvalidPathException;
import net.trajano.doxdb.jsonpath.PathNotFoundException;
import net.trajano.doxdb.jsonpath.internal.PathRef;
import net.trajano.doxdb.jsonpath.internal.Utils;

import static java.lang.String.format;

/**
 *
 */
public class ArrayPathToken extends PathToken {

    public static enum Operation {
        CONTEXT_SIZE,
        SLICE_TO,
        SLICE_FROM,
        SLICE_BETWEEN,
        INDEX_SEQUENCE,
        SINGLE_INDEX;
    }

    private final List<Integer> criteria;
    private final Operation operation;
    private final boolean isDefinite;

    public ArrayPathToken(List<Integer> criteria, Operation operation) {
        this.criteria = criteria;
        this.operation = operation;
        this.isDefinite = (Operation.SINGLE_INDEX == operation || Operation.CONTEXT_SIZE == operation);
    }

    @Override
    public void evaluate(String currentPath, PathRef parent, Object model, EvaluationContextImpl ctx) {
        if(model == null){
            throw new PathNotFoundException("The path " + currentPath + " is null");
        }
        if (!ctx.jsonProvider().isArray(model)) {
            throw new InvalidPathException(format("Filter: %s can only be applied to arrays. Current context is: %s", toString(), model));
        }

        try {
            int idx;
            int input;
            int length;
            int from;
            int to;

            switch (operation){
                case SINGLE_INDEX:
                    handleArrayIndex(criteria.get(0), currentPath, model, ctx);
                    break;

                case INDEX_SEQUENCE:
                    for (Integer i : criteria) {
                        handleArrayIndex(i, currentPath,  model, ctx);
                    }
                    break;

                case CONTEXT_SIZE:
                    length = ctx.jsonProvider().length(model);
                    idx = length + criteria.get(0);
                    handleArrayIndex(idx, currentPath, model, ctx);
                    break;

                case SLICE_FROM: //[2:]
                    input = criteria.get(0);
                    length = ctx.jsonProvider().length(model);
                    from = input;
                    if (from < 0) {
                        //calculate slice start from array length
                        from = length + from;
                    }
                    from = Math.max(0, from);

                    if (length == 0 || from >= length) {
                        return;
                    }
                    for (int i = from; i < length; i++) {
                        handleArrayIndex(i, currentPath, model, ctx);
                    }
                    break;

                case SLICE_TO : //[:2]
                    input = criteria.get(0);
                    length = ctx.jsonProvider().length(model);
                    to = input;
                    if (to < 0) {
                        //calculate slice end from array length
                        to = length + to;
                    }
                    to = Math.min(length, to);

                    if (length == 0) {
                        return;
                    }
                    for (int i = 0; i < to; i++) {
                        handleArrayIndex(i, currentPath, model, ctx);
                    }
                    break;

                case SLICE_BETWEEN : //[2:4]
                    from = criteria.get(0);
                    to = criteria.get(1);
                    length = ctx.jsonProvider().length(model);

                    to = Math.min(length, to);

                    if (from >= to || length == 0) {
                        return;
                    }

                    for (int i = from; i < to; i++) {
                        handleArrayIndex(i, currentPath, model, ctx);
                    }
                    break;
                }
        } catch (IndexOutOfBoundsException e) {
            throw new PathNotFoundException("Index out of bounds when evaluating path " + currentPath);
        }
    }

    @Override
    public String getPathFragment() {
        StringBuilder sb = new StringBuilder();
        if (Operation.SINGLE_INDEX == operation || Operation.INDEX_SEQUENCE == operation) {
            sb.append("[")
                    .append(Utils.join(",", "", criteria))
                    .append("]");
        } else if (Operation.CONTEXT_SIZE == operation) {
            sb.append("[@.size()")
                    .append(criteria.get(0))
                    .append("]");
        } else if (Operation.SLICE_FROM == operation) {
            sb.append("[")
                    .append(criteria.get(0))
                    .append(":]");
        } else if (Operation.SLICE_TO == operation) {
            sb.append("[:")
                    .append(criteria.get(0))
                    .append("]");
        } else if (Operation.SLICE_BETWEEN == operation) {
            sb.append("[")
                    .append(criteria.get(0))
                    .append(":")
                    .append(criteria.get(1))
                    .append("]");
        } else
            sb.append("NOT IMPLEMENTED");

        return sb.toString();
    }

    @Override
    boolean isTokenDefinite() {
        return isDefinite;
    }
}
