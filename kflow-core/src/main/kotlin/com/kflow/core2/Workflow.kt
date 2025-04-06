package com.kflow.core2

import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class WorkflowDSl

enum class WorkflowState {
    CREATED, ALl_STEPS_DISCOVERED, ALL_STEPS_RELATION_CONFIGURED, READY
}

@WorkflowDSl
class Workflow<RESULT>(val name: String) {



    companion object {
        fun <T> build(name: String, init: @WorkflowDSl Workflow<T>.() -> Unit): Workflow<T> {
            return Workflow<T>(name).apply(init)
        }
    }

    fun validate() {

    }

    private val workflowContext = WorkflowContext(mutableMapOf());

    fun generateDiagramString() = sharedWorkflow.generateMermaid()


    private val sharedWorkflow = SharedWorkflowInfo(
        name, mutableMapOf(), mutableMapOf()
    )

    fun start(description: String, init: @WorkflowDSl ExecutionContext.() -> Unit) = StartStep(
        sharedWorkflow, description, init
    ).also {
        sharedWorkflow.addStep(it)
    }

    fun <E> ExecutableWorkFlowStep<E>.check(
        description: String,
        init: ExecutionContext.() -> Boolean
    ): ConditionalStep {
        val edgeType = WorkflowEdgeType.NEXT()
        sharedWorkflow.validateCanAddEdge(id, edgeType)
        val step = ConditionalStep(
            sharedWorkflow, description, init
        )
        sharedWorkflow.addStep(step)
        sharedWorkflow.addEdge(
            WorkflowStepEdge(
                from = id, to = step.id, edgeType
            )
        )
        return step
    }

    fun ConditionalStep.ifTrue(
        description: String? = null,
        init: ExecutionContext.() -> Unit
    ): ConditionalStep {
        val edgeType = WorkflowEdgeType.ON_VALUE(true)
        sharedWorkflow.validateCanAddEdge(id, edgeType)
        val step = IntermediateDependentStep(
            this,
            id = "onTrue_$id",
            description = description ?: "on true of ${this.description}",
            executable = init
        )
        sharedWorkflow.addStep(step)
        sharedWorkflow.addEdge(
            WorkflowStepEdge(
                from = this.id, to = step.id, type = edgeType
            )
        )
        return this
    }

    fun ConditionalStep.ifFalse(
        description: String? = null,
        init: ExecutionContext.() -> Unit
    ): ConditionalStep {
        val edgeType = WorkflowEdgeType.ON_VALUE(false)
        sharedWorkflow.validateCanAddEdge(id, edgeType)
        val step = IntermediateDependentStep(
            this,
            id = "onFalse_$id",
            description = description ?: "on false of ${this.description}",
            executable = init
        )
        sharedWorkflow.addStep(step)
        sharedWorkflow.addEdge(
            WorkflowStepEdge(
                from = this.id, to = step.id, type = edgeType
            )
        )
        return this
    }

    fun <T, R : Any> ExecutableWorkFlowStep<T>.then(
        description: String? = null,
        init: ExecutionContext.() -> R
    ): ExecutableWorkFlowStep<R> {
        val edgeType = WorkflowEdgeType.NEXT()
        sharedWorkflow.validateCanAddEdge(id, edgeType)
        val step = IntermediateDependentStep(
            id = "${this.id}_NEXT_${Random.nextInt(500)}",
            description = description ?: "then : ${this.description}",
            executable = init,
            parentStep = this
        )
        sharedWorkflow.addStep(step)
        sharedWorkflow.addEdge(
            WorkflowStepEdge(from = this.id, to = step.id, type = edgeType)
        )
        return step
    }

    /**
     * @param id : needs to be unique per [workflow] else will result in error
     */
    fun <R> work(
        id: String? = null,
        description: String,
        init: ExecutionContext.() -> R
    ): IndependentWorkflowStep<R> {
        val stepId = id?.also { sharedWorkflow.validateCanAddStep(it) } ?: "Work${UUID.randomUUID()}"
        val step = IndependentWorkflowStep(stepId, description, init)
        sharedWorkflow.addStep(step)
        return step
    }


    fun <R> ExecutableWorkFlowStep<R>.jumpTo(id: String) {
        val edge = WorkflowEdgeType.JUMP_TO()
        sharedWorkflow.validateCanAddEdge(this.id, edge)
        sharedWorkflow.addEdge(
            WorkflowStepEdge(
                from = this.id, to = id, edge
            )
        )
    }

    fun <T> ExecutableWorkFlowStep<*>.terminate(init: (ExecutionContext.() -> T?)? = null): TerminationStep<T?> {
        val terminationStep = TerminationStep(
            "terminate_${Random.nextInt(1000)}_${this@Workflow.name}",
            description = "terminate after ${this.description}",
            init ?: { null }
        )
        val edge = WorkflowEdgeType.NEXT()
        sharedWorkflow.validateCanAddEdge(this.id, edge)
        sharedWorkflow.addStep(terminationStep)
        sharedWorkflow.addEdge(
            workflowStepEdge = WorkflowStepEdge(
                from = this.id, to = terminationStep.id, edge
            )
        )
        return terminationStep
    }
}

class ExecutionContext(
    private val workflowContext: WorkflowContext,
) {
    fun setContext(key: String, value: Any) {
        workflowContext.setContext(key, value)
    }

    operator fun get(str: String) = workflowContext[str]

    fun contextValueMatches(str: String, value: Any): Boolean {
        return workflowContext.contextValueMatches(str, value)
    }

    operator fun String.rangeTo(value: Any) = setContext(this, value)

}

class WorkflowContext(
    private val contextMap: MutableMap<String, Any>
) {
    fun setContext(key: String, value: Any) {
        contextMap[key] = value
    }

    operator fun get(str: String) = contextMap[str]

    fun contextValueMatches(str: String, value: Any): Boolean {
        return this[str]?.equals(value) == true
    }

    operator fun String.rangeTo(value: Any) = setContext(this, value)
}


//______________
class SharedWorkflowInfo(
    val workflowName: String,
    private val workFlowStepGraph: MutableMap<String, MutableList<WorkflowStepEdge>>,
    private val workFlowSteps: MutableMap<String, ExecutableWorkFlowStep<*>>
) {
    fun addStep(step: ExecutableWorkFlowStep<*>): ExecutableWorkFlowStep<*> {
        if (workFlowSteps.containsKey(step.id)) {
            throw IllegalStateException("Cannot add id ${step.id} to workflow $workflowName as there already a step for same id")
        }
        workFlowSteps[step.id] = step
        workFlowStepGraph[step.id] = mutableListOf()
        return step
    }


    fun validateCanAddEdge(id: String, edgeType: WorkflowEdgeType) {
        if (getStepOutGoingEdges(id).any { it.type == edgeType }) {
            throw IllegalArgumentException("cannot add EDGE ${edgeType.name} to step with id $id as there is already a ${edgeType.name} configured for id")
        }
    }

    fun validateCanAddStep(id: String) {
        if (workFlowSteps.containsKey(id)) {
            throw IllegalStateException("Cannot add step with id $id to workflow $workflowName")
        }
    }

    fun addEdge(workflowStepEdge: WorkflowStepEdge) {
        getStepOutGoingEdges(workflowStepEdge.from).add(workflowStepEdge)
    }

    fun getStepOutGoingEdges(id: String): MutableList<WorkflowStepEdge> {
        return workFlowStepGraph[id]
            ?: throw IllegalStateException("Some thing went wrong, as workflow $workflowName does not have ant step with id $id")

    }

    fun getStep(id: String): ExecutableWorkFlowStep<*> {
        return workFlowSteps[id]
            ?: throw IllegalStateException("Some thing went wrong, as workflow $workflowName does not have ant step with id $id")
    }

    fun generateMermaid(): String {
        val builder = StringBuilder()
        builder.appendLine("graph TD")

        for ((_, edges) in workFlowStepGraph) {
            for (edge in edges) {
                val fromStep = workFlowSteps[edge.from]
                val toStep = workFlowSteps[edge.to]

                val fromLabel = "${edge.from}[\"${fromStep?.description ?: edge.from}\"]"
                val toLabel = "${edge.to}[\"${toStep?.description ?: edge.to}\"]"
                val label = edge.type.name

                builder.appendLine("  $fromLabel -->|$label| $toLabel")
            }
        }

        return builder.toString()
    }
}

sealed class WorkflowEdgeType(val name: String, private val value: Any? = null) {
    override fun hashCode(): Int {
        return value?.let { this.name.hashCode() + value.hashCode() } ?: this.name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WorkflowEdgeType) return false
        return this.name == other.name && this.value == other.value
    }

    class NEXT : WorkflowEdgeType("NEXT")
    class JUMP_TO : WorkflowEdgeType("JUMP_TO")
    class ON_VALUE<T>(value: T) : WorkflowEdgeType("ON_VALUE", value)
}

class WorkflowStepEdge(
    val from: String,
    val to: String,
    val type: WorkflowEdgeType
)


abstract class ExecutableWorkFlowStep<T>(
    val id: String,
    val description: String,
    val executable: (ExecutionContext.() -> T),

    ) {
    abstract fun getExecutableContainer(context: ExecutionContext): ExecutableContainer<T>
}

abstract class ExecutableWorkflowStepNotReturning(
    id: String, description: String, executable: ExecutionContext.() -> Unit
) : ExecutableWorkFlowStep<Unit>(id, description, executable)

open class IndependentWorkflowStep<R>(
    id: String, description: String, executable: ExecutionContext.() -> R
) : ExecutableWorkFlowStep<R>(id, description, executable) {
    override fun getExecutableContainer(context: ExecutionContext): ExecutableContainer<R> {
        return ExecutableContainer(context, executable)
    }
}


class StartStep(
    workflowInfo: SharedWorkflowInfo,
    description: String = "start of workflow ${workflowInfo.workflowName}",
    executable: (ExecutionContext.() -> Unit)
) : ExecutableWorkflowStepNotReturning(
    id = "__start_${workflowInfo.workflowName}",
    description = description,
    executable
) {
    override fun getExecutableContainer(context: ExecutionContext): ExecutableContainer<Unit> {
        return ExecutableContainer(context, executable)
    }
}

class ConditionalStep(
    workflowInfo: SharedWorkflowInfo,
    description: String = "condition on workflow ${workflowInfo.workflowName}",
    executable: (ExecutionContext.() -> Boolean)
) : ExecutableWorkFlowStep<Boolean>(
    id = "__condition_${Random.nextInt().absoluteValue}_${workflowInfo.workflowName}",
    description = description,
    executable = executable
) {
    override fun getExecutableContainer(context: ExecutionContext): ExecutableContainer<Boolean> {
        return ChainedExecutable(ExecutableContainer(context, executable))
    }
}

class IntermediateDependentStep<T>(
    parentStep: ExecutableWorkFlowStep<*>,
    description: String = "childStep of ${parentStep.description}",
    id: String = "childStep${UUID.randomUUID()}_${parentStep.id}",
    executable: ExecutionContext.() -> T
) : ExecutableWorkFlowStep<T>(id = id, description = description, executable) {
    override fun getExecutableContainer(context: ExecutionContext): ExecutableContainer<T> {
        return ExecutableContainer(context, executable)
    }
}

class TerminationStep<T>(id: String, description: String, executable: ExecutionContext.() -> T) :
    IndependentWorkflowStep<T>(
        id, description,
        executable
    )

open class ExecutableContainer<T>(
    private val context: ExecutionContext? = null,
    private val executable: (ExecutionContext.() -> T)? = null
) {
    open fun execute() = context?.let { executable?.invoke(it) }
}

class ChainedExecutable<T>(val initialExecute: ExecutableContainer<T>) : ExecutableContainer<T>() {

    val mapOn = mutableMapOf<T, ExecutableContainer<*>>()
    fun <E> on(value: T, executableContainer: ExecutableContainer<E>): ExecutableContainer<E> {
        mapOn[value] = executableContainer
        return executableContainer
    }

    override fun execute(): T? {
        val initialValue = initialExecute.execute()
        mapOn[initialValue]?.execute()
        return initialValue
    }


}