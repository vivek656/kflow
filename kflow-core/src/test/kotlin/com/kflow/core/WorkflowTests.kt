package com.kflow.core

import kotlin.test.Test
import kotlin.test.assertEquals
import com.kflow.core2.Workflow as Workflow2

class WorkflowTest {

    @Test
    fun `test createClientWorkflow`() {
        val workflow = workflow("createClientWorkflow") {
            start(description = "Initialize workflow") {
                set("creator", "john.doe")
                set("env", "dev")
            }.check(
                description = "Is creator an employee?",
                condition = { get<String>("creator") == "john.doe" },
                ifTrue = {
                    then(description = "Retrieve environment variable") {
                        val env = get<String>("env")
                        set("clientType", if (env == "dev") "premium" else "standard")
                    }
                },
                ifFalse = {
                    terminate(description = "Unauthorized creator")
                }
            ).check(
                description = "Is client type premium?",
                condition = { get<String>("clientType") == "premium" },
                ifTrue = {
                    then(description = "Create premium client") {
                        set("clientId", 123)
                    }
                },
                ifFalse = {
                    then(description = "Create standard client") {
                        set("clientId", 456)
                    }
                }
            ).then(description = "Log client creation") {
                val id = get<Int>("clientId")
                println("Client created with ID: $id")
            }.terminate(description = "Workflow finished")
        }

        val context = FlowContext()
        val result = WorkflowExecutor(workflow.steps).run(context)

        //assertEquals(StepResult.Next, result)
        assertEquals(123, context.get<Int>("clientId"))
    }

    @Test
    fun workflow2(){
        val workflow = Workflow2.build<String>("createClientWorkflow") {
            start(description = "Initialize workflow") {
                "creator" .. "john.doe"
                "env" .. "dev"
            }.check("Is creator an employee?") {
                contextValueMatches("creator" , "john.doe")
            }.ifTrue("creator is an employee") {
                "clientId" .. 123
            }.ifFalse("Creator not an employee") {
                "clientId" .. 456
            }.jumpTo("realWork")

            work(id = "realWork" , description = "working my socks off") {

            }.jumpTo("beforeTerminateCheck")

            work(id = "beforeTerminateCheck", description = "beforeTerminate"){

            }.then {

            }.terminate { "thanks" }
        }

        val string = workflow.generateDiagramString()

//        workflow.build {
//            start("staring workflow") {
//                ...
//            } check("checking condition" , id = "check creator id") {
//                ...
//                true
//            } ifTrue {
//                jumpTo("creation block")
//            } ifFalse {
//                terminate()
//            }
//
//            blockStart("creationBlock") {
//
//            } check () {
//                ...
//            }ifFalse {
//                jumpBackTo("check creator id")
//            }ifTrue {
//                ....
//            } than {
//                .....
//            }
//        }
//
//        workFlow.getDaigram()
    }
}
