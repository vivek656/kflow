package com.kflow.core

class ConditionalStep(
    override val id: String,
    override val description: String? = null,
    private val condition: FlowContext.() -> Boolean,
    private val ifTrueSteps: List<WorkflowStep>,
    private val ifFalseSteps: List<WorkflowStep>
) : WorkflowStep {
    override fun execute(context: FlowContext): StepResult {
        return if (context.condition()) {
            WorkflowExecutor(ifTrueSteps).run(context)
        } else {
            WorkflowExecutor(ifFalseSteps).run(context)
        }
    }
}
