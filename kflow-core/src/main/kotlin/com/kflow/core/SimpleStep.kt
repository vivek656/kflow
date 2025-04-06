package com.kflow.core

class SimpleStep(
    override val id: String,
    override val description: String? = null,
    private val block: FlowContext.() -> Unit
) : WorkflowStep {
    override fun execute(context: FlowContext): StepResult {
        block(context)
        return StepResult.Next
    }
}
