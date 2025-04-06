package com.kflow.core

class StepChain(
    private val builder: WorkflowBuilder,
    private val steps: MutableList<WorkflowStep>
) {

    fun check(
        id: String = randomId(),
        description: String? = null,
        condition: FlowContext.() -> Boolean,
        ifTrue: StepChain.() -> Unit,
        ifFalse: StepChain.() -> Unit = {}
    ): StepChain {
        val trueBranch = StepChain(builder, mutableListOf()).apply(ifTrue).steps
        val falseBranch = StepChain(builder, mutableListOf()).apply(ifFalse).steps
        val conditionalStep = ConditionalStep(id, description, condition, trueBranch, falseBranch)
        steps.add(conditionalStep)
        return this
    }

    fun then(
        id: String = randomId(),
        description: String? = null,
        block: FlowContext.() -> Unit
    ): StepChain {
        steps.add(SimpleStep(id, description, block))
        return this
    }

    fun terminate(
        id: String = "terminate",
        description: String? = "Terminating"
    ): StepChain {
        steps.add(object : WorkflowStep {
            override val id = id
            override val description = description
            override fun execute(context: FlowContext) = StepResult.Terminate
        })
        return this
    }
}

fun randomId(): String = "step_" + (1000..9999).random()
