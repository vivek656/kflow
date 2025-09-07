package com.kflow.core


class WorkflowTest {

    @org.junit.jupiter.api.Test
    fun workflowTest() {
        val workflow = Workflow.build<String>("createClientWorkflow") {
            start(description = "Initialize workflow") {
                "creator".."john.doe"
                "env".."dev"
            }.check("Is creator an employee?") {
                contextValueMatches("creator", "john.doe")
            }.ifTrue("creator is an employee" , jumpTo = "client456") {
                "clientId"..123
            }.ifFalse("Creator not an employee") {
                "clientId"..456
            }.jumpTo("realWork")

            work("client456", description = "client is 546") {
                println("client is 456")
            }

            work(id = "realWork", description = "working my socks off") {

            }.jumpTo("beforeTerminateCheck")


            work(id = "beforeTerminateCheck", description = "beforeTerminate") {

            }.then {

            }.terminate { "thanks" }
        }.also {
            it.generateMermaidDiagramString()
        }



        Workflow.build<String>("createClientWorkflow") {
            start("getEmployee") {
                "employee".."123"
            }.check("clientCheck") {
                get("employee") == "123"
            }.ifTrue {
                println("if true employee = ${get("employee")}")
            }.ifFalse {
                println("if false employee = ${get("employee")}")
            }.then {
                println("client ${get("employee")} validated")
            }.then {
                val client = "client_${get("employee")}"
                "client"..client
            }.jumpTo("draftToInvites")

            work("draftToInvites", "converting drafts to invite") {
                println("draft updated for ${get("client")}")
            }.jumpTo("kycFlow")

            work("kycFlow", "checking and updating KYC") {
                println("kyc done ${get("client")}")
            }.terminate { get("employee") }
        }.also {
            it.generateMermaidDiagramString()
        }



        val string = workflow.generateMermaidDiagramString()

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
