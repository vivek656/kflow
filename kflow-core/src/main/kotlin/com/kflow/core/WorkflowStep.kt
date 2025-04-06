package com.kflow.core

interface WorkflowStep {
    val id: String
    val description: String?
    fun execute(context: FlowContext): StepResult
}
