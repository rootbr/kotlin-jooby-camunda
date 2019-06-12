package org.rootbr

import org.camunda.bpm.BpmPlatform
import org.camunda.bpm.application.ProcessApplication
import org.camunda.bpm.application.impl.EmbeddedProcessApplication
import org.camunda.bpm.container.RuntimeContainerDelegate
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.jooby.Jooby.run
import org.jooby.Kooby


class JoobyCamundaApplication : Kooby({
    val processEngine = BpmPlatform.getDefaultProcessEngine()
    val processApplicationService = BpmPlatform.getProcessApplicationService()
    val taskService = processEngine.taskService
    val runtimeService = processEngine.runtimeService

    get { "Process Engine: ${processEngine.name}" }

    get("/process-application") {
        "Hello ${processApplicationService.getProcessApplicationInfo("example-process-application").deploymentInfo.get(0).deploymentId}!"
    }

    get("/task") { taskService.createTaskQuery().list() }
})

fun main(args: Array<String>) {
    RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(
            ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                    .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                    .setJdbcUrl("jdbc:h2:mem:my-own-db;DB_CLOSE_DELAY=1000")
                    .setJobExecutorActivate(true)
                    .buildProcessEngine()
    )
    ExampleProcessApplication().deploy();
    run(::JoobyCamundaApplication, args)
}

@ProcessApplication(name = "example-process-application", deploymentDescriptors = ["processes.xml"])
class ExampleProcessApplication : EmbeddedProcessApplication()
