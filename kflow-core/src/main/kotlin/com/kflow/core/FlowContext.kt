package com.kflow.core

class FlowContext {
    private val data = mutableMapOf<String, Any?>()
    private val trace = mutableListOf<ExecutionLog>()

    fun <T> get(key: String): T? = data[key] as? T
    fun set(key: String, value: Any?) { data[key] = value }

    fun addTrace(nodeId: String, description: String? = null) {
        trace.add(ExecutionLog(nodeId, description))
    }

    fun getTrace(): List<ExecutionLog> = trace.toList()
}

data class ExecutionLog(val stepId: String, val description: String?)
