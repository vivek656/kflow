package com.kflow.core

class WorkflowExecutor(private val steps: List<WorkflowStep>) {
    fun run(context: FlowContext): StepResult {
        var index = 0
        while (index < steps.size) {
            val step = steps[index]
            context.addTrace(step.id, step.description)

            when (val result = step.execute(context)) {
                is StepResult.Next -> index++
                is StepResult.Terminate -> return result
                is StepResult.Break -> break
                is StepResult.JumpTo -> index = steps.indexOfFirst { it.id == result.stepId }
            }
        }
        return StepResult.Next
    }
}
