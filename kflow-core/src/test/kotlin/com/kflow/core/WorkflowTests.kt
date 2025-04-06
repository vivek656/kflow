package com.kflow.core

import kotlin.test.Test
import com.kflow.core.Workflow as Workflow2

class WorkflowTest {

    @Test
    fun workflowTest(){
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
