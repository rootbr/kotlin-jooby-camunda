package org.rootbr

import org.camunda.bpm.BpmPlatform
import org.camunda.bpm.application.ProcessApplication
import org.camunda.bpm.application.impl.EmbeddedProcessApplication
import org.camunda.bpm.container.RuntimeContainerDelegate
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.jooby.Jooby.run
import org.jooby.Kooby
import org.jooby.json.Gzon


class JoobyCamundaApplication : Kooby({
    use(Gzon())

    onStart {
        RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(
                ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                        .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                        .setJdbcUrl("jdbc:h2:mem:my-own-db;DB_CLOSE_DELAY=1000")
                        .setJobExecutorActivate(true)
                        .buildProcessEngine()
        )
        ExampleProcessApplication().deploy();
    }

    get {
        BpmPlatform.getDefaultProcessEngine().runtimeService.startProcessInstanceByKey("Process_13nmxyw");
        "Process Engine: ${BpmPlatform.getDefaultProcessEngine().name}"
    }

    get("/task") { BpmPlatform.getDefaultProcessEngine().taskService.createTaskQuery().list() }
})

fun main(args: Array<String>) {
    run(::JoobyCamundaApplication, args)
}

@ProcessApplication(name = "example-process-application", deploymentDescriptors = ["processes.xml"])
class ExampleProcessApplication : EmbeddedProcessApplication()
