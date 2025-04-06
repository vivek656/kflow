package com.kflow.core

fun generateMermaidGraph(steps: List<WorkflowStep>): String {
    return buildString {
        appendLine("graph TD")
        steps.windowed(2, 1, partialWindows = true).forEach { pair ->
            val from = pair[0]
            val to = pair.getOrNull(1)
            appendLine("${from.id}(${from.description ?: from.id}) --> ${to?.id ?: "End"}")
        }
    }
}
