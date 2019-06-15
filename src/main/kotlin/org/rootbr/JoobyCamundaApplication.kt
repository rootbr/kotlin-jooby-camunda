package org.rootbr

import org.camunda.bpm.BpmPlatform
import org.camunda.bpm.application.ProcessApplication
import org.camunda.bpm.application.impl.EmbeddedProcessApplication
import org.camunda.bpm.container.RuntimeContainerDelegate
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.variable.Variables
import org.camunda.spin.Spin.JSON
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin
import org.jooby.Jooby.run
import org.jooby.Kooby

data class Dto(val testString: String)
class JoobyCamundaApplication : Kooby({

    onStart {
        val processEngine = (ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                as StandaloneInMemProcessEngineConfiguration).apply {
            processEnginePlugins.add(SpinProcessEnginePlugin())
            defaultSerializationFormat = Variables.SerializationDataFormats.JSON.name
            databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
            jdbcUrl = "jdbc:h2:tcp://localhost/~/test;DB_CLOSE_DELAY=1000"
            setJobExecutorActivate(true)
        }.buildProcessEngine()
        RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(processEngine)
        ExampleProcessApplication().deploy()
        val runtimeService = BpmPlatform.getDefaultProcessEngine().runtimeService
        val processInstance = runtimeService.startProcessInstanceByKey("Process_13nmxyw")
    }

    get {
        val json = JSON("{\"val\":\"var\"}")
        val taskService = BpmPlatform.getDefaultProcessEngine().taskService
        val task = taskService.createTaskQuery().list().first()

        taskService.setVariable(task.id, "jsonVariable5", json)
        taskService.getVariable(task.id, "jsonVariable4")
    }

    get("/task") { BpmPlatform.getDefaultProcessEngine().taskService.createTaskQuery().list() }

    get("/task/{id}/{variableName}") { req ->
        BpmPlatform.getDefaultProcessEngine()
                .taskService.getVariable(
                req.param("id").value(),
                req.param("variableName").value()
        )
    }


})

fun main(args: Array<String>) {
    run(::JoobyCamundaApplication, args)
}

@ProcessApplication(name = "example-process-application", deploymentDescriptors = ["processes.xml"])
class ExampleProcessApplication : EmbeddedProcessApplication()
