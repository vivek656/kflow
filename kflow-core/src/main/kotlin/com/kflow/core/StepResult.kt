package com.kflow.core

sealed class StepResult {
    data object Next : StepResult()
    data object Terminate : StepResult()
    data object Break : StepResult()
    data class JumpTo(val stepId: String) : StepResult()
}
