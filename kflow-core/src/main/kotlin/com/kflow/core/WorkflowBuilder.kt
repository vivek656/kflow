package com.kflow.core

class WorkflowBuilder(val name: String) {
    private val steps = mutableListOf<WorkflowStep>()

    fun start(
        id: String = "start",
        description: String? = null,
        block: FlowContext.() -> Unit
    ): StepChain {
        steps.add(SimpleStep(id, description, block))
        return StepChain(this, steps)
    }

    fun build(): Workflow = Workflow(name, steps)
}

fun workflow(name: String, block: WorkflowBuilder.() -> Unit): Workflow {
    val builder = WorkflowBuilder(name)
    builder.block()
    return builder.build()
}
/**
 * workflow(my flow ) start {
 *      initial context setup
 * } then {
 *      do something
 * } check ifValue getValue()
 *   is(x) {
 *
 *   } is (y) {
 *
 *   } else {
 *
 *   }
 *
 */
