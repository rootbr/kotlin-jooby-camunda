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
import org.jooby.Results
import org.rootbr.camunda.spin.gson.GsonNode


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

    assets("/**/*.js")
    assets("/index.html")
    assets("/*.bpmn")

    get("/api") {
        val json = JSON("{\"val\":\"var\"}")
        val taskService = BpmPlatform.getDefaultProcessEngine().taskService
        val task = taskService.createTaskQuery().list().first()

        taskService.setVariable(task.id, "jsonVariable5", json)
        val variable = taskService.getVariable(task.id, "jsonVariable4") as GsonNode
        variable.prop("val").value()
    }

    post("/engine-rest/deployment/create") {
        //TODO
    }.consumes("json")

    post("/api/message/{messageName}") { request ->
        val messageName = request.param("messageName").value()
        val businessKey = request.param("businessKey").value()
        val body = request.body(String::class.java)
        BpmPlatform.getDefaultProcessEngine().runtimeService.correlateMessage(messageName, businessKey, mapOf(messageName to JSON(body)))
    }.consumes("json").produces("json")


})

fun main(args: Array<String>) {
    run(::JoobyCamundaApplication, args)
}

@ProcessApplication(name = "example-process-application", deploymentDescriptors = ["processes.xml"])
class ExampleProcessApplication : EmbeddedProcessApplication()
