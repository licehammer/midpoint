/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.model.common.expression.evaluator.transformation;

import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.evolveum.midpoint.prism.ValueMetadata;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.delta.PlusMinusZero;
import com.evolveum.midpoint.prism.delta.PrismValueDeltaSetTriple;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.repo.common.expression.*;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ExceptionUtil;
import com.evolveum.midpoint.schema.util.TraceUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValueTransformationTraceType;
import com.evolveum.prism.xml.ns._public.types_3.PlusMinusZeroType;

/**
 * Transformation of a value tuple (single value from each source) to a collection of output values.
 * <p>
 * It is is a part of combinatorial evaluation.
 *
 * @param <V> type of the output value
 */
class ValueTupleTransformation<V extends PrismValue> implements AutoCloseable {

    private static final Trace LOGGER = TraceManager.getTrace(ValueTupleTransformation.class);

    private static final String OP_EVALUATE = ValueTupleTransformation.class.getName() + ".evaluate";

    /**
     * The whole combinatorial evaluation.
     */
    @NotNull private final CombinatorialEvaluation<V, ?, ?> combinatorialEvaluation;

    /**
     * (Client-supplied) context of the expression evaluation.
     */
    @NotNull private final ExpressionEvaluationContext context;

    /**
     * List of source triples (plus-minus-zero); with some null tweaks.
     */
    @NotNull private final List<SourceTriple<?, ?>> sourceTripleList;

    /**
     * Currently transformed value tuple - one value from every source.
     */
    @NotNull private final List<PrismValue> valuesTuple;

    /**
     * Resulting output triple where we collect results of the whole combinatorial evaluation.
     */
    @NotNull private final PrismValueDeltaSetTriple<V> outputTriple;

    /**
     * Operation result for this tuple evaluation (condition + transformation).
     */
    @NotNull private final OperationResult result;

    /**
     * Trace for the tuple evaluation.
     */
    private final ValueTransformationTraceType trace;

    /**
     * How many sources do we have?
     */
    private final int numberOfSources;

    /**
     * Does this tuple contain a value that is present in plus set of its source?
     */
    private boolean hasPlus;

    /**
     * Does this tuple contain a value that is present in minus set of its source?
     */
    private boolean hasMinus;

    /**
     * Does this tuple contain a value that is present in zero set of its source?
     */
    private boolean hasZero;

    /**
     * What state (old, new) should be input variables taken from?
     */
    private InputVariableState inputVariableState;

    private enum InputVariableState {
        OLD, NEW
    }

    /**
     * To what set should the output go?
     */
    private PlusMinusZero outputSet;

    /**
     * Result of condition evaluation (or true if there's no condition).
     * (Boolean instead of boolean to be sure that we know when it's evaluated.)
     */
    private Boolean conditionResult;

    /**
     * Result of the transformation. Empty set if condition is false. (Null means the transformation was not carried out).
     */
    private Collection<V> transformationResult;

    ValueTupleTransformation(List<PrismValue> valuesTuple, CombinatorialEvaluation<V, ?, ?> combinatorialEvaluation,
            OperationResult parentResult) {
        this.combinatorialEvaluation = combinatorialEvaluation;
        this.context = combinatorialEvaluation.context;
        this.sourceTripleList = combinatorialEvaluation.sourceTripleList;
        this.valuesTuple = valuesTuple;
        this.outputTriple = combinatorialEvaluation.outputTriple;
        this.numberOfSources = sourceTripleList.size();
        assert numberOfSources == valuesTuple.size();

        this.result = parentResult.subresult(OP_EVALUATE)
                .setMinor()
                .build();
        result.addParam("context", context.getContextDescription());
        if (result.isTraced()) {
            trace = new ValueTransformationTraceType(combinatorialEvaluation.prismContext);
            result.getTraces().add(trace);
            dumpValueCombinationToTrace();
        } else {
            trace = null;
        }
    }

    void evaluate() {
        try {
            if (!combinatorialEvaluation.evaluator.isIncludeNullInputs() && MiscUtil.isAllNull(valuesTuple)) {
                // The case that all the sources are null. There is no point executing the expression.
                setTraceComment("All sources are null and includeNullInputs is true.");
                return;
            }
            ExpressionVariables staticVariables = createStaticVariablesFromSources();
            recordBeforeTransformation();

            if (isApplicableRegardingPlusMinusSetPresence()) {

                determineInputStateAndOutputSet();
                augmentStaticVariablesWithInputVariables(staticVariables);

                evaluateConditionAndTransformation(staticVariables);

                recordTransformationResult();
                outputTriple.addAllToSet(outputSet, transformationResult);
            }

        } catch (Throwable t) {
            result.recordFatalError(t.getMessage(), t);
            throw t;
        }
    }

    /**
     * @return Final form of static (delta-less) variables derived from the sources.
     * Also sets hasPlus/hasZero/hasMinus flags.
     */
    @NotNull
    private ExpressionVariables createStaticVariablesFromSources() {
        ExpressionVariables staticVariables = new ExpressionVariables();
        for (int sourceIndex = 0; sourceIndex < numberOfSources; sourceIndex++) {
            // This strange casting is needed because of presentInPlusSet/MinusSet/ZeroSet calls
            // that expect the same type as the SourceTriple has.
            //noinspection unchecked
            SourceTriple<PrismValue, ?> sourceTriple = (SourceTriple<PrismValue, ?>) sourceTripleList.get(sourceIndex);
            PrismValue value = valuesTuple.get(sourceIndex);

            String name = sourceTriple.getName().getLocalPart();
            ItemDefinition definition = sourceTriple.getSource().getDefinition();
            if (definition == null) { // TODO reconsider @NotNull annotation on getDefinition
                throw new IllegalArgumentException("Source '" + name + "' without a definition");
            }
            staticVariables.put(name, getRealContent(value, sourceTriple.getResidualPath()), definition);
            // Note: a value may be both in plus and minus sets, e.g. in case that the value is replaced
            // with the same value. We pretend that this is the same as ADD case.
            // TODO: Maybe we will need better handling in the future. Maybe we would need
            //       to execute the script twice?
            // TODO: Couldn't we remember the set when constructing the union of triple values?
            //       We would be able to avoid searching the sets for the values here.
            if (sourceTriple.presentInPlusSet(value)) {
                hasPlus = true;
            } else if (sourceTriple.presentInZeroSet(value)) {
                hasZero = true;
            } else if (sourceTriple.presentInMinusSet(value)) {
                hasMinus = true;
            }
            if (context.getVariableProducer() != null) {
                //noinspection unchecked
                ((VariableProducer<PrismValue>) context.getVariableProducer())
                        .produce(value, context.getVariables());
            }
        }
        return staticVariables;
    }

    private Object getRealContent(PrismValue pval, ItemPath residualPath) {
        if (residualPath == null || residualPath.isEmpty()) {
            return pval;
        }
        if (pval == null) {
            return null;
        }
        return pval.find(residualPath);
    }

    private void augmentStaticVariablesWithInputVariables(ExpressionVariables staticVariables) {
        if (inputVariableState == InputVariableState.NEW) {
            staticVariables.addVariableDefinitionsNew(context.getVariables());
        } else if (inputVariableState == InputVariableState.OLD) {
            staticVariables.addVariableDefinitionsOld(context.getVariables());
        } else {
            throw new AssertionError();
        }
    }

    private boolean isApplicableRegardingPlusMinusSetPresence() {
        if (!hasPlus && !hasMinus && !hasZero && !MiscUtil.isAllNull(valuesTuple)) {
            throw new IllegalStateException("Internal error! The impossible has happened! tuple=" + valuesTuple + "; source triples: " + sourceTripleList + "; in " + context.getContextDescription());
        }

        if (hasPlus && hasMinus) {
            // The combination of values that are both in plus and minus. Evaluating this combination does not make sense.
            // Just skip it.
            //
            // Note: There will NOT be a single value that is in both plus and minus (e.g. "replace with itself" case).
            // That case is handled when setting hasPlus/hasMinus/hasZero in prepareStaticVariables() method.
            //
            // This case strictly applies to combination of different values from the plus and minus sets.
            setTraceComment("The combination of values that are both in plus and minus. Evaluating this combination does not make sense. Just skip it.");
            return false;
        } else if (hasPlus && context.isSkipEvaluationPlus()) {
            setTraceComment("The results will end up in the plus set and skipEvaluationPlus is true, therefore we can skip them.");
            return false;
        } else if (hasMinus && context.isSkipEvaluationMinus()) {
            setTraceComment("The results will end up in the minus set and skipEvaluationMinus is true, therefore we can skip them.");
            return false;
        } else {
            return true;
        }
    }

    private void determineInputStateAndOutputSet() {
        if (hasPlus) {
            // Pluses and zeroes: Result goes to plus set, use NEW values for variables
            // (No minus! This has been checked earlier)
            outputSet = PlusMinusZero.PLUS;
            inputVariableState = InputVariableState.NEW;
        } else if (hasMinus) {
            // Minuses and zeroes: Result goes to minus set, use OLD values for variables
            outputSet = PlusMinusZero.MINUS;
            inputVariableState = InputVariableState.OLD;
        } else {
            // All zeros: Result goes to zero set, use NEW values for variables
            outputSet = PlusMinusZero.ZERO;
            inputVariableState = InputVariableState.NEW;
        }
    }

    private void evaluateConditionAndTransformation(ExpressionVariables staticVariables) {
        try {
            conditionResult = evaluateCondition(staticVariables);
            if (conditionResult) {
                transformationResult = evaluateTransformation(staticVariables);
            } else {
                setTraceComment("Skipping value transformation because condition evaluated to false.");
                transformationResult = emptySet();
            }
        } catch (ExpressionEvaluationException e) {
            ExpressionEvaluationException exEx = new ExpressionEvaluationException(
                    e.getMessage() + "(" + staticVariables.dumpSingleLine() + ") in " + context.getContextDescription(),
                    e,
                    ExceptionUtil.getUserFriendlyMessage(e));
            if (combinatorialEvaluation.evaluator.localizationService != null) {
                combinatorialEvaluation.evaluator.localizationService.translate(exEx);
            }
            throw new TunnelException(exEx);
        } catch (Throwable e) {
            String msg = e.getMessage() + "(" + staticVariables.dumpSingleLine() + ") in " + context.getContextDescription();
            throw new TunnelException(MiscUtil.createSame(e, msg));
        }
    }

    private boolean evaluateCondition(ExpressionVariables staticVariables)
            throws SchemaException, ExpressionEvaluationException, ObjectNotFoundException, CommunicationException,
            ConfigurationException, SecurityViolationException {
        if (combinatorialEvaluation.conditionExpression != null) {
            ExpressionEvaluationContext conditionCtx = new ExpressionEvaluationContext(null, staticVariables,
                    "condition in " + context.getContextDescription(), context.getTask());
            PrismValueDeltaSetTriple<PrismPropertyValue<Boolean>> triple = combinatorialEvaluation.conditionExpression
                    .evaluate(conditionCtx, result);
            return ExpressionUtil.computeConditionResult(triple.getNonNegativeValues());
        } else {
            return true;
        }
    }

    @NotNull
    private List<V> evaluateTransformation(ExpressionVariables staticVariables) throws ExpressionEvaluationException,
            ObjectNotFoundException, SchemaException, CommunicationException, ConfigurationException, SecurityViolationException {
        List<V> transformationOutput = combinatorialEvaluation.evaluator.transformSingleValue(staticVariables, outputSet,
                inputVariableState == InputVariableState.NEW, context,
                context.getContextDescription(), context.getTask(), result);
        computeAndApplyOutputValueMetadata(transformationOutput);
        return transformationOutput;
    }

    private void computeAndApplyOutputValueMetadata(List<V> output) throws CommunicationException, ObjectNotFoundException,
            SchemaException, SecurityViolationException, ConfigurationException, ExpressionEvaluationException {
        ValueMetadataComputer valueMetadataComputer = context.getValueMetadataComputer();
        if (valueMetadataComputer != null) {
            ValueMetadata outputValueMetadata = valueMetadataComputer.compute(valuesTuple, result);
            if (outputValueMetadata != null) {
                for (int i = 0; i < output.size(); i++) {
                    V oVal = output.get(i);
                    if (oVal != null) {
                        if (i < output.size() - 1) {
                            oVal.setValueMetadata(outputValueMetadata.clone());
                        } else {
                            oVal.setValueMetadata(outputValueMetadata);
                        }
                    }
                }
            }
        } else {
            LOGGER.trace("No value metadata computer present, skipping metadata computation.");
        }
    }

    private void setTraceComment(String comment) {
        LOGGER.trace("{} In {}.", comment, context.getContextDescription());
        if (trace != null) {
            trace.setComment(comment);
        }
    }

    private Object dumpValueTupleLazily() {
        return new Object() {
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                Iterator<SourceTriple<?, ?>> sourceTriplesIterator = combinatorialEvaluation.sourceTripleList.iterator();
                for (PrismValue value : valuesTuple) {
                    SourceTriple<?, ?> sourceTriple = sourceTriplesIterator.next();
                    sb.append(sourceTriple.getName().getLocalPart()).append('=');
                    sb.append(value == null ? null : value.getRealValueOrRawType(combinatorialEvaluation.prismContext));
                    if (sourceTriplesIterator.hasNext()) {
                        sb.append(", ");
                    }
                }
                return sb.toString();
            }
        };
    }

    private void dumpValueCombinationToTrace() {
        Iterator<SourceTriple<?, ?>> sourceTriplesIterator = combinatorialEvaluation.sourceTripleList.iterator();
        for (PrismValue pval : valuesTuple) {
            SourceTriple<?, ?> sourceTriple = sourceTriplesIterator.next();
            trace.getInput().add(TraceUtil.toNamedValueType(pval, sourceTriple.getName(), combinatorialEvaluation.prismContext));
        }
    }

    private void recordBeforeTransformation() {
        LOGGER.trace("Processing value combination {} in {}\n   hasPlus={}, hasZero={}, hasMinus={}, skipEvaluationPlus={}, skipEvaluationMinus={}",
                dumpValueTupleLazily(), context.getContextDescription(), hasPlus, hasZero, hasMinus,
                context.isSkipEvaluationPlus(), context.isSkipEvaluationMinus());
        if (trace != null) {
            trace.setHasPlus(hasPlus);
            trace.setHasMinus(hasMinus);
            trace.setHasZero(hasZero);
        }
    }

    private void recordTransformationResult() {
        LOGGER.trace("Processed value tuple {} in {}\n  valueDestination: {}\n  scriptResults:{}{}",
                dumpValueTupleLazily(), context.getContextDescription(), outputSet, transformationResult,
                conditionResult ? "" : " (condition evaluated to false)");

        if (trace != null) {
            trace.setDestination(PlusMinusZeroType.fromValue(outputSet));
            trace.setConditionResult(conditionResult);
            trace.getOutput().addAll(TraceUtil.toAnyValueTypeList(transformationResult, combinatorialEvaluation.prismContext));
        }
    }

    @Override
    public void close() {
        result.computeStatusIfUnknown();
    }
}
